package com.example.teacherscheduler.data

import android.content.Context
import androidx.lifecycle.asFlow
import com.example.teacherscheduler.data.local.*
import com.example.teacherscheduler.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * DepartmentManager - Central management for department operations
 *
 * Handles:
 * - Department CRUD operations
 * - Member management
 * - Task assignment (including bulk)
 * - Announcements
 * - Department analytics
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class DepartmentManager(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val departmentDao = database.departmentDao()
    private val memberDao = database.departmentMemberDao()
    private val announcementDao = database.departmentAnnouncementDao()
    private val todoDao = database.todoDao()

    // ==================== DEPARTMENT OPERATIONS ====================

    /**
     * Create a new department
     */
    suspend fun createDepartment(
        code: String,
        name: String,
        description: String,
        hodId: String,
        hodName: String,
        hodEmail: String,
        building: String = "",
        floor: String = "",
        officeRoom: String = "",
        phoneExtension: String = "",
        email: String = ""
    ): Long {
        val department = Department(
            departmentCode = code,
            departmentName = name,
            description = description,
            hodId = hodId,
            hodName = hodName,
            hodEmail = hodEmail,
            building = building,
            floor = floor,
            officeRoom = officeRoom,
            phoneExtension = phoneExtension,
            email = email
        )

        val departmentId = departmentDao.insert(department)

        // Add HOD as member
        addMemberToDepartment(
            departmentId = departmentId,
            userId = hodId,
            userName = hodName,
            userEmail = hodEmail,
            role = UserRole.HOD
        )

        return departmentId
    }

    /**
     * Get department by ID
     */
    suspend fun getDepartment(departmentId: Long): Department? {
        return departmentDao.getDepartmentById(departmentId)
    }

    /**
     * Get department by code
     */
    suspend fun getDepartmentByCode(code: String): Department? {
        return departmentDao.getDepartmentByCode(code)
    }

    /**
     * Get department where user is HOD
     */
    suspend fun getDepartmentByHOD(hodId: String): Department? {
        return departmentDao.getDepartmentByHOD(hodId)
    }

    /**
     * Get all active departments
     */
    fun getAllDepartments(): Flow<List<Department>> {
        return departmentDao.getAllActiveDepartments()
    }

    /**
     * Update department information
     */
    suspend fun updateDepartment(department: Department) {
        val updated = department.copy(updatedAt = System.currentTimeMillis())
        departmentDao.update(updated)
    }

    /**
     * Change HOD of department
     */
    suspend fun changeHOD(
        departmentId: Long,
        newHodId: String,
        newHodName: String,
        newHodEmail: String
    ) {
        // Update department
        departmentDao.updateHOD(departmentId, newHodId, newHodName, newHodEmail)

        // Update old HOD role to TEACHER
        val oldHod = memberDao.getHOD(departmentId)
        oldHod?.let {
            memberDao.updateRole(it.id, UserRole.TEACHER.name)
        }

        // Update new HOD role
        val newHodMember = memberDao.getMembership(departmentId, newHodId)
        if (newHodMember != null) {
            memberDao.updateRole(newHodMember.id, UserRole.HOD.name)
        } else {
            // Add new HOD as member if not exists
            addMemberToDepartment(
                departmentId = departmentId,
                userId = newHodId,
                userName = newHodName,
                userEmail = newHodEmail,
                role = UserRole.HOD
            )
        }
    }

    /**
     * Delete department
     */
    suspend fun deleteDepartment(departmentId: Long) {
        departmentDao.deleteById(departmentId)
        // Members, tasks, and announcements will be cascade deleted
    }

    // ==================== MEMBER OPERATIONS ====================

    /**
     * Add member to department
     */
    suspend fun addMemberToDepartment(
        departmentId: Long,
        userId: String,
        userName: String,
        userEmail: String,
        role: UserRole = UserRole.TEACHER
    ): Long {
        val member = DepartmentMember(
            departmentId = departmentId,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            role = role.name
        )

        val memberId = memberDao.insert(member)

        // Update department teacher count if teacher
        if (role == UserRole.TEACHER) {
            updateDepartmentTeacherCount(departmentId)
        }

        return memberId
    }

    /**
     * Remove member from department
     */
    suspend fun removeMemberFromDepartment(departmentId: Long, userId: String) {
        val member = memberDao.getMembership(departmentId, userId)
        member?.let {
            memberDao.updateActiveStatus(it.id, false, System.currentTimeMillis())
            updateDepartmentTeacherCount(departmentId)
        }
    }

    /**
     * Get all members of department
     */
    fun getDepartmentMembers(departmentId: Long): Flow<List<DepartmentMember>> {
        return memberDao.getMembersByDepartment(departmentId)
    }

    /**
     * Get all teachers in department
     */
    fun getDepartmentTeachers(departmentId: Long): Flow<List<DepartmentMember>> {
        return memberDao.getTeachers(departmentId)
    }

    /**
     * Get departments for a user
     */
    fun getUserDepartments(userId: String): Flow<List<DepartmentMember>> {
        return memberDao.getMembersByUser(userId)
    }

    /**
     * Check if user is member of department
     */
    suspend fun isMemberOfDepartment(departmentId: Long, userId: String): Boolean {
        return memberDao.getMembership(departmentId, userId)?.isActive == true
    }

    /**
     * Check if user is HOD of department
     */
    suspend fun isHODOfDepartment(departmentId: Long, userId: String): Boolean {
        val member = memberDao.getMembership(departmentId, userId)
        return member?.isActive == true && member.role == UserRole.HOD.name
    }

    /**
     * Update department teacher count
     */
    private suspend fun updateDepartmentTeacherCount(departmentId: Long) {
        val count = memberDao.getTeacherCount(departmentId)
        departmentDao.updateTeacherCount(departmentId, count)
    }

    // ==================== ANNOUNCEMENT OPERATIONS ====================

    /**
     * Create announcement
     */
    suspend fun createAnnouncement(
        departmentId: Long,
        title: String,
        message: String,
        priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
        createdBy: String,
        createdByName: String,
        isPinned: Boolean = false,
        expiresAt: Long? = null
    ): Long {
        val totalMembers = memberDao.getActiveMemberCount(departmentId)

        val announcement = DepartmentAnnouncement(
            departmentId = departmentId,
            title = title,
            message = message,
            priority = priority,
            createdBy = createdBy,
            createdByName = createdByName,
            isPinned = isPinned,
            expiresAt = expiresAt,
            totalMembers = totalMembers
        )

        return announcementDao.insert(announcement)
    }

    /**
     * Get announcements for department
     */
    fun getDepartmentAnnouncements(departmentId: Long): Flow<List<DepartmentAnnouncement>> {
        return announcementDao.getActiveAnnouncements(departmentId).asFlow()
    }

    /**
     * Get announcement by ID
     */
    suspend fun getAnnouncementById(announcementId: Long): DepartmentAnnouncement? {
        return announcementDao.getById(announcementId)
    }

    /**
     * Get pinned announcements
     */
    fun getPinnedAnnouncements(departmentId: Long): Flow<List<DepartmentAnnouncement>> {
        return announcementDao.getPinnedAnnouncements(departmentId).asFlow()
    }

    /**
     * Update announcement
     */
    suspend fun updateAnnouncement(announcement: DepartmentAnnouncement) {
        val updated = announcement.copy(updatedAt = System.currentTimeMillis())
        announcementDao.update(updated)
    }

    /**
     * Delete announcement
     */
    suspend fun deleteAnnouncement(announcementId: Long) {
        val announcement = announcementDao.getById(announcementId)
        announcement?.let {
            announcementDao.delete(it)
        }
    }

    /**
     * Pin/Unpin announcement
     */
    suspend fun toggleAnnouncementPin(announcementId: Long, isPinned: Boolean) {
        val announcement = announcementDao.getById(announcementId)
        announcement?.let {
            announcementDao.update(it.copy(isPinned = isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Assign task to single teacher
     */
    suspend fun assignTask(
        departmentId: Long,
        title: String,
        description: String,
        category: String = "General",
        priority: ToDo.Priority = ToDo.Priority.MEDIUM,
        assignedTo: String,
        assignedToName: String,
        assignedBy: String,
        assignedByName: String,
        dueDate: Date?
    ): Long {
        val task = ToDo(
            title = title,
            description = description,
            category = category,
            priority = priority,
            dueDate = dueDate,
            isDepartmentTask = true,
            departmentId = departmentId,
            assignedTo = assignedTo,
            assignedBy = assignedBy,
            assignedByName = assignedByName,
            taskType = TaskType.DEPARTMENT,
            isBulkTask = false,
            status = TaskStatus.ASSIGNED
        )

        return todoDao.insert(task)
    }

    /**
     * Bulk assign task to all teachers in department
     */
    suspend fun bulkAssignTask(
        departmentId: Long,
        title: String,
        description: String,
        category: String = "General",
        priority: ToDo.Priority = ToDo.Priority.MEDIUM,
        assignedBy: String,
        assignedByName: String,
        dueDate: Date?
    ): Long {
        val task = ToDo(
            title = title,
            description = description,
            category = category,
            priority = priority,
            dueDate = dueDate,
            isDepartmentTask = true,
            departmentId = departmentId,
            assignedTo = "ALL",
            assignedBy = assignedBy,
            assignedByName = assignedByName,
            taskType = TaskType.DEPARTMENT,
            isBulkTask = true,
            status = TaskStatus.ASSIGNED
        )

        return todoDao.insert(task)
    }

    /**
     * Get tasks for user
     */
    fun getUserTasks(userId: String): Flow<List<ToDo>> {
        return todoDao.getDepartmentTasksForUser(userId)
    }

    /**
     * Get tasks for department
     */
    fun getDepartmentTasks(departmentId: Long): Flow<List<ToDo>> {
        return todoDao.getDepartmentTasks(departmentId)
    }

    /**
     * Get overdue tasks for user
     */
    fun getOverdueTasks(userId: String): Flow<List<ToDo>> {
        return todoDao.getOverdueDepartmentTasks(userId, System.currentTimeMillis())
    }

    /**
     * Complete task with notes
     */
    suspend fun completeTask(taskId: Long, notes: String = "") {
        todoDao.completeTask(taskId, System.currentTimeMillis(), notes)
    }

    /**
     * Start task (transition to IN_PROGRESS)
     */
    suspend fun startTask(taskId: Long) {
        todoDao.startTask(taskId, System.currentTimeMillis())
    }

    /**
     * Update task status
     */
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus) {
        todoDao.updateTaskStatus(taskId, status)
    }

    /**
     * Delete task
     */
    suspend fun deleteTask(taskId: Long) {
        todoDao.deleteById(taskId)
    }

    // ==================== ANALYTICS ====================

    /**
     * Get department statistics
     */
    suspend fun getDepartmentStatistics(departmentId: Long): DepartmentStatistics {
        val department = getDepartment(departmentId) ?: return DepartmentStatistics()
        val memberCount = memberDao.getActiveMemberCount(departmentId)
        val teacherCount = memberDao.getTeacherCount(departmentId)

        // Announcements are Flow<List<DepartmentAnnouncement>>, need to get first
        val activeAnnouncements = getDepartmentAnnouncements(departmentId).first()
        val announcementCount = activeAnnouncements.size

        // Count department tasks
        val allDepartmentTasks = todoDao.getDepartmentTasks(departmentId).first()
        val taskCount = allDepartmentTasks.size
        val bulkTaskCount = allDepartmentTasks.count { it.isBulkTask }

        return DepartmentStatistics(
            departmentId = departmentId,
            departmentName = department.departmentName,
            totalMembers = memberCount,
            totalTeachers = teacherCount,
            totalAnnouncements = announcementCount,
            totalTasks = taskCount,
            totalBulkTasks = bulkTaskCount,
            totalClasses = department.totalClasses
        )
    }

    /**
     * Get user task statistics
     */
    suspend fun getUserTaskStatistics(userId: String): UserTaskStatistics {
        val pendingCount = todoDao.countDepartmentTasks(userId, false)
        val completedCount = todoDao.countDepartmentTasks(userId, true)
        val overdueTasks = todoDao.getOverdueDepartmentTasks(userId, System.currentTimeMillis()).first()
        val overdueCount = overdueTasks.size

        return UserTaskStatistics(
            userId = userId,
            pendingTasks = pendingCount,
            inProgressTasks = 0,
            completedTasks = completedCount,
            overdueTasks = overdueCount
        )
    }
}

/**
 * Department statistics data class
 */
data class DepartmentStatistics(
    val departmentId: Long = 0,
    val departmentName: String = "",
    val totalMembers: Int = 0,
    val totalTeachers: Int = 0,
    val totalAnnouncements: Int = 0,
    val totalTasks: Int = 0,
    val totalBulkTasks: Int = 0,
    val totalClasses: Int = 0
)

/**
 * User task statistics data class
 */
data class UserTaskStatistics(
    val userId: String = "",
    val pendingTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0
)

