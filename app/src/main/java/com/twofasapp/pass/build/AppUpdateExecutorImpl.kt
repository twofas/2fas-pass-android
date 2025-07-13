package com.twofasapp.pass.build

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.AppUpdateExecutor
import com.twofasapp.core.common.build.AppUpdateResult
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.pass.storage.migrations.data.MigrateLoginsToItems
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppUpdateExecutorImpl(
    private val dispatchers: Dispatchers,
    private val appBuild: AppBuild,
    private val sessionRepository: SessionRepository,
    private val migrateLoginsToItems: MigrateLoginsToItems,
) : AppUpdateExecutor {

    companion object {
        private const val Tag = "AppUpdateExecutor"
    }

    override suspend fun execute(): AppUpdateResult {
        return withContext(dispatchers.io) {
            if (appBuild.versionCode == sessionRepository.getAppVersionCode()) {
                Timber.tag(Tag).d("App state is up to date")
                return@withContext AppUpdateResult.Completed
            }

            Timber.tag(Tag).d("Start app update: ${sessionRepository.getAppVersionCode()} -> ${appBuild.versionCode}")

            try {
                runMigrations()
            } catch (e: Exception) {
                Timber.tag(Tag).d("App update failed: ${e.message}")
                return@withContext AppUpdateResult.Failed(e)
            }

            Timber.tag(Tag).d("App update completed with success")
            sessionRepository.setAppVersionCode(appBuild.versionCode)
            return@withContext AppUpdateResult.Completed
        }
    }

    private suspend fun runMigrations() {
        migrateLoginsToItems.execute()
    }
}