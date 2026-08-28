package com.example.data.repository

import com.example.data.local.dao.SessionDao
import com.example.data.local.entity.ActiveDraftEntity
import com.example.data.local.entity.SessionRecordEntity
import com.example.domain.model.ActiveSessionDraft
import com.example.domain.model.SessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(private val sessionDao: SessionDao) {

    val allRecords: Flow<List<SessionRecord>> = sessionDao.getAllRecords()
        .map { list -> list.map { it.toDomain() } }

    suspend fun saveRecord(record: SessionRecord) {
        sessionDao.insertOrUpdateRecord(SessionRecordEntity.fromDomain(record))
    }

    suspend fun countCompletedSessions(): Int {
        return sessionDao.countCompletedSessions()
    }

    suspend fun countTotalSessions(): Int {
        return sessionDao.countTotalSessions()
    }

    suspend fun deleteAllRecords() {
        sessionDao.deleteAllRecords()
        sessionDao.deleteAllDrafts()
    }

    suspend fun saveDraft(draft: ActiveSessionDraft) {
        sessionDao.saveDraft(ActiveDraftEntity.fromDomain(draft))
    }

    suspend fun getActiveDraft(): ActiveSessionDraft? {
        return sessionDao.getActiveDraft()?.toDomain()
    }

    suspend fun deleteDraft(sessionId: String) {
        sessionDao.deleteDraftById(sessionId)
    }

    suspend fun deleteAllDrafts() {
        sessionDao.deleteAllDrafts()
    }
}
