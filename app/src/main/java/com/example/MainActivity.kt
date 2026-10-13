package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BthChatViewModel
import com.example.ui.MainTab
import com.example.ui.components.BthBottomNavBar
import com.example.ui.components.BthTopBar
import com.example.ui.screens.CallScreen
import com.example.ui.screens.CallsTabScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsTabScreen
import com.example.ui.screens.GroupsTabScreen
import com.example.ui.screens.RadarTabScreen
import com.example.ui.screens.TransfersTabScreen
import com.example.ui.theme.BthChatTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BthChatTheme {
        BthChatAppRoot()
      }
    }
  }
}

@Composable
fun BthChatAppRoot(viewModel: BthChatViewModel = viewModel()) {
  val permissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { /* Handled gracefully */ }

  LaunchedEffect(Unit) {
    val perms = mutableListOf(
      Manifest.permission.RECORD_AUDIO,
      Manifest.permission.CAMERA,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      perms.add(Manifest.permission.BLUETOOTH_SCAN)
      perms.add(Manifest.permission.BLUETOOTH_CONNECT)
      perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    permissionsLauncher.launch(perms.toTypedArray())
  }

  val currentTab by viewModel.currentTab.collectAsState()
  val activeConversationId by viewModel.activeConversationId.collectAsState()
  val isGroupActive by viewModel.isGroupActive.collectAsState()
  val activeCall by viewModel.activeCall.collectAsState()
  val language by viewModel.language.collectAsState()
  val isSimulationMode by viewModel.isSimulationMode.collectAsState()
  val peers by viewModel.connectedPeers.collectAsState()
  val groups by viewModel.groups.collectAsState()
  val messages by viewModel.repository.messages.collectAsState()
  val activeTransfers by viewModel.activeTransfers.collectAsState()
  val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
  val voiceRecordingDuration by viewModel.voiceRecordingDuration.collectAsState()
  val conversationMessages by viewModel.currentConversationMessages.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val isBluetoothScanning by viewModel.isBluetoothScanning.collectAsState()
  val discoveredBluetoothDevices by viewModel.discoveredBluetoothDevices.collectAsState()

  // 1. Full Screen Call
  if (activeCall != null) {
    CallScreen(
      callSession = activeCall!!,
      language = language,
      onAccept = { viewModel.acceptCall() },
      onEndCall = { viewModel.endCall() },
      onToggleMute = { viewModel.toggleMute() },
      onToggleSpeaker = { viewModel.toggleSpeaker() },
      onToggleCameraFlip = { viewModel.toggleCameraFlip() }
    )
    return
  }

  // 2. Chat Conversation View
  if (activeConversationId != null) {
    val activePeer = peers.find { it.id == activeConversationId }
    val activeGroup = groups.find { it.id == activeConversationId }

    ChatDetailScreen(
      conversationId = activeConversationId!!,
      peer = activePeer,
      isGroup = isGroupActive,
      groupName = activeGroup?.name,
      messages = conversationMessages,
      language = language,
      isRecordingVoice = isRecordingVoice,
      voiceDurationSec = voiceRecordingDuration,
      onBack = { viewModel.closeChat() },
      onSendMessage = { viewModel.sendMessage(it) },
      onSendVoiceNote = { viewModel.stopAndSendVoiceRecording() },
      onCancelVoiceNote = { viewModel.cancelVoiceRecording() },
      onStartVoiceRecording = { viewModel.startVoiceRecording() },
      onPlayVoiceNote = { viewModel.playVoiceNote(it) },
      onSendLargeFile = { fileName, sizeMB, type ->
        viewModel.sendHighSpeedFile(fileName, sizeMB, type)
      },
      onStartVoiceCall = {
        activePeer?.let { viewModel.startVoiceCall(it.id) }
      },
      onStartVideoCall = {
        activePeer?.let { viewModel.startVideoCall(it.id) }
      }
    )
    return
  }

  // 3. Main Tabs Screen
  Scaffold(
    topBar = {
      BthTopBar(
        language = language,
        isSimulationMode = isSimulationMode,
        connectedPeersCount = peers.size,
        onToggleLanguage = { viewModel.toggleLanguage() },
        onToggleSimulation = { viewModel.toggleSimulationMode() }
      )
    },
    bottomBar = {
      BthBottomNavBar(
        currentTab = currentTab,
        language = language,
        activeTransfersCount = activeTransfers.count { it.status == com.example.model.TransferStatus.TRANSFERRING },
        onSelectTab = { viewModel.selectTab(it) }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        MainTab.CHATS -> {
          ChatsTabScreen(
            peers = peers,
            messagesMap = messages,
            searchQuery = searchQuery,
            language = language,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onOpenChat = { viewModel.openChat(it, isGroup = false) },
            onStartVoiceCall = { viewModel.startVoiceCall(it) },
            onStartVideoCall = { viewModel.startVideoCall(it) }
          )
        }

        MainTab.GROUPS -> {
          GroupsTabScreen(
            groups = groups,
            peers = peers,
            messagesMap = messages,
            language = language,
            onOpenGroupChat = { viewModel.openChat(it, isGroup = true) },
            onCreateGroup = { name, desc, members ->
              viewModel.createGroup(name, desc, members)
            }
          )
        }

        MainTab.TRANSFERS -> {
          TransfersTabScreen(
            transfers = activeTransfers,
            peers = peers,
            language = language,
            onSendTestFile = { sizeMB ->
              val firstPeer = peers.firstOrNull()
              if (firstPeer != null) {
                viewModel.openChat(firstPeer.id)
                viewModel.sendHighSpeedFile("BTH_TurboPack_${sizeMB}MB.pkg", sizeMB)
              }
            },
            onClearCompleted = {
              viewModel.repository.transferEngine.clearFinishedTransfers()
            }
          )
        }

        MainTab.CALLS -> {
          CallsTabScreen(
            peers = peers,
            language = language,
            onStartVoiceCall = { viewModel.startVoiceCall(it) },
            onStartVideoCall = { viewModel.startVideoCall(it) }
          )
        }

        MainTab.RADAR -> {
          RadarTabScreen(
            isScanning = isBluetoothScanning,
            peers = peers,
            discoveredDevices = discoveredBluetoothDevices,
            language = language,
            onStartScan = { viewModel.startNearbyScan() },
            onConnectPeer = { viewModel.connectToPeer(it) }
          )
        }
      }
    }
  }
}
