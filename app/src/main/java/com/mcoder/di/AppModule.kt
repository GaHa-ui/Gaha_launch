package com.mcoder.di

import android.content.Context
import androidx.room.Room
import com.mcoder.data.db.AppDatabase
import com.mcoder.data.db.ChatDao
import com.mcoder.data.db.ProotDao
import com.mcoder.data.db.ServerProfileDao
import com.mcoder.data.db.SettingsDao
import com.mcoder.data.repo.ChatRepositoryImpl
import com.mcoder.data.repo.FileRepositoryImpl
import com.mcoder.data.repo.GatewayRepositoryImpl
import com.mcoder.data.repo.ProotRepositoryImpl
import com.mcoder.data.repo.ServerProfileRepositoryImpl
import com.mcoder.data.repo.SettingsRepositoryImpl
import com.mcoder.domain.ChatRepository
import com.mcoder.domain.FileRepository
import com.mcoder.domain.GatewayRepository
import com.mcoder.domain.ProotRepository
import com.mcoder.domain.ServerProfileRepository
import com.mcoder.domain.SettingsRepository
import com.mcoder.domain.usecase.ChatUseCases
import com.mcoder.domain.usecase.FileUseCases
import com.mcoder.domain.usecase.GatewayUseCases
import com.mcoder.domain.usecase.ProotUseCases
import com.mcoder.domain.usecase.ServerProfileUseCases
import com.mcoder.domain.usecase.SettingsUseCases
import com.mcoder.util.NotificationHelper
import com.mcoder.util.AssetInstaller
import com.mcoder.util.SecureStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "mcoder.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideProotDao(database: AppDatabase): ProotDao = database.prootDao()

    @Provides
    fun provideServerProfileDao(database: AppDatabase): ServerProfileDao = database.serverProfileDao()

    @Provides
    fun provideChatUseCases(repository: ChatRepository): ChatUseCases = ChatUseCases(repository)

    @Provides
    fun provideSettingsUseCases(repository: SettingsRepository): SettingsUseCases = SettingsUseCases(repository)

    @Provides
    fun provideFileUseCases(repository: FileRepository): FileUseCases = FileUseCases(repository)

    @Provides
    fun provideGatewayUseCases(repository: GatewayRepository): GatewayUseCases = GatewayUseCases(repository)

    @Provides
    fun provideProotUseCases(repository: ProotRepository): ProotUseCases = ProotUseCases(repository)

    @Provides
    fun provideServerProfileUseCases(repository: ServerProfileRepository): ServerProfileUseCases =
        ServerProfileUseCases(repository)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideAssetInstaller(@ApplicationContext context: Context): AssetInstaller {
        return AssetInstaller(context)
    }

    @Provides
    @Singleton
    fun provideSecureStore(@ApplicationContext context: Context): SecureStore {
        return SecureStore(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    abstract fun bindGatewayRepository(impl: GatewayRepositoryImpl): GatewayRepository

    @Binds
    abstract fun bindProotRepository(impl: ProotRepositoryImpl): ProotRepository

    @Binds
    abstract fun bindServerProfileRepository(impl: ServerProfileRepositoryImpl): ServerProfileRepository
}
