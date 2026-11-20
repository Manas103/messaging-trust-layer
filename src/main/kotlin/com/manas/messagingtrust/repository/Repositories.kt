package com.manas.messagingtrust.repository

import com.manas.messagingtrust.model.MessageRecord
import com.manas.messagingtrust.model.ReviewQueueEntry
import com.manas.messagingtrust.model.ReviewStatus
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRecordRepository : JpaRepository<MessageRecord, String>

interface ReviewQueueRepository : JpaRepository<ReviewQueueEntry, Long> {
    fun findByStatus(status: ReviewStatus): List<ReviewQueueEntry>
    fun findByMessageId(messageId: String): ReviewQueueEntry?
}
