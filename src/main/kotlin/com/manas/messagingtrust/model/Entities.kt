package com.manas.messagingtrust.model

import jakarta.persistence.*
import java.time.Instant

enum class ReviewStatus { PENDING, APPROVED, DENIED }

/**
 * One message's two stored forms: {@code liveText}, what the host/guest actually sent (delivered
 * to the other party unless a human reviewer denies it), and {@code redactedText}, the copy any
 * downstream training pipeline is ever allowed to read. Redaction happens before this row is
 * written, not as a later batch job: there is no code path that inserts a row with the live text
 * already present in {@code redactedText}.
 */
@Entity
@Table(name = "message_record")
class MessageRecord(
    @Id
    val id: String,
    @Column(nullable = false, length = 4000)
    val liveText: String,
    @Column(nullable = false, length = 4000)
    val redactedText: String,
    @Enumerated(EnumType.STRING)
    val locale: MessageLocale,
    val fromHost: Boolean,
    @Enumerated(EnumType.STRING)
    val riskBand: RiskBand,
    val classifierScore: Double,
    val challenged: Boolean,
    val createdAtMillis: Long = Instant.now().toEpochMilli()
)

/** Created only for a CHALLENGED (top-band) message; every row here is held until a human decides. */
@Entity
@Table(name = "review_queue_entry")
class ReviewQueueEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val messageId: String,
    @Enumerated(EnumType.STRING)
    var status: ReviewStatus = ReviewStatus.PENDING,
    val createdAtMillis: Long = Instant.now().toEpochMilli(),
    var decidedAtMillis: Long? = null
)
