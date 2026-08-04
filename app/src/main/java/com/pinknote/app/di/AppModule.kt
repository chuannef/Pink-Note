package com.pinknote.app.di

import android.content.Context
import androidx.room.Room
import com.pinknote.app.data.local.PinkNoteDatabase
import com.pinknote.app.data.local.RoomMigrations
import com.pinknote.app.data.repository.AuthRepositoryImpl
import com.pinknote.app.data.repository.CycleRepositoryImpl
import com.pinknote.app.data.repository.PregnancyRepositoryImpl
import com.pinknote.app.data.repository.ReminderRepositoryImpl
import com.pinknote.app.data.repository.SettingsRepositoryImpl
import com.pinknote.app.data.repository.UserRepositoryImpl
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.domain.repository.CycleRepository
import com.pinknote.app.domain.repository.PregnancyRepository
import com.pinknote.app.domain.repository.ReminderRepository
import com.pinknote.app.domain.repository.SettingsRepository
import com.pinknote.app.domain.repository.UserRepository
import com.pinknote.app.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PinkNoteDatabase {
        return Room.databaseBuilder(context, PinkNoteDatabase::class.java, Constants.DATABASE_NAME)
            .addMigrations(*RoomMigrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides fun provideUserDao(database: PinkNoteDatabase) = database.userDao()
    @Provides fun provideCycleDao(database: PinkNoteDatabase) = database.cycleDao()
    @Provides fun provideDailyLogDao(database: PinkNoteDatabase) = database.dailyLogDao()
    @Provides fun providePregnancyDao(database: PinkNoteDatabase) = database.pregnancyDao()
    @Provides fun provideReminderDao(database: PinkNoteDatabase) = database.reminderDao()

    @Provides @Singleton fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindCycleRepository(impl: CycleRepositoryImpl): CycleRepository
    @Binds @Singleton abstract fun bindPregnancyRepository(impl: PregnancyRepositoryImpl): PregnancyRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
