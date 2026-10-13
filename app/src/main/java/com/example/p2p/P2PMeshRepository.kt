package com.example.p2p

import android.content.Context
import com.example.model.CallSession
import com.example.model.CallState
import com.example.model.ChatGroup
import com.example.model.ChatMessage
import com.example.model.ConnectionType
import com.example.model.DeliveryStatus
import com.example.model.FileTransferSession
import com.example.model.MessageType
import com.example.model.PeerDevice
import com.example.model.TransferStatus
import com.example.util.AudioRecorderPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class P2PMeshRepository(
  private val context: Context,
  private val scope: CoroutineScope
) {
  val bluetoothManager = BthBluetoothManager(context, scope)
  val transferEngine = BthTurboTransferEngine(context, scope)
  val audioHelper = AudioRecorderPlayer(context)

  // Simulation vs Hardware mode
  private val _isSimulationMode = MutableStateFlow(true)
  val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

  // Peer devices
  private val _connectedPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
  val connectedPeers: StateFlow<List<PeerDevice>> = _connectedPeers.asStateFlow()

  // Messages map: conversationId -> List<ChatMessage>
  private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

  // Groups
  private val _groups = MutableStateFlow<List<ChatGroup>>(emptyList())
  val groups: StateFlow<List<ChatGroup>> = _groups.asStateFlow()

  // Active Call Session
  private val _activeCall = MutableStateFlow<CallSession?>(null)
  val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

  private var callTimerJob: Job? = null

  init {
    initDefaultData()
    transferEngine.startHighSpeedReceiver { file, name ->
      // When a file is received via high speed socket
      scope.launch(Dispatchers.Main) {
        val peer = _connectedPeers.value.firstOrNull()
        if (peer != null) {
          receiveMessage(
            ChatMessage(
              id = UUID.randomUUID().toString(),
              conversationId = peer.id,
              senderId = peer.id,
              senderName = peer.name,
              isOutgoing = false,
              content = "Received file: $name (${file.length() / (1024 * 1024)} MB)",
              type = MessageType.FILE,
              attachmentUri = file.absolutePath,
              attachmentName = name,
              attachmentSize = file.length(),
              deliveryStatus = DeliveryStatus.READ
            )
          )
        }
      }
    }
  }

  private fun initDefaultData() {
    val samplePeers = listOf(
      PeerDevice(
        id = "peer_rehan",
        name = "Rehan Allahwala (P2P Host)",
        address = "78:21:84:9A:C3:01",
        isBonded = true,
        isConnected = true,
        rssi = -42,
        connectionType = ConnectionType.LOCAL_MESH,
        avatarColor = 0xFF00D285,
        statusMessage = "Broadcasting BTH Network • Wi-Fi Direct"
      ),
      PeerDevice(
        id = "peer_fatima",
        name = "Fatima (Galaxy S24 Ultra)",
        address = "9C:14:63:F1:B2:44",
        isBonded = true,
        isConnected = true,
        rssi = -51,
        connectionType = ConnectionType.WIFI_DIRECT,
        avatarColor = 0xFF00E5FF,
        statusMessage = "Ready for 100MB Turbo Transfer"
      ),
      PeerDevice(
        id = "peer_ahmed",
        name = "Ahmed (Pixel 9 Pro)",
        address = "A4:83:E7:5C:11:89",
        isBonded = true,
        isConnected = true,
        rssi = -64,
        connectionType = ConnectionType.BLUETOOTH,
        avatarColor = 0xFFFF9800,
        statusMessage = "Bluetooth RFCOMM Connected"
      ),
      PeerDevice(
        id = "peer_hamza",
        name = "Hamza (Redmi Note 13)",
        address = "C0:E4:34:72:90:5F",
        isBonded = false,
        isConnected = true,
        rssi = -72,
        connectionType = ConnectionType.LOCAL_MESH,
        avatarColor = 0xFF9C27B0,
        statusMessage = "Mesh Relay Node • Active"
      )
    )
    _connectedPeers.value = samplePeers

    val sampleGroup = ChatGroup(
      id = "group_bth_mesh",
      name = "BTH Pakistan P2P Network",
      description = "Rehan Wala Team P2P mesh offline community",
      memberPeerIds = samplePeers.map { it.id },
      groupAvatarColor = 0xFF00A884
    )
    _groups.value = listOf(sampleGroup)

    // Seed conversation messages
    val now = System.currentTimeMillis()
    val initialMessages = mutableMapOf<String, List<ChatMessage>>()

    initialMessages["peer_rehan"] = listOf(
      ChatMessage(
        id = "m1",
        conversationId = "peer_rehan",
        senderId = "peer_rehan",
        senderName = "Rehan Allahwala (P2P Host)",
        isOutgoing = false,
        content = "السلام علیکم! یہ BTH Chat ایپ انٹرنیٹ کے بغیر براہ راست بلوٹوتھ اور وائی فائی ڈائریکٹ پر کام کر رہی ہے۔",
        timestamp = now - 3600000,
        type = MessageType.TEXT
      ),
      ChatMessage(
        id = "m2",
        conversationId = "peer_rehan",
        senderId = "me",
        senderName = "Me",
        isOutgoing = true,
        content = "زبردست! کیا ہم 50 ایم بی اور 100 ایم بی کی بڑی فائلیں بھی ایک دو سیکنڈ میں بھیج سکتے ہیں؟",
        timestamp = now - 3500000,
        type = MessageType.TEXT
      ),
      ChatMessage(
        id = "m3",
        conversationId = "peer_rehan",
        senderId = "peer_rehan",
        senderName = "Rehan Allahwala (P2P Host)",
        isOutgoing = false,
        content = "جی ہاں بالکل! ٹربو وائی فائی ڈائریکٹ میش کے ذریعے 50MB سے 200MB تک فائلیں چند سیکنڈ میں منتقل ہوتی ہیں!",
        timestamp = now - 3400000,
        type = MessageType.TEXT
      ),
      ChatMessage(
        id = "m4",
        conversationId = "peer_rehan",
        senderId = "peer_rehan",
        senderName = "Rehan Allahwala (P2P Host)",
        isOutgoing = false,
        content = "Voice note test",
        timestamp = now - 3200000,
        type = MessageType.VOICE_NOTE,
        voiceDurationSec = 14
      )
    )

    initialMessages["peer_fatima"] = listOf(
      ChatMessage(
        id = "mf1",
        conversationId = "peer_fatima",
        senderId = "peer_fatima",
        senderName = "Fatima (Galaxy S24 Ultra)",
        isOutgoing = false,
        content = "Hi! I just joined the offline P2P mesh network. Signal strength is 98%!",
        timestamp = now - 1800000,
        type = MessageType.TEXT
      )
    )

    initialMessages["group_bth_mesh"] = listOf(
      ChatMessage(
        id = "mg1",
        conversationId = "group_bth_mesh",
        senderId = "peer_rehan",
        senderName = "Rehan Allahwala",
        isOutgoing = false,
        content = "تمام دوستوں کو خوش آمدید! یہ گروپ میش براڈکاسٹ ہے، ایک کلک پر تمام موبائلز کو ڈیٹا پہنچے گا۔",
        timestamp = now - 900000,
        type = MessageType.TEXT
      )
    )

    _messages.value = initialMessages
  }

  fun toggleSimulationMode() {
    _isSimulationMode.value = !_isSimulationMode.value
    if (!_isSimulationMode.value) {
      // Switched to hardware mode: load bonded Bluetooth devices
      val paired = bluetoothManager.loadPairedDevices()
      if (paired.isNotEmpty()) {
        _connectedPeers.value = paired
      }
      bluetoothManager.startDiscovery()
    } else {
      initDefaultData()
    }
  }

  fun sendTextMessage(conversationId: String, text: String, isGroup: Boolean = false) {
    if (text.isBlank()) return
    val msg = ChatMessage(
      id = UUID.randomUUID().toString(),
      conversationId = conversationId,
      senderId = "me",
      senderName = "Me",
      recipientId = if (isGroup) null else conversationId,
      isOutgoing = true,
      content = text.trim(),
      timestamp = System.currentTimeMillis(),
      type = MessageType.TEXT,
      deliveryStatus = DeliveryStatus.DELIVERED
    )
    appendMessage(conversationId, msg)

    // In simulation mode, simulate an automatic peer response
    if (_isSimulationMode.value && !isGroup) {
      simulatePeerResponse(conversationId)
    }
  }

  fun sendVoiceNote(conversationId: String, audioFile: File?, durationSec: Int) {
    val msg = ChatMessage(
      id = UUID.randomUUID().toString(),
      conversationId = conversationId,
      senderId = "me",
      senderName = "Me",
      recipientId = conversationId,
      isOutgoing = true,
      content = "Voice note ($durationSec s)",
      timestamp = System.currentTimeMillis(),
      type = MessageType.VOICE_NOTE,
      attachmentUri = audioFile?.absolutePath,
      voiceDurationSec = durationSec,
      deliveryStatus = DeliveryStatus.READ
    )
    appendMessage(conversationId, msg)

    if (_isSimulationMode.value) {
      scope.launch(Dispatchers.Default) {
        delay(2000)
        receiveMessage(
          ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = conversationId,
            senderName = _connectedPeers.value.find { it.id == conversationId }?.name ?: "Peer",
            isOutgoing = false,
            content = "Voice note received loud & clear via P2P!",
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
          )
        )
      }
    }
  }

  fun sendLargeFile(
    conversationId: String,
    fileName: String,
    sizeMB: Int,
    fileType: MessageType = MessageType.FILE
  ) {
    val sizeBytes = sizeMB * 1024 * 1024L
    val msgId = UUID.randomUUID().toString()
    val msg = ChatMessage(
      id = msgId,
      conversationId = conversationId,
      senderId = "me",
      senderName = "Me",
      recipientId = conversationId,
      isOutgoing = true,
      content = "Transferring $fileName ($sizeMB MB)...",
      timestamp = System.currentTimeMillis(),
      type = fileType,
      attachmentName = fileName,
      attachmentSize = sizeBytes,
      deliveryStatus = DeliveryStatus.SENT,
      transferProgress = 0.05f,
      transferSpeedMBs = 68.5f
    )
    appendMessage(conversationId, msg)

    val peer = _connectedPeers.value.find { it.id == conversationId }
    val peerName = peer?.name ?: "P2P Peer"

    transferEngine.triggerQuickBenchmark(sizeMB, peerName, isOutgoing = true) {
      // On completed
      updateMessage(conversationId, msgId) { old ->
        old.copy(
          content = "Sent $fileName ($sizeMB MB) via Turbo Mesh",
          transferProgress = 1.0f,
          transferSpeedMBs = 74.5f,
          deliveryStatus = DeliveryStatus.READ
        )
      }
    }

    // Progress updates
    scope.launch(Dispatchers.Default) {
      val steps = 10
      for (i in 1..steps) {
        delay((sizeMB * 1000L / (68 * steps)).coerceIn(100, 300))
        val prog = (i.toFloat() / steps).coerceAtMost(1f)
        updateMessage(conversationId, msgId) { old ->
          old.copy(
            transferProgress = prog,
            transferSpeedMBs = 65f + ((-4..6).random())
          )
        }
      }
    }
  }

  fun createGroup(name: String, description: String, memberIds: List<String>) {
    val newGroup = ChatGroup(
      id = "group_${System.currentTimeMillis()}",
      name = name,
      description = description,
      memberPeerIds = memberIds,
      createdAt = System.currentTimeMillis()
    )
    _groups.value = _groups.value + newGroup

    sendTextMessage(
      conversationId = newGroup.id,
      text = "گروپ بنایا گیا: $name. تمام منتخب دوست میش کے ذریعے جڑ چکے ہیں۔",
      isGroup = true
    )
  }

  fun startCall(peerId: String, isVideo: Boolean) {
    val peer = _connectedPeers.value.find { it.id == peerId } ?: return
    val session = CallSession(
      callId = UUID.randomUUID().toString(),
      peerId = peer.id,
      peerName = peer.name,
      isVideo = isVideo,
      isIncoming = false,
      state = CallState.OUTGOING_RINGING,
      durationSeconds = 0
    )
    _activeCall.value = session

    // Simulate peer pickup after 2 seconds
    scope.launch(Dispatchers.Default) {
      delay(2500)
      if (_activeCall.value?.callId == session.callId && _activeCall.value?.state == CallState.OUTGOING_RINGING) {
        _activeCall.value = session.copy(state = CallState.CONNECTED)
        startCallTimer()
      }
    }
  }

  fun acceptIncomingCall() {
    _activeCall.value?.let { current ->
      _activeCall.value = current.copy(state = CallState.CONNECTED)
      startCallTimer()
    }
  }

  fun toggleMute() {
    _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
  }

  fun toggleSpeaker() {
    _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
  }

  fun toggleCameraFlip() {
    _activeCall.value = _activeCall.value?.let { it.copy(isCameraFront = !it.isCameraFront) }
  }

  fun endCall() {
    callTimerJob?.cancel()
    callTimerJob = null
    _activeCall.value = null
  }

  private fun startCallTimer() {
    callTimerJob?.cancel()
    callTimerJob = scope.launch(Dispatchers.Default) {
      while (_activeCall.value?.state == CallState.CONNECTED) {
        delay(1000)
        _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
      }
    }
  }

  private fun appendMessage(conversationId: String, message: ChatMessage) {
    val map = _messages.value.toMutableMap()
    val list = map[conversationId]?.toMutableList() ?: mutableListOf()
    list.add(message)
    map[conversationId] = list
    _messages.value = map
  }

  private fun updateMessage(conversationId: String, messageId: String, transform: (ChatMessage) -> ChatMessage) {
    val map = _messages.value.toMutableMap()
    val list = map[conversationId]?.toMutableList() ?: return
    val index = list.indexOfFirst { it.id == messageId }
    if (index >= 0) {
      list[index] = transform(list[index])
      map[conversationId] = list
      _messages.value = map
    }
  }

  private fun receiveMessage(message: ChatMessage) {
    appendMessage(message.conversationId, message)
  }

  private fun simulatePeerResponse(conversationId: String) {
    val peer = _connectedPeers.value.find { it.id == conversationId } ?: return
    scope.launch(Dispatchers.Default) {
      delay(1800)
      val responses = listOf(
        "پیغام موصول ہو گیا ہے! بلوٹوتھ پی ٹو پی سگنل بہترین ہے۔",
        "P2P connection is crystal clear! Ready for next file transfer.",
        "جی بھائی، ریحان والا ٹیم کے طریقہ کار پر سب کنیکٹڈ ہے۔",
        "File received at 72 MB/s without any internet!",
        "زبردست! تمام فیچرز درست طریقے سے کام کر رہے ہیں۔"
      )
      val reply = ChatMessage(
        id = UUID.randomUUID().toString(),
        conversationId = conversationId,
        senderId = peer.id,
        senderName = peer.name,
        isOutgoing = false,
        content = responses.random(),
        timestamp = System.currentTimeMillis(),
        type = MessageType.TEXT
      )
      receiveMessage(reply)
    }
  }
}
