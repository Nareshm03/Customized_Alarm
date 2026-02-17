package com.example.teacherscheduler.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.teacherscheduler.model.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


@Database(
    entities = [
        Class::class,
        Meeting::class,
        AppSettings::class,
        ToDo::class,
        Department::class,
        DepartmentMember::class,
        DepartmentAnnouncement::class,
        Notice::class,
        NoticeSeenStatus::class,
        Resource::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun meetingDao(): MeetingDao
    abstract fun settingsDao(): SettingsDao
    abstract fun todoDao(): ToDoDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun departmentMemberDao(): DepartmentMemberDao
    abstract fun departmentAnnouncementDao(): DepartmentAnnouncementDao
    abstract fun noticeDao(): NoticeDao
    abstract fun resourceDao(): ResourceDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_PASSPHRASE = "TeacherScheduler_Secure_2025"

        fun getDatabase(context: Context): AppDatabase {
            return getInstance(context)
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SQLiteDatabase.getBytes(DB_PASSPHRASE.toCharArray())
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teacher_scheduler_database"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}