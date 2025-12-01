package com.example.gestionvehicules.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for WebSocket and real-time communication
 */
interface WebSocketApiService {
    
    @POST("ws/api/send-message/")
    suspend fun sendMessage(
        @Body messageRequest: SendMessageRequest
    ): Response<ApiResponse>
    
    @GET("ws/api/get-messages/")
    suspend fun getMessages(
        @Query("user1_id") user1Id: Int,
        @Query("user2_id") user2Id: Int
    ): Response<MessagesResponse>
    
    @GET("ws/api/chat-users/")
    suspend fun getChatUsers(
        @Query("user_id") userId: Int
    ): Response<ChatUsersResponse>
    
    @POST("ws/api/mark-read/")
    suspend fun markMessageRead(
        @Body request: MarkReadRequest
    ): Response<ApiResponse>
    
    @POST("ws/api/course-status/")
    suspend fun updateCourseStatus(
        @Body request: CourseStatusRequest
    ): Response<CourseStatusResponse>
}

/**
 * Request data classes
 */
data class SendMessageRequest(
    val sender_id: Int,
    val recipient_id: Int,
    val message: String
)

data class MarkReadRequest(
    val message_id: Int
)

data class CourseStatusRequest(
    val course_id: Int,
    val status: String,
    val user_id: Int
)

/**
 * Response data classes
 */
data class MessagesResponse(
    val success: Boolean,
    val messages: List<ChatMessage>? = null,
    val error: String? = null
)

data class ChatUsersResponse(
    val success: Boolean,
    val users: List<ChatUser>? = null,
    val error: String? = null
)

data class CourseStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val old_status: String? = null,
    val new_status: String? = null,
    val error: String? = null
)

data class ChatMessage(
    val id: Int,
    val sender_id: Int,
    val sender_name: String,
    val recipient_id: Int,
    val content: String,
    val timestamp: String,
    val is_read: Boolean
)

data class ChatUser(
    val id: Int,
    val name: String,
    val role: String,
    val unread_count: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
