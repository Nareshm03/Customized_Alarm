package com.example.teacherscheduler.util

import android.content.Context
import com.example.teacherscheduler.data.DepartmentManager
import com.example.teacherscheduler.data.ProfileManager
import com.example.teacherscheduler.model.UserRole
import kotlinx.coroutines.runBlocking

/**
 * Centralized manager for role-based permissions and access control
 *
 * This class provides methods to check permissions based on user roles.
 * It follows the principle: Same app, same login, different permissions.
 *
 * STEP 2: Enhanced with department-aware permissions
 */
class RoleManager(private val context: Context) {

    private val profileManager = ProfileManager(context)
    private val departmentManager = DepartmentManager(context)

    companion object {
        // Singleton instance
        @Volatile
        private var instance: RoleManager? = null

        fun getInstance(context: Context): RoleManager {
            return instance ?: synchronized(this) {
                instance ?: RoleManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Get current user's role
     */
    fun getCurrentUserRole(): UserRole {
        val profile = profileManager.getUserProfile()
        return profile.getUserRole()
    }

    /**
     * Get current user's profile
     */
    fun getCurrentUserProfile() = profileManager.getUserProfile()

    /**
     * Check if current user is HOD
     */
    fun isCurrentUserHOD(): Boolean {
        return getCurrentUserRole().isHOD()
    }

    /**
     * Check if current user is Teacher
     */
    fun isCurrentUserTeacher(): Boolean {
        return getCurrentUserRole().isTeacher()
    }

    // ==================== DEPARTMENT-AWARE PERMISSIONS ====================

    /**
     * Check if user is HOD of specific department
     */
    fun isHODOfDepartment(departmentId: Long, userId: String): Boolean = runBlocking {
        departmentManager.isHODOfDepartment(departmentId, userId)
    }

    /**
     * Check if user is member of department
     */
    fun isMemberOfDepartment(departmentId: Long, userId: String): Boolean = runBlocking {
        departmentManager.isMemberOfDepartment(departmentId, userId)
    }

    /**
     * Can manage department (HOD only)
     */
    fun canManageDepartment(departmentId: Long): Boolean {
        val userId = getCurrentUserProfile().id
        return isCurrentUserHOD() && isHODOfDepartment(departmentId, userId)
    }

    /**
     * Can create announcements in department
     */
    fun canCreateAnnouncements(departmentId: Long): Boolean {
        val userId = getCurrentUserProfile().id
        return isHODOfDepartment(departmentId, userId)
    }

    /**
     * Can assign tasks in department
     */
    fun canAssignTasks(departmentId: Long): Boolean {
        val userId = getCurrentUserProfile().id
        return isHODOfDepartment(departmentId, userId)
    }

    /**
     * Can do bulk task assignment
     */
    fun canBulkAssignTasks(departmentId: Long): Boolean {
        return canAssignTasks(departmentId)
    }

    // ==================== PERMISSION CHECKS ====================

    /**
     * Can view all teachers' schedules in department
     */
    fun canViewAllSchedules(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can view department-wide analytics
     */
    fun canViewDepartmentAnalytics(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can manage department resources
     */
    fun canManageDepartmentResources(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can export department reports
     */
    fun canExportDepartmentReports(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can view teacher details (of other teachers)
     */
    fun canViewTeacherDetails(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can access HOD dashboard
     */
    fun canAccessHODDashboard(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can assign classes to teachers (future feature)
     */
    fun canAssignClasses(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can view department timetable
     */
    fun canViewDepartmentTimetable(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can approve leave requests (future feature)
     */
    fun canApproveLeaveRequests(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Can manage department meetings
     */
    fun canManageDepartmentMeetings(): Boolean {
        return isCurrentUserHOD()
    }

    // ==================== UI VISIBILITY HELPERS ====================

    /**
     * Should show HOD-specific menu items
     */
    fun shouldShowHODMenuItems(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Should show "All Teachers" tab/section
     */
    fun shouldShowAllTeachersSection(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Should show department analytics section
     */
    fun shouldShowDepartmentAnalytics(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Get appropriate dashboard title based on role
     */
    fun getDashboardTitle(): String {
        return when (getCurrentUserRole()) {
            UserRole.HOD -> "HOD Dashboard"
            UserRole.TEACHER -> "My Dashboard"
        }
    }

    /**
     * Get appropriate welcome message based on role
     */
    fun getWelcomeMessage(userName: String): String {
        return when (getCurrentUserRole()) {
            UserRole.HOD -> "Welcome back, $userName (HOD)"
            UserRole.TEACHER -> "Welcome back, $userName"
        }
    }

    /**
     * Get role badge color resource
     */
    fun getRoleBadgeColor(): Int {
        return when (getCurrentUserRole()) {
            UserRole.HOD -> android.graphics.Color.parseColor("#FF6B35") // Orange for HOD
            UserRole.TEACHER -> android.graphics.Color.parseColor("#4A90E2") // Blue for Teacher
        }
    }

    // ==================== FEATURE FLAGS ====================

    /**
     * Feature flag: Should show advanced features
     */
    fun hasAdvancedFeatures(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Feature flag: Can bulk import data
     */
    fun canBulkImportData(): Boolean {
        return isCurrentUserHOD()
    }

    /**
     * Feature flag: Can generate institutional reports
     */
    fun canGenerateInstitutionalReports(): Boolean {
        return isCurrentUserHOD()
    }

    // ==================== ANALYTICS ====================

    /**
     * Track role-based feature usage
     */
    fun logRoleBasedFeatureUsage(featureName: String) {
        val role = getCurrentUserRole()
        android.util.Log.d("RoleManager", "Feature '$featureName' accessed by role: ${role.name}")
        // Can integrate with Firebase Analytics here
    }
}

