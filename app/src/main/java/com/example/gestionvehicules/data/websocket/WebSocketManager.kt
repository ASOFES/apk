package com.example.gestionvehicules.data.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket client for real-time communication
 */
class WebSocketManager private constructor() {
    
    private val webSocketConnections = ConcurrentHashMap<String, OkHttpWebSocketClient>()
    private val _messageFlow = MutableSharedFlow<WebSocketMessage>()
    val messageFlow: SharedFlow<WebSocketMessage> = _messageFlow
    
    companion object {
        @Volatile
        private var INSTANCE: WebSocketManager? = null
        
        fun getInstance(): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Connect to a WebSocket endpoint
     */
    fun connect(endpoint: String, userId: Int? = null): Boolean {
        return try {
            val client = OkHttpWebSocketClient(endpoint, _messageFlow, userId)
            webSocketConnections[endpoint] = client
            client.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Disconnect from a WebSocket endpoint
     */
    fun disconnect(endpoint: String) {
        webSocketConnections[endpoint]?.disconnect()
        webSocketConnections.remove(endpoint)
    }
    
    /**
     * Send a message through WebSocket
     */
    fun sendMessage(endpoint: String, message: Map<String, Any>): Boolean {
        return try {
            val jsonMessage = JSONObject(message).toString()
            webSocketConnections[endpoint]?.sendMessage(jsonMessage) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Send a chat message
     */
    fun sendChatMessage(senderId: Int, recipientId: Int, message: String): Boolean {
        val chatEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/chat/${senderId}_${recipientId}/"
        
        if (!webSocketConnections.containsKey(chatEndpoint)) {
            connect(chatEndpoint, senderId)
        }
        
        val messageData = mapOf(
            "message" to message,
            "sender_id" to senderId,
            "recipient_id" to recipientId,
            "timestamp" to System.currentTimeMillis()
        )
        
        return sendMessage(chatEndpoint, messageData)
    }
    
    /**
     * Join a course room for real-time updates
     */
    fun joinCourseRoom(courseId: Int, userId: Int): Boolean {
        val courseEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/course/${courseId}/"
        return connect(courseEndpoint, userId)
    }
    
    /**
     * Send location update
     */
    fun sendLocationUpdate(courseId: Int, userId: Int, latitude: Double, longitude: Double): Boolean {
        val courseEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/course/${courseId}/"
        
        val locationData = mapOf(
            "type" to "location_update",
            "latitude" to latitude,
            "longitude" to longitude,
            "user_id" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        
        return sendMessage(courseEndpoint, locationData)
    }
    
    /**
     * Update course status
     */
    fun updateCourseStatus(courseId: Int, userId: Int, newStatus: String): Boolean {
        val courseEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/course/${courseId}/"
        
        val statusData = mapOf(
            "type" to "status_update",
            "status" to newStatus,
            "user_id" to userId,
            "timestamp" to System.currentTimeMillis()
        )
        
        return sendMessage(courseEndpoint, statusData)
    }
    
    /**
     * Connect to notifications
     */
    fun connectToNotifications(userId: Int): Boolean {
        val notificationEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/notifications/"
        return connect(notificationEndpoint, userId)
    }
    
    /**
     * Disconnect all connections
     */
    fun disconnectAll() {
        webSocketConnections.values.forEach { it.disconnect() }
        webSocketConnections.clear()
    }
    
    /**
     * Check if connected to specific endpoint
     */
    fun isConnected(endpoint: String): Boolean {
        return webSocketConnections[endpoint]?.isConnected() ?: false
    }
}

/**
 * WebSocket message data class
 */
data class WebSocketMessage(
    val type: String,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis(),
    val endpoint: String? = null
)
