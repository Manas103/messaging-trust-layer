package com.manas.messagingtrust.service

import com.manas.messagingtrust.model.Message
import com.manas.messagingtrust.model.MessageRecord
import com.manas.messagingtrust.model.ReviewQueueEntry
import com.manas.messagingtrust.repository.MessageRecordRepository
import com.manas.messagingtrust.repository.ReviewQueueRepository
import org.springframework.stereotype.Service

/**
 * Screens a message, persists its two stored forms (see {@code MessageRecord}'s javadoc), and,
 * only if it is CHALLENGED, opens a review-queue entry that holds it pending a human decision.
 * Every other message is implicitly delivered (this project does not model a separate delivery
 * side-effect; "delivered" here means "not held").
 */
@Service
class MessageWorkflowService(
    private val screeningService: MessageScreeningService,
    private val messageRecordRepository: MessageRecordRepository,
    private val reviewQueueRepository: ReviewQueueRepository
) {
    fun submit(message: Message): MessageRecord {
        val result = screeningService.screen(message)
        val record = MessageRecord(
            id = message.id,
            liveText = message.text,
            redactedText = result.redactedText,
            locale = message.locale,
            fromHost = message.fromHost,
            riskBand = result.riskBand,
            classifierScore = result.classifierScore,
            challenged = result.challenged
        )
        messageRecordRepository.save(record)
        if (result.challenged) {
            reviewQueueRepository.save(ReviewQueueEntry(messageId = message.id))
        }
        return record
    }
}
