package com.manas.messagingtrust.controller

import com.manas.messagingtrust.model.ReviewQueueEntry
import com.manas.messagingtrust.service.ReviewQueueService
import org.springframework.web.bind.annotation.*

data class ReviewDecisionRequest(val approve: Boolean)

/** The REST backend the React review queue polls and posts decisions to. */
@RestController
@RequestMapping("/review-queue")
class ReviewQueueController(private val reviewQueueService: ReviewQueueService) {

    @GetMapping
    fun pending(): List<ReviewQueueEntry> = reviewQueueService.pending()

    @PostMapping("/{id}/decision")
    fun decide(@PathVariable id: Long, @RequestBody request: ReviewDecisionRequest): ReviewQueueEntry =
        reviewQueueService.decide(id, request.approve)
}
