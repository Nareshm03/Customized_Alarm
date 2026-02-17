package com.example.teacherscheduler.features.department.repository

import android.net.Uri
import android.util.Log
import com.example.teacherscheduler.data.FirestoreManager
import com.example.teacherscheduler.features.department.data.local.DepartmentResourceDao
import com.example.teacherscheduler.features.department.model.DepartmentResource
import com.example.teacherscheduler.features.department.model.ResourceVisibility
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Enhanced DepartmentResourceRepository with real-time Firestore listeners
 *
 * Features:
 * - Real-time updates via Firestore snapshots
 * - Automatic listener management and cleanup
 * - Efficient data mapping
 * - Memory leak prevention
 * - Separate flows for department and private resources
 */
class DepartmentResourceRepository(
    private val resourceDao: DepartmentResourceDao,
    private val firestoreManager: FirestoreManager
) {
    private val TAG = "DepartmentResourceRepository"
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    // Coroutine scope for background operations with proper lifecycle
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ==================== REAL-TIME LISTENERS ====================

    /**
     * Get real-time department resources with Firestore listener
     * Automatically syncs changes from Firestore to local database
     *
     * @param departmentId Department ID to filter resources
     * @return Flow of resources list with real-time updates
     */
    fun getDepartmentResourcesRealtime(departmentId: String): Flow<List<DepartmentResource>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            if (!firestoreManager.isUserLoggedIn()) {
                Log.w(TAG, "User not logged in, returning local data only")
                // Fallback to local data
                resourceDao.getDepartmentResources(departmentId).collect { resources ->
                    trySend(resources)
                }
                return@callbackFlow
            }

            Log.d(TAG, "Setting up real-time listener for department: $departmentId")

            // Set up Firestore real-time listener
            listenerRegistration = db.collection("departments")
                .document(departmentId)
                .collection("resources")
                .whereEqualTo("departmentId", departmentId)
                .whereEqualTo("visibility", ResourceVisibility.DEPARTMENT.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore listener error", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d(TAG, "Received ${snapshot.documents.size} department resources from Firestore")

                        // Map Firestore documents to DepartmentResource objects
                        val resources = snapshot.documents.mapNotNull { document ->
                            try {
                                val data = document.data ?: return@mapNotNull null

                                DepartmentResource(
                                    resourceId = data["resourceId"] as? String ?: document.id,
                                    title = data["title"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    subjectName = data["subjectName"] as? String ?: "",
                                    uploadedBy = data["uploadedBy"] as? String ?: "",
                                    departmentId = data["departmentId"] as? String ?: departmentId,
                                    visibility = ResourceVisibility.valueOf(
                                        data["visibility"] as? String ?: ResourceVisibility.DEPARTMENT.name
                                    ),
                                    fileUrl = data["fileUrl"] as? String ?: "",
                                    fileType = data["fileType"] as? String ?: "",
                                    uploadDate = (data["uploadDate"] as? Number)?.toLong()
                                        ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping resource document: ${document.id}", e)
                                null
                            }
                        }

                        // Sync to local database in background
                        repositoryScope.launch {
                            try {
                                resources.forEach { resource ->
                                    resourceDao.insert(resource)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error syncing to local database", e)
                            }
                        }

                        // Emit updated list
                        trySend(resources)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up real-time listener", e)
            close(e)
        }

        // Cleanup listener when Flow is cancelled
        awaitClose {
            Log.d(TAG, "Removing department resources listener for: $departmentId")
            listenerRegistration?.remove()
        }
    }.catch { e ->
        Log.e(TAG, "Flow error, falling back to local data", e)
        // Fallback to local database on error
        resourceDao.getDepartmentResources(departmentId).collect { emit(it) }
    }

    /**
     * Get real-time private resources with Firestore listener
     * Filters resources uploaded by specific user with private visibility
     *
     * @param userId User ID to filter private resources
     * @return Flow of private resources list with real-time updates
     */
    fun getPrivateResourcesRealtime(userId: String, departmentId: String): Flow<List<DepartmentResource>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null

        try {
            if (!firestoreManager.isUserLoggedIn()) {
                Log.w(TAG, "User not logged in, returning local data only")
                // Fallback to local data
                resourceDao.getPrivateResources(userId).collect { resources ->
                    trySend(resources)
                }
                return@callbackFlow
            }

            Log.d(TAG, "Setting up real-time listener for private resources: $userId")

            // Set up Firestore real-time listener for private resources
            listenerRegistration = db.collection("departments")
                .document(departmentId)
                .collection("resources")
                .whereEqualTo("uploadedBy", userId)
                .whereEqualTo("visibility", ResourceVisibility.PRIVATE.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore listener error for private resources", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d(TAG, "Received ${snapshot.documents.size} private resources from Firestore")

                        // Map Firestore documents to DepartmentResource objects
                        val resources = snapshot.documents.mapNotNull { document ->
                            try {
                                val data = document.data ?: return@mapNotNull null

                                DepartmentResource(
                                    resourceId = data["resourceId"] as? String ?: document.id,
                                    title = data["title"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    subjectName = data["subjectName"] as? String ?: "",
                                    uploadedBy = data["uploadedBy"] as? String ?: userId,
                                    departmentId = data["departmentId"] as? String ?: departmentId,
                                    visibility = ResourceVisibility.valueOf(
                                        data["visibility"] as? String ?: ResourceVisibility.PRIVATE.name
                                    ),
                                    fileUrl = data["fileUrl"] as? String ?: "",
                                    fileType = data["fileType"] as? String ?: "",
                                    uploadDate = (data["uploadDate"] as? Number)?.toLong()
                                        ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping private resource document: ${document.id}", e)
                                null
                            }
                        }

                        // Sync to local database in background
                        repositoryScope.launch {
                            try {
                                resources.forEach { resource ->
                                    resourceDao.insert(resource)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error syncing private resources to local database", e)
                            }
                        }

                        // Emit updated list
                        trySend(resources)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up private resources listener", e)
            close(e)
        }

        // Cleanup listener when Flow is cancelled
        awaitClose {
            Log.d(TAG, "Removing private resources listener for: $userId")
            listenerRegistration?.remove()
        }
    }.catch { e ->
        Log.e(TAG, "Flow error for private resources, falling back to local data", e)
        // Fallback to local database on error
        resourceDao.getPrivateResources(userId).collect { emit(it) }
    }

    // ==================== EXISTING METHODS (Keep for backward compatibility) ====================

    /**
     * Upload resource to Firebase Storage and Firestore
     * Legacy method - still functional
     */
    suspend fun uploadResource(
        fileUri: Uri,
        title: String,
        description: String,
        subjectName: String,
        uploadedBy: String,
        departmentId: String,
        visibility: ResourceVisibility,
        fileType: String
    ): Result<String> {
        return try {
            if (!firestoreManager.isUserLoggedIn()) {
                return Result.failure(Exception("User not logged in"))
            }
            
            val resourceId = UUID.randomUUID().toString()
            val fileName = "${resourceId}_${System.currentTimeMillis()}.${fileType}"
            val storagePath = "departments/$departmentId/resources/$fileName"
            
            val storageRef = storage.reference.child(storagePath)
            storageRef.putFile(fileUri).await()
            val fileUrl = storageRef.downloadUrl.await().toString()
            
            val resource = DepartmentResource(
                resourceId = resourceId,
                title = title,
                description = description,
                subjectName = subjectName,
                uploadedBy = uploadedBy,
                departmentId = departmentId,
                visibility = visibility,
                fileUrl = fileUrl,
                fileType = fileType,
                uploadDate = System.currentTimeMillis()
            )
            
            resourceDao.insert(resource)
            syncResourceToFirestore(resource)
            
            Log.d(TAG, "Resource uploaded successfully: $resourceId")
            Result.success(resourceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading resource", e)
            Result.failure(e)
        }
    }
    
    fun fetchDepartmentResources(departmentId: String, userId: String): Flow<List<DepartmentResource>> {
        return resourceDao.getDepartmentResources(departmentId)
    }
    
    fun fetchPrivateResources(userId: String): Flow<List<DepartmentResource>> {
        return resourceDao.getPrivateResources(userId)
    }
    
    suspend fun getResourceById(resourceId: String, userId: String, userDepartmentId: String): Result<DepartmentResource> {
        return try {
            val resource = resourceDao.getResourceByResourceId(resourceId)
                ?: return Result.failure(Exception("Resource not found"))
            
            val hasAccess = when (resource.visibility) {
                ResourceVisibility.PRIVATE -> resource.uploadedBy == userId
                ResourceVisibility.DEPARTMENT -> resource.departmentId == userDepartmentId
            }
            
            if (!hasAccess) {
                return Result.failure(Exception("Access denied: Insufficient permissions"))
            }
            
            Result.success(resource)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching resource", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteResource(
        resourceId: String,
        userId: String,
        isHOD: Boolean,
        userDepartmentId: String
    ): Result<Unit> {
        return try {
            val resource = resourceDao.getResourceByResourceId(resourceId)
                ?: return Result.failure(Exception("Resource not found"))
            
            val canDelete = when (resource.visibility) {
                ResourceVisibility.PRIVATE -> resource.uploadedBy == userId
                ResourceVisibility.DEPARTMENT -> {
                    if (isHOD && resource.departmentId == userDepartmentId) {
                        true
                    } else {
                        resource.uploadedBy == userId
                    }
                }
            }
            
            if (!canDelete) {
                return Result.failure(Exception("Unauthorized: Insufficient permissions to delete"))
            }
            
            deleteFileFromStorage(resource.fileUrl)
            resourceDao.deleteByResourceId(resourceId)
            deleteResourceFromFirestore(resourceId, resource.departmentId)
            
            Log.d(TAG, "Resource deleted successfully: $resourceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting resource", e)
            Result.failure(e)
        }
    }
    
    private suspend fun syncResourceToFirestore(resource: DepartmentResource) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            val resourceData = hashMapOf(
                "resourceId" to resource.resourceId,
                "title" to resource.title,
                "description" to resource.description,
                "subjectName" to resource.subjectName,
                "uploadedBy" to resource.uploadedBy,
                "departmentId" to resource.departmentId,
                "visibility" to resource.visibility.name,
                "fileUrl" to resource.fileUrl,
                "fileType" to resource.fileType,
                "uploadDate" to resource.uploadDate
            )
            
            db.collection("departments")
                .document(resource.departmentId)
                .collection("resources")
                .document(resource.resourceId)
                .set(resourceData, SetOptions.merge())
                .await()
            
            Log.d(TAG, "Resource synced to Firestore: ${resource.resourceId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing resource to Firestore", e)
        }
    }
    
    private suspend fun deleteFileFromStorage(fileUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
            Log.d(TAG, "File deleted from Storage: $fileUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file from Storage", e)
        }
    }
    
    private suspend fun deleteResourceFromFirestore(resourceId: String, departmentId: String) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            db.collection("departments")
                .document(departmentId)
                .collection("resources")
                .document(resourceId)
                .delete()
                .await()
            
            Log.d(TAG, "Resource deleted from Firestore: $resourceId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting resource from Firestore", e)
        }
    }
    
    suspend fun syncFromFirestore(departmentId: String, userId: String): Result<Unit> {
        return try {
            if (!firestoreManager.isUserLoggedIn()) {
                return Result.failure(Exception("User not logged in"))
            }
            
            val resourcesSnapshot = db.collection("departments")
                .document(departmentId)
                .collection("resources")
                .get()
                .await()
            
            for (document in resourcesSnapshot.documents) {
                val data = document.data ?: continue
                
                val visibility = ResourceVisibility.valueOf(data["visibility"] as? String ?: "DEPARTMENT")
                val uploadedBy = data["uploadedBy"] as? String ?: ""
                
                if (visibility == ResourceVisibility.PRIVATE && uploadedBy != userId) {
                    continue
                }
                
                val resource = DepartmentResource(
                    resourceId = data["resourceId"] as? String ?: continue,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    subjectName = data["subjectName"] as? String ?: "",
                    uploadedBy = uploadedBy,
                    departmentId = data["departmentId"] as? String ?: "",
                    visibility = visibility,
                    fileUrl = data["fileUrl"] as? String ?: "",
                    fileType = data["fileType"] as? String ?: "",
                    uploadDate = (data["uploadDate"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                
                resourceDao.insert(resource)
            }
            
            Log.d(TAG, "Synced resources from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firestore", e)
            Result.failure(e)
        }
    }

    // ==================== CLEANUP ====================

    /**
     * Cleanup method to cancel all ongoing coroutines
     * Should be called when the repository is no longer needed
     * Prevents memory leaks by canceling all background operations
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up DepartmentResourceRepository")
        repositoryScope.coroutineContext.cancel()
    }
}
