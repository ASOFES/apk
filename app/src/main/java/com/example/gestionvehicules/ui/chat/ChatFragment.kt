package com.example.gestionvehicules.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionvehicules.R
import com.example.gestionvehicules.databinding.FragmentChatBinding
import com.example.gestionvehicules.ui.adapters.ChatAdapter
import com.example.gestionvehicules.ui.adapters.ChatUserAdapter
import com.example.gestionvehicules.data.api.SessionManager
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ChatViewModel by viewModels()
    
    private lateinit var sessionManager: SessionManager
    
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatUserAdapter: ChatUserAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        
        // Initialize with current user
        sessionManager.currentUser?.let { user ->
            viewModel.initialize(user.id)
        }
    }
    
    private fun setupRecyclerViews() {
        // Chat users RecyclerView
        chatUserAdapter = ChatUserAdapter { user ->
            viewModel.selectChatUser(user)
            binding.chatUsersLayout.visibility = View.GONE
            binding.chatLayout.visibility = View.VISIBLE
        }
        
        binding.recyclerViewChatUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatUserAdapter
        }
        
        // Messages RecyclerView
        chatAdapter = ChatAdapter()
        
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatUsers.collect { users ->
                chatUserAdapter.submitList(users)
                binding.textViewNoUsers.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                chatAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                }
                binding.textViewNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentChatUser.collect { user ->
                user?.let {
                    binding.textViewChatUserName.text = it.name
                    binding.textViewChatUserRole.text = getRoleDisplayName(it.role)
                    binding.buttonBack.setOnClickListener {
                        binding.chatLayout.visibility = View.GONE
                        binding.chatUsersLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonSendMessage.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.currentChatUser.value?.let { user ->
                    viewModel.sendMessage(message, user.id)
                    binding.editTextMessage.text.clear()
                }
            } else {
                Toast.makeText(context, "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonRefreshUsers.setOnClickListener {
            sessionManager.currentUser?.let { user ->
                viewModel.loadChatUsers(user.id)
            }
        }
        
        binding.buttonRefreshMessages.setOnClickListener {
            viewModel.currentChatUser.value?.let { user ->
                viewModel.loadMessages(user.id)
            }
        }
    }
    
    private fun getRoleDisplayName(role: String): String {
        return when (role) {
            "chauffeur" -> "Chauffeur"
            "demandeur" -> "Demandeur"
            "dispatch" -> "Dispatcher"
            "admin" -> "Administrateur"
            else -> role
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
