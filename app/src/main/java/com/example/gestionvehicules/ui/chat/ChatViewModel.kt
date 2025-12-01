package com.example.gestionvehicules.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionvehicules.data.api.ApiService
import com.example.gestionvehicules.data.api.WebSocketApiService
import com.example.gestionvehicules.data.api.ChatMessage
import com.example.gestionvehicules.data.api.ChatUser
import com.example.gestionvehicules.data.websocket.WebSocketManager
import com.example.gestionvehicules.data.websocket.WebSocketMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val apiService: ApiService,
    private val webSocketApiService: WebSocketApiService,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentChatUser = MutableStateFlow<ChatUser?>(null)
    val currentChatUser: StateFlow<ChatUser?> = _currentChatUser.asStateFlow()
    
    private var currentUserId: Int = -1
    
    init {
        // Listen for WebSocket messages
        viewModelScope.launch {
            webSocketManager.messageFlow.collect { message ->
                handleWebSocketMessage(message)
            }
        }
    }
    
    /**
     * Initialize with current user ID
     */
    fun initialize(userId: Int) {
        currentUserId = userId
        connectToNotifications(userId)
        loadChatUsers(userId)
    }
    
    /**
     * Connect to notifications WebSocket
     */
    private fun connectToNotifications(userId: Int) {
        webSocketManager.connectToNotifications(userId)
    }
    
    /**
     * Load chat users
     */
    fun loadChatUsers(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = webSocketApiService.getChatUsers(userId)
                if (response.isSuccessful) {
                    response.body()?.let { chatUsersResponse ->
                        if (chatUsersResponse.success) {
                            _chatUsers.value = chatUsersResponse.users ?: emptyList()
                        } else {
                            _error.value = chatUsersResponse.error ?: "Erreur lors du chargement des utilisateurs"
                        }
                    }
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load messages with a specific user
     */
    fun loadMessages(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = webSocketApiService.getMessages(currentUserId, userId)
                if (response.isSuccessful) {
                    response.body()?.let { messagesResponse ->
                        if (messagesResponse.success) {
                            _messages.value = messagesResponse.messages ?: emptyList()
                            
                            // Mark messages as read
                            markMessagesAsRead()
                        } else {
                            _error.value = messagesResponse.error ?: "Erreur lors du chargement des messages"
                        }
                    }
                } else {
                    _error.value = "Erreur serveur: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send a message
     */
    fun sendMessage(message: String, recipientId: Int) {
        viewModelScope.launch {
            try {
                // Send via WebSocket for real-time delivery
                val webSocketSuccess = webSocketManager.sendChatMessage(
                    currentUserId,
                    recipientId,
                    message
                )
                
                if (!webSocketSuccess) {
                    // Fallback to HTTP API
                    val response = webSocketApiService.sendMessage(
                        com.example.gestionvehicules.data.api.SendMessageRequest(
                            sender_id = currentUserId,
                            recipient_id = recipientId,
                            message = message
                        )
                    )
                    
                    if (!response.isSuccessful) {
                        _error.value = "Erreur lors de l'envoi du message"
                    }
                }
                
                // Reload messages to get the latest
                loadMessages(recipientId)
                
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'envoi: ${e.message}"
            }
        }
    }
    
    /**
     * Select a chat user
     */
    fun selectChatUser(user: ChatUser) {
        _currentChatUser.value = user
        loadMessages(user.id)
        
        // Connect to chat room
        val chatEndpoint = "ws://135-231-109-208.host.secureserver.net:8000/ws/chat/${currentUserId}_${user.id}/"
        if (!webSocketManager.isConnected(chatEndpoint)) {
            webSocketManager.connect(chatEndpoint, currentUserId)
        }
    }
    
    /**
     * Handle WebSocket messages
     */
    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message.type) {
            "chat_message" -> {
                // Update messages if it's for current chat
                val senderId = message.data["sender_id"] as? Int
                val recipientId = message.data["recipient_id"] as? Int
                
                if ((senderId == currentUserId && recipientId == _currentChatUser.value?.id) ||
                    (senderId == _currentChatUser.value?.id && recipientId == currentUserId)) {
                    
                    // Reload messages to get the latest
                    _currentChatUser.value?.let { loadMessages(it.id) }
                }
            }
            
            "notification" -> {
                // Handle notifications
                val notificationData = message.data["notification"] as? String
                // Parse notification and update UI accordingly
                loadChatUsers(currentUserId) // Refresh unread counts
            }
        }
    }
    
    /**
     * Mark messages as read
     */
    private fun markMessagesAsRead() {
        viewModelScope.launch {
                    _messages.value.forEach { message ->
                        if (!message.is_read && message.recipient_id == currentUserId) {
                            try {
                                webSocketApiService.markMessageRead(
                                    com.example.gestionvehicules.data.api.MarkReadRequest(message.id)
                                )
                            } catch (e: Exception) {
                                // Ignore errors for marking as read
                            }
                        }
                    }
                }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Disconnect WebSocket connections
        webSocketManager.disconnectAll()
    }
}
