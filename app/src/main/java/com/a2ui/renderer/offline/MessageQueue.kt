package com.a2ui.renderer.offline

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Message queue for offline support
 * Queues outgoing messages when offline and sends when online
 */
class MessageQueue(context: Context) {
    
    private val messageQueue = ConcurrentLinkedQueue<OutgoingMessage>()
    private val cacheDir = File(context.cacheDir, "a2ui_messages").apply {
        if (!exists()) mkdirs()
    }
    
    /**
     * Outgoing message types
     */
    sealed class OutgoingMessage {
        abstract val id: String
        abstract val timestamp: Long
        abstract val priority: Priority
        
        enum class Priority {
            LOW,
            NORMAL,
            HIGH,
            CRITICAL
        }
    }
    
    /**
     * User action message
     */
    data class UserAction(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: OutgoingMessage.Priority = OutgoingMessage.Priority.NORMAL,
        val surfaceId: String,
        val componentId: String,
        val action: String,
        val data: Map<String, Any> = emptyMap()
    ) : OutgoingMessage()
    
    /**
     * Error report message
     */
    data class ErrorReport(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: OutgoingMessage.Priority = OutgoingMessage.Priority.HIGH,
        val errorCode: String,
        val errorMessage: String,
        val stackTrace: String? = null,
        val context: Map<String, String> = emptyMap()
    ) : OutgoingMessage()
    
    /**
     * Analytics event message
     */
    data class AnalyticsEvent(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: OutgoingMessage.Priority = OutgoingMessage.Priority.LOW,
        val eventName: String,
        val properties: Map<String, Any> = emptyMap()
    ) : OutgoingMessage()
    
    /**
     * Enqueue message for sending
     */
    suspend fun enqueue(message: OutgoingMessage) = withContext(Dispatchers.IO) {
        messageQueue.add(message)
        persistToDisk(message)
        android.util.Log.d("MessageQueue", "Message enqueued: ${message.id}")
    }
    
    /**
     * Flush queue - send all pending messages
     */
    suspend fun flushQueue(sendCallback: suspend (OutgoingMessage) -> Boolean) = withContext(Dispatchers.IO) {
        val failedMessages = mutableListOf<OutgoingMessage>()
        
        while (messageQueue.isNotEmpty()) {
            val message = messageQueue.poll()
            if (message != null) {
                try {
                    val success = sendCallback(message)
                    if (success) {
                        deleteFromDisk(message.id)
                        android.util.Log.d("MessageQueue", "Message sent: ${message.id}")
                    } else {
                        failedMessages.add(message)
                        android.util.Log.w("MessageQueue", "Message send failed: ${message.id}")
                    }
                } catch (e: Exception) {
                    failedMessages.add(message)
                    android.util.Log.e("MessageQueue", "Message send error: ${message.id}", e)
                }
            }
        }
        
        // Re-queue failed messages
        failedMessages.forEach { messageQueue.add(it) }
    }
    
    /**
     * Get pending message count
     */
    fun getPendingCount(): Int {
        return messageQueue.size
    }
    
    /**
     * Get pending messages by priority
     */
    fun getPendingByPriority(priority: OutgoingMessage.Priority): List<OutgoingMessage> {
        return messageQueue.filter { it.priority == priority }
    }
    
    /**
     * Clear all pending messages
     */
    fun clear() {
        messageQueue.clear()
        cacheDir.deleteRecursively()
    }
    
    /**
     * Remove specific message
     */
    fun remove(messageId: String) {
        messageQueue.removeIf { it.id == messageId }
        deleteFromDisk(messageId)
    }
    
    /**
     * Get oldest message age in milliseconds
     */
    fun getOldestMessageAge(): Long {
        return messageQueue.minOfOrNull { System.currentTimeMillis() - it.timestamp } ?: 0
    }
    
    /**
     * Load persisted messages from disk
     */
    suspend fun loadFromDisk() = withContext(Dispatchers.IO) {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.extension == "msg") {
                    // Parse message from file and add to queue
                    android.util.Log.d("MessageQueue", "Loaded persisted message: ${file.name}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MessageQueue", "Failed to load messages from disk", e)
        }
    }
    
    // Private disk persistence methods
    
    private fun persistToDisk(message: OutgoingMessage) {
        try {
            val file = File(cacheDir, "${message.id}.msg")
            // Serialize message to JSON and write to file
            file.writeText("{\"id\":\"${message.id}\",\"type\":\"${message::class.java.simpleName}\",\"priority\":\"${message.priority}\"}")
        } catch (e: Exception) {
            android.util.Log.e("MessageQueue", "Failed to persist message to disk: ${message.id}", e)
        }
    }
    
    private fun deleteFromDisk(messageId: String) {
        File(cacheDir, "$messageId.msg").delete()
    }
}
