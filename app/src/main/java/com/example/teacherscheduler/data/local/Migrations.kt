package com.example.teacherscheduler.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for role-based department task system
 */
object Migrations {
    
    /**
     * Migration to add department task fields to ToDo table
     * 
     * Adds:
     * - taskType (PERSONAL/DEPARTMENT)
     * - isDepartmentTask flag
     * - assignedBy, assignedByName (HOD info)
     * - assignedTo (teacher or "ALL")
     * - isBulkTask flag
     * - departmentId reference
     * - status (ASSIGNED/IN_PROGRESS/COMPLETED/OVERDUE)
     * - startedAt timestamp
     * - overdueNotificationSent flag
     */
    val MIGRATION_DEPARTMENT_TASKS = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns with default values
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN taskType TEXT NOT NULL DEFAULT 'PERSONAL'
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN isDepartmentTask INTEGER NOT NULL DEFAULT 0
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN assignedBy TEXT NOT NULL DEFAULT ''
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN assignedByName TEXT NOT NULL DEFAULT ''
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN assignedTo TEXT NOT NULL DEFAULT ''
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN isBulkTask INTEGER NOT NULL DEFAULT 0
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN departmentId INTEGER NOT NULL DEFAULT 0
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN status TEXT NOT NULL DEFAULT 'ASSIGNED'
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN startedAt INTEGER
            """)
            
            database.execSQL("""
                ALTER TABLE todos 
                ADD COLUMN overdueNotificationSent INTEGER NOT NULL DEFAULT 0
            """)
            
            // Create indexes for better query performance
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_todos_taskType 
                ON todos(taskType)
            """)
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_todos_assignedTo 
                ON todos(assignedTo)
            """)
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_todos_status 
                ON todos(status)
            """)
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_todos_departmentId 
                ON todos(departmentId)
            """)
        }
    }
    
    /**
     * Get all migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_DEPARTMENT_TASKS
            // Add more migrations here as needed
        )
    }
}
