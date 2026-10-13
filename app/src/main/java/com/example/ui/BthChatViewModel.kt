package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CallSession
import com.example.model.ChatGroup
import com.example.model.ChatMessage
import com.example.model.FileTransferSession
import com.example.model.MessageType
import com.example.model.PeerDevice
import com.example.p2p.P2PMeshRepository
import com.example.util.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class MainTab {
  CHATS,
  GROUPS,
  TRANSFERS,
  CALLS,
  RADAR
}

class BthChatViewModel(application: Application) : AndroidViewModel(application) {
  val repository = P2PMeshRepository(application, viewModelScope)

  private val _currentTab = MutableStateFlow(MainTab.CHATS)
  val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

  private val _activeConversationId = MutableStateFlow<String?>(null)
  val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

  private val _isGroupActive = MutableStateFlow(false)
  val isGroupActive: StateFlow<Boolean> = _isGroupActive.asStateFlow()

  private val _language = MutableStateFlow(AppLanguage.URDU) // Default Urdu as requested by user prompt!
  val language: StateFlow<AppLanguage> = _language.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Voice recording state
  private val _isRecordingVoice = MutableStateFlow(false)
  val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

  private val _voiceRecordingDuration = MutableStateFlow(0)
  val voiceRecordingDuration: StateFlow<Int> = _voiceRecordingDuration.asStateFlow()

  private var voiceTimerJob: Job? = null
  private var currentVoiceFile: File? = null

  // Active call
  val activeCall: StateFlow<CallSession?> = repository.activeCall
  val connectedPeers: StateFlow<List<PeerDevice>> = repository.connectedPeers
  val groups: StateFlow<List<ChatGroup>> = repository.groups
  val activeTransfers: StateFlow<List<FileTransferSession>> = repository.transferEngine.activeTransfers
  val isSimulationMode: StateFlow<Boolean> = repository.isSimulationMode
  val isBluetoothScanning: StateFlow<Boolean> = repository.bluetoothManager.isScanning
  val discoveredBluetoothDevices: StateFlow<List<PeerDevice>> = repository.bluetoothManager.discoveredDevices

  // Active chat messages
  val currentConversationMessages: StateFlow<List<ChatMessage>> = combine(
    repository.messages,
    _activeConversationId
  ) { messagesMap, activeId ->
    if (activeId != null) messagesMap[activeId] ?: emptyList() else emptyList()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectTab(tab: MainTab) {
    _currentTab.value = tab
  }

  fun openChat(conversationId: String, isGroup: Boolean = false) {
    _activeConversationId.value = conversationId
    _isGroupActive.value = isGroup
  }

  fun closeChat() {
    _activeConversationId.value = null
  }

  fun toggleLanguage() {
    _language.value = if (_language.value == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun toggleSimulationMode() {
    repository.toggleSimulationMode()
  }

  fun sendMessage(text: String) {
    val convId = _activeConversationId.value ?: return
    repository.sendTextMessage(convId, text, isGroup = _isGroupActive.value)
  }

  fun startVoiceRecording() {
    currentVoiceFile = repository.audioHelper.startRecording()
    _isRecordingVoice.value = true
    _voiceRecordingDuration.value = 0
    voiceTimerJob?.cancel()
    voiceTimerJob = viewModelScope.launch {
      while (_isRecordingVoice.value) {
        delay(1000)
        _voiceRecordingDuration.value = _voiceRecordingDuration.value + 1
      }
    }
  }

  fun stopAndSendVoiceRecording() {
    voiceTimerJob?.cancel()
    _isRecordingVoice.value = false
    val recorded = repository.audioHelper.stopRecording()
    val duration = _voiceRecordingDuration.value.coerceAtLeast(1)
    val convId = _activeConversationId.value ?: return
    repository.sendVoiceNote(convId, recorded ?: currentVoiceFile, duration)
    _voiceRecordingDuration.value = 0
  }

  fun cancelVoiceRecording() {
    voiceTimerJob?.cancel()
    _isRecordingVoice.value = false
    repository.audioHelper.stopRecording()
    _voiceRecordingDuration.value = 0
  }

  fun playVoiceNote(audioPath: String?) {
    if (audioPath == null) return
    val file = File(audioPath)
    if (file.exists()) {
      repository.audioHelper.playAudio(file) {}
    }
  }

  fun sendHighSpeedFile(fileName: String, sizeMB: Int, type: MessageType = MessageType.FILE) {
    val convId = _activeConversationId.value ?: return
    repository.sendLargeFile(convId, fileName, sizeMB, type)
  }

  fun startVoiceCall(peerId: String) {
    repository.startCall(peerId, isVideo = false)
  }

  fun startVideoCall(peerId: String) {
    repository.startCall(peerId, isVideo = true)
  }

  fun acceptCall() {
    repository.acceptIncomingCall()
  }

  fun endCall() {
    repository.endCall()
  }

  fun toggleMute() {
    repository.toggleMute()
  }

  fun toggleSpeaker() {
    repository.toggleSpeaker()
  }

  fun toggleCameraFlip() {
    repository.toggleCameraFlip()
  }

  fun createGroup(name: String, description: String, memberIds: List<String>) {
    repository.createGroup(name, description, memberIds)
  }

  fun startNearbyScan() {
    repository.bluetoothManager.startDiscovery()
  }

  fun connectToPeer(device: PeerDevice) {
    repository.bluetoothManager.connectToDevice(
      device.address,
      onConnected = {
        openChat(device.id)
      },
      onError = {
        // Fallback open chat in sandbox
        openChat(device.id)
      }
    )
  }
}
