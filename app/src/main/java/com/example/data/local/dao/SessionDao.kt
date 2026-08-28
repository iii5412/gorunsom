package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ActiveDraftEntity
import com.example.data.local.entity.SessionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: SessionRecordEntity)

    @Query("SELECT * FROM session_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<SessionRecordEntity>>

    @Query("SELECT COUNT(*) FROM session_records WHERE completed = 1")
    suspend fun countCompletedSessions(): Int

    @Query("SELECT COUNT(*) FROM session_records")
    suspend fun countTotalSessions(): Int

    @Query("DELETE FROM session_records")
    suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ActiveDraftEntity)

    @Query("SELECT * FROM active_drafts LIMIT 1")
    suspend fun getActiveDraft(): ActiveDraftEntity?

    @Query("DELETE FROM active_drafts WHERE sessionId = :sessionId")
    suspend fun deleteDraftById(sessionId: String)

    @Query("DELETE FROM active_drafts")
    suspend fun deleteAllDrafts()
}
