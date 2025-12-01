package com.example.gestionvehicules.data.websocket

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

/**
 * OkHttp WebSocket client implementation
 */
class OkHttpWebSocketClient(
    private val endpoint: String,
    private val messageFlow: MutableSharedFlow<WebSocketMessage>,
    private val userId: Int? = null
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var isConnected = false
    
    fun connect() {
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .addHeader("User-Agent", "GestionVehicules-Android")
        
        userId?.let { id ->
            requestBuilder.addHeader("X-User-ID", id.toString())
        }
        
        val request = requestBuilder.build()
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
    }
    
    fun sendMessage(message: String): Boolean {
        return try {
            webSocket?.send(message) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun isConnected(): Boolean = isConnected
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                println("WebSocket connected to: $endpoint")
                
                // Send connection message
                userId?.let {
                    val connectMessage = JSONObject().apply {
                        put("type", "connect")
                        put("user_id", it)
                    }.toString()
                    webSocket.send(connectMessage)
                }
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Handle binary messages if needed
                handleMessage(bytes.utf8())
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closing: $code - $reason")
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("WebSocket closed: $code - $reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                println("WebSocket error: ${t.message}")
                
                // Send error message
                try {
                    val errorMessage = WebSocketMessage(
                        type = "error",
                        data = mapOf(
                            "error" to (t.message ?: "Unknown error"),
                            "endpoint" to endpoint
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(errorMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun handleMessage(text: String) {
        try {
            val jsonObject = JSONObject(text)
            val type = jsonObject.optString("type", "unknown")
            
            when (type) {
                "chat_message" -> {
                    val message = WebSocketMessage(
                        type = "chat_message",
                        data = mapOf(
                            "message" to jsonObject.optString("message"),
                            "sender_id" to jsonObject.optInt("sender_id"),
                            "sender_name" to jsonObject.optString("sender_name"),
                            "recipient_id" to jsonObject.optInt("recipient_id"),
                            "timestamp" to jsonObject.optString("timestamp"),
                            "message_id" to jsonObject.optInt("message_id")
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
                
                "location_update" -> {
                    val message = WebSocketMessage(
                        type = "location_update",
                        data = mapOf(
                            "latitude" to jsonObject.optDouble("latitude"),
                            "longitude" to jsonObject.optDouble("longitude"),
                            "user_id" to jsonObject.optInt("user_id"),
                            "timestamp" to jsonObject.optLong("timestamp")
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
                
                "status_update" -> {
                    val message = WebSocketMessage(
                        type = "status_update",
                        data = mapOf(
                            "course_id" to jsonObject.optInt("course_id"),
                            "old_status" to jsonObject.optString("old_status"),
                            "new_status" to jsonObject.optString("new_status"),
                            "updated_by" to jsonObject.optInt("updated_by"),
                            "timestamp" to jsonObject.optString("timestamp")
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
                
                "notification" -> {
                    val notification = jsonObject.optJSONObject("notification")
                    val message = WebSocketMessage(
                        type = "notification",
                        data = mapOf(
                            "notification" to (notification?.toString() ?: "{}")
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
                
                "error" -> {
                    val message = WebSocketMessage(
                        type = "error",
                        data = mapOf(
                            "error" to jsonObject.optString("message")
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
                
                else -> {
                    val message = WebSocketMessage(
                        type = "unknown",
                        data = mapOf(
                            "raw_message" to text
                        ),
                        endpoint = endpoint
                    )
                    messageFlow.tryEmit(message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            
            // Send raw message as fallback
            val message = WebSocketMessage(
                type = "raw",
                data = mapOf(
                    "raw_message" to text
                ),
                endpoint = endpoint
            )
            messageFlow.tryEmit(message)
        }
    }
}
