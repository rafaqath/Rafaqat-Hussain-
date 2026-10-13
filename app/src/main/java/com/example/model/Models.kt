package com.example.model

enum class ConnectionType {
  BLUETOOTH,
  WIFI_DIRECT,
  LOCAL_MESH
}

enum class MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  VOICE_NOTE,
  FILE,
  CALL_EVENT
}

enum class DeliveryStatus {
  PENDING,
  SENT,
  DELIVERED,
  READ
}

enum class TransferStatus {
  PREPARING,
  TRANSFERRING,
  COMPLETED,
  PAUSED,
  FAILED
}

enum class CallState {
  OUTGOING_RINGING,
  INCOMING_RINGING,
  CONNECTED,
  ENDED
}

data class PeerDevice(
  val id: String,
  val name: String,
  val address: String,
  val isBonded: Boolean = false,
  val isConnected: Boolean = false,
  val rssi: Int = -60, // dBm
  val connectionType: ConnectionType = ConnectionType.BLUETOOTH,
  val ipAddress: String? = null,
  val port: Int = 8988,
  val avatarColor: Long = 0xFF00D285,
  val statusMessage: String = "Online via BTH Mesh"
)

data class ChatMessage(
  val id: String,
  val conversationId: String,
  val senderId: String,
  val senderName: String,
  val recipientId: String? = null,
  val isOutgoing: Boolean,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val type: MessageType = MessageType.TEXT,
  val attachmentUri: String? = null,
  val attachmentName: String? = null,
  val attachmentSize: Long = 0L,
  val attachmentMime: String? = null,
  val voiceDurationSec: Int? = null,
  val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
  val transferProgress: Float = 1.0f,
  val transferSpeedMBs: Float = 0f
)

data class ChatGroup(
  val id: String,
  val name: String,
  val description: String,
  val memberPeerIds: List<String>,
  val createdAt: Long = System.currentTimeMillis(),
  val groupAvatarColor: Long = 0xFF00A884
)

data class FileTransferSession(
  val id: String,
  val fileName: String,
  val fileSizeBytes: Long,
  val transferredBytes: Long,
  val speedMBs: Float,
  val isOutgoing: Boolean,
  val peerName: String,
  val status: TransferStatus,
  val protocol: String,
  val startTime: Long = System.currentTimeMillis(),
  val estimatedSecondsRemaining: Int = 0
)

data class CallSession(
  val callId: String,
  val peerId: String,
  val peerName: String,
  val isVideo: Boolean,
  val isIncoming: Boolean,
  val state: CallState,
  val durationSeconds: Int = 0,
  val isMuted: Boolean = false,
  val isSpeakerOn: Boolean = true,
  val isCameraFront: Boolean = true,
  val videoStreamQuality: String = "HD 720p (Local Mesh)"
)
