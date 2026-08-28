package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AbortReason
import com.example.domain.model.ActiveSessionDraft
import com.example.domain.model.DraftStatus
import com.example.domain.model.SessionRecord

@Entity(tableName = "session_records")
data class SessionRecordEntity(
    @PrimaryKey val sessionId: String,
    val schemaVersion: Int = 1,
    val startedAt: String,
    val endedAt: String,
    val activeDurationMs: Long,
    val wallClockDurationMs: Long,
    val inhaleMs: Long,
    val exhaleMs: Long,
    val holdMs: Long,
    val comfortBefore: Int?,
    val comfortAfter: Int?,
    val completed: Boolean,
    val abortReason: String?,
    val createdAt: String
) {
    fun toDomain(): SessionRecord = SessionRecord(
        schemaVersion = schemaVersion,
        sessionId = sessionId,
        startedAt = startedAt,
        endedAt = endedAt,
        activeDurationMs = activeDurationMs,
        wallClockDurationMs = wallClockDurationMs,
        inhaleMs = inhaleMs,
        exhaleMs = exhaleMs,
        holdMs = holdMs,
        comfortBefore = comfortBefore,
        comfortAfter = comfortAfter,
        completed = completed,
        abortReason = abortReason?.let { runCatching { AbortReason.valueOf(it) }.getOrNull() },
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(record: SessionRecord): SessionRecordEntity = SessionRecordEntity(
            sessionId = record.sessionId,
            schemaVersion = record.schemaVersion,
            startedAt = record.startedAt,
            endedAt = record.endedAt,
            activeDurationMs = record.activeDurationMs,
            wallClockDurationMs = record.wallClockDurationMs,
            inhaleMs = record.inhaleMs,
            exhaleMs = record.exhaleMs,
            holdMs = record.holdMs,
            comfortBefore = record.comfortBefore,
            comfortAfter = record.comfortAfter,
            completed = record.completed,
            abortReason = record.abortReason?.name,
            createdAt = record.createdAt
        )
    }
}

@Entity(tableName = "active_drafts")
data class ActiveDraftEntity(
    @PrimaryKey val sessionId: String,
    val status: String,
    val startedAt: String,
    val completedAt: String?,
    val activeElapsedMs: Long,
    val comfortBefore: Int?,
    val lastPersistedAt: String
) {
    fun toDomain(): ActiveSessionDraft = ActiveSessionDraft(
        sessionId = sessionId,
        status = if (status == DraftStatus.completed_awaiting_feedback.name) DraftStatus.completed_awaiting_feedback else DraftStatus.active,
        startedAt = startedAt,
        completedAt = completedAt,
        activeElapsedMs = activeElapsedMs,
        comfortBefore = comfortBefore,
        lastPersistedAt = lastPersistedAt
    )

    companion object {
        fun fromDomain(draft: ActiveSessionDraft): ActiveDraftEntity = ActiveDraftEntity(
            sessionId = draft.sessionId,
            status = draft.status.name,
            startedAt = draft.startedAt,
            completedAt = draft.completedAt,
            activeElapsedMs = draft.activeElapsedMs,
            comfortBefore = draft.comfortBefore,
            lastPersistedAt = draft.lastPersistedAt
        )
    }
}
