package com.example.data.repository

import com.example.data.local.ProfileDao
import com.example.data.local.ProgressDao
import com.example.data.model.Profile
import com.example.data.model.ProgressLog
import kotlinx.coroutines.flow.Flow

class MathQuestRepository(
    private val profileDao: ProfileDao,
    private val progressDao: ProgressDao
) {
    val allProfilesFlow: Flow<List<Profile>> = profileDao.getAllProfilesFlow()

    suspend fun getActiveProfile(): Profile? = profileDao.getActiveProfile()

    suspend fun getProfileById(id: Int): Profile? = profileDao.getProfileById(id)

    suspend fun insertProfile(profile: Profile): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: Profile) {
        // First delete logs of this profile for clean database management
        progressDao.deleteLogsForProfile(profile.id)
        profileDao.deleteProfile(profile)
    }

    suspend fun selectActiveProfile(id: Int) {
        profileDao.deactivateAllProfiles()
        profileDao.activateProfile(id)
    }

    suspend fun deactivateAll() {
        profileDao.deactivateAllProfiles()
    }

    fun getProgressLogsFlow(profileId: Int): Flow<List<ProgressLog>> =
        progressDao.getLogsForProfileFlow(profileId)

    suspend fun getProgressLogs(profileId: Int): List<ProgressLog> =
        progressDao.getLogsForProfile(profileId)

    suspend fun insertProgressLog(log: ProgressLog): Long =
        progressDao.insertLog(log)
}
