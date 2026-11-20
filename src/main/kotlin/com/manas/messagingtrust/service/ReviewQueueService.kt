package com.manas.messagingtrust.service

import com.manas.messagingtrust.model.ReviewQueueEntry
import com.manas.messagingtrust.model.ReviewStatus
import com.manas.messagingtrust.repository.ReviewQueueRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ReviewQueueService(private val reviewQueueRepository: ReviewQueueRepository) {

    fun pending(): List<ReviewQueueEntry> = reviewQueueRepository.findByStatus(ReviewStatus.PENDING)

    fun decide(entryId: Long, approve: Boolean): ReviewQueueEntry {
        val entry = reviewQueueRepository.findById(entryId).orElseThrow { NoSuchElementException("no review queue entry $entryId") }
        check(entry.status == ReviewStatus.PENDING) { "entry $entryId already decided (${entry.status})" }
        entry.status = if (approve) ReviewStatus.APPROVED else ReviewStatus.DENIED
        entry.decidedAtMillis = Instant.now().toEpochMilli()
        return reviewQueueRepository.save(entry)
    }
}
