package com.example.teacherscheduler.di

import android.content.Context
import androidx.room.Room
import com.example.teacherscheduler.data.local.*
import com.example.teacherscheduler.data.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = SQLiteDatabase.getBytes("TeacherScheduler_Secure_2025".toCharArray())
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "teacher_scheduler_database"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideClassDao(database: AppDatabase): ClassDao = database.classDao()

    @Provides
    fun provideMeetingDao(database: AppDatabase): MeetingDao = database.meetingDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideNoticeDao(database: AppDatabase): NoticeDao = database.noticeDao()

    @Provides
    fun provideResourceDao(database: AppDatabase): ResourceDao = database.resourceDao()

    @Provides
    fun provideToDoDao(database: AppDatabase): ToDoDao = database.todoDao()

    @Provides
    @Singleton
    fun provideRepository(@ApplicationContext context: Context): Repository {
        return Repository(context)
    }
}
