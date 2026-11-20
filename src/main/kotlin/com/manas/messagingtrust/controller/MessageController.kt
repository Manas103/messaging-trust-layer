package com.manas.messagingtrust.controller

import com.manas.messagingtrust.model.Message
import com.manas.messagingtrust.model.MessageLocale
import com.manas.messagingtrust.model.MessageRecord
import com.manas.messagingtrust.service.MessageWorkflowService
import org.springframework.web.bind.annotation.*

data class SubmitMessageRequest(val id: String, val text: String, val locale: MessageLocale, val fromHost: Boolean)

@RestController
@RequestMapping("/messages")
class MessageController(private val workflowService: MessageWorkflowService) {

    @PostMapping
    fun submit(@RequestBody request: SubmitMessageRequest): MessageRecord {
        return workflowService.submit(Message(request.id, request.text, request.locale, request.fromHost))
    }
}
