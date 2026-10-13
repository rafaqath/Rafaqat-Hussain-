package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.model.PeerDevice
import com.example.ui.theme.BthDarkIncoming
import com.example.ui.theme.BthDarkOutgoing
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.BthTeal
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SignalBlue
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SpeedTurbo
import com.example.util.AppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
  conversationId: String,
  peer: PeerDevice?,
  isGroup: Boolean,
  groupName: String?,
  messages: List<ChatMessage>,
  language: AppLanguage,
  isRecordingVoice: Boolean,
  voiceDurationSec: Int,
  onBack: () -> Unit,
  onSendMessage: (String) -> Unit,
  onSendVoiceNote: () -> Unit,
  onCancelVoiceNote: () -> Unit,
  onStartVoiceRecording: () -> Unit,
  onPlayVoiceNote: (String?) -> Unit,
  onSendLargeFile: (fileName: String, sizeMB: Int, type: MessageType) -> Unit,
  onStartVoiceCall: () -> Unit,
  onStartVideoCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  var textInput by remember { mutableStateOf("") }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val title = if (isGroup) (groupName ?: "Mesh Group") else (peer?.name ?: "P2P Peer")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Top Bar
    TopAppBar(
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (peer != null) Color(peer.avatarColor) else BthTeal)
          ) {
            Text(
              text = title.take(2).uppercase(),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(SignalGreen)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isGroup) "Mesh Broadcast • 0 Mobile Data" else "P2P Latency ~12ms • Direct",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = BthEmerald
              )
            }
          }
        }
      },
      navigationIcon = {
        IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      },
      actions = {
        if (!isGroup) {
          IconButton(onClick = onStartVoiceCall, modifier = Modifier.testTag("detail_call_btn")) {
            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = BthEmerald)
          }
          IconButton(onClick = onStartVideoCall, modifier = Modifier.testTag("detail_video_btn")) {
            Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video", tint = BthTeal)
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )

    // Chat Message Stream
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      items(messages, key = { it.id }) { message ->
        ChatBubbleItem(
          message = message,
          onPlayVoice = { onPlayVoiceNote(message.attachmentUri) }
        )
        Spacer(modifier = Modifier.height(6.dp))
      }
    }

    // Attachment Options Sheet
    AnimatedVisibility(
      visible = showAttachmentSheet,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it } + fadeOut()
    ) {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = Strings.get("file_transfer_title", language),
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { showAttachmentSheet = false }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
          }

          Text(
            text = Strings.get("file_transfer_desc", language),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Transfer Buttons
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            AttachmentTile(
              icon = Icons.Default.Image,
              label = "Photo",
              badge = "Instant",
              color = BthEmerald,
              onClick = {
                onSendLargeFile("Photo_HighRes_${System.currentTimeMillis()}.jpg", 8, MessageType.IMAGE)
                showAttachmentSheet = false
              },
              modifier = Modifier.weight(1f)
            )
            AttachmentTile(
              icon = Icons.Default.VideoFile,
              label = "Video 30MB",
              badge = "1 Sec",
              color = SpeedTurbo,
              onClick = {
                onSendLargeFile("Video_Clip_1080p.mp4", 30, MessageType.VIDEO)
                showAttachmentSheet = false
              },
              modifier = Modifier.weight(1f)
            )
            AttachmentTile(
              icon = Icons.Default.Bolt,
              label = "File 50MB",
              badge = "1.5 Sec",
              color = BthTeal,
              onClick = {
                onSendLargeFile("Large_Data_Archive_50MB.zip", 50, MessageType.FILE)
                showAttachmentSheet = false
              },
              modifier = Modifier.weight(1f)
            )
            AttachmentTile(
              icon = Icons.Default.InsertDriveFile,
              label = "APK 100MB",
              badge = "2 Sec",
              color = SignalBlue,
              onClick = {
                onSendLargeFile("Offline_App_Installer_100MB.apk", 100, MessageType.FILE)
                showAttachmentSheet = false
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Bottom Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      if (isRecordingVoice) {
        // Voice Note Recording Mode
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(ErrorRed)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "0:${String.format("%02d", voiceDurationSec)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ErrorRed
          )
          Spacer(modifier = Modifier.width(12.dp))
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = BthEmerald,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = Strings.get("recording", language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
          )

          IconButton(onClick = onCancelVoiceNote, modifier = Modifier.testTag("cancel_voice_btn")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
          }

          IconButton(
            onClick = onSendVoiceNote,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(BthEmerald)
              .testTag("send_voice_btn")
          ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
          }
        }
      } else {
        // Regular Input Mode
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          IconButton(
            onClick = { showAttachmentSheet = !showAttachmentSheet },
            modifier = Modifier.testTag("attach_button")
          ) {
            Icon(
              imageVector = Icons.Default.AttachFile,
              contentDescription = "Attach",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text(Strings.get("type_message", language), fontSize = 13.sp) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BthEmerald,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 4.dp)
              .testTag("message_input_field")
          )

          if (textInput.isNotBlank()) {
            IconButton(
              onClick = {
                onSendMessage(textInput)
                textInput = ""
              },
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BthEmerald)
                .testTag("send_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            IconButton(
              onClick = onStartVoiceRecording,
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BthTeal.copy(alpha = 0.2f))
                .testTag("mic_button")
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = Strings.get("record_voice", language),
                tint = BthEmerald,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AttachmentTile(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  badge: String,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
      Text(text = badge, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
fun ChatBubbleItem(
  message: ChatMessage,
  onPlayVoice: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isOut = message.isOutgoing
  val bubbleColor = if (isOut) BthDarkOutgoing else BthDarkIncoming
  val align = if (isOut) Alignment.End else Alignment.Start
  val shape = if (isOut) {
    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  }

  val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val timeStr = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

  Column(
    horizontalAlignment = align,
    modifier = modifier.fillMaxWidth()
  ) {
    Card(
      shape = shape,
      colors = CardDefaults.cardColors(containerColor = bubbleColor),
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        if (!isOut && message.senderName.isNotBlank()) {
          Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BthEmerald)
          )
          Spacer(modifier = Modifier.height(2.dp))
        }

        when (message.type) {
          MessageType.TEXT -> {
            Text(
              text = message.content,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White
            )
          }

          MessageType.VOICE_NOTE -> {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable(onClick = onPlayVoice)
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(BthEmerald)
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Play Voice Note",
                  tint = Color.Black,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Icon(
                  imageVector = Icons.Default.GraphicEq,
                  contentDescription = null,
                  tint = BthEmerald,
                  modifier = Modifier.size(28.dp)
                )
                Text(
                  text = "0:${String.format("%02d", message.voiceDurationSec ?: 10)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.LightGray
                )
              }
            }
          }

          MessageType.IMAGE -> {
            Column {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(140.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.DarkGray)
              ) {
                Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = null,
                  tint = Color.LightGray,
                  modifier = Modifier.size(48.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = message.attachmentName ?: "Photo.jpg",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
              )
            }
          }

          MessageType.VIDEO -> {
            Column {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1E2A38))
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = SpeedTurbo,
                  modifier = Modifier.size(44.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${message.attachmentName ?: "Video.mp4"} (${message.attachmentSize / (1024 * 1024)} MB)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
              )
            }
          }

          MessageType.FILE -> {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SpeedTurbo.copy(alpha = 0.2f))
                ) {
                  Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = SpeedTurbo,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = message.attachmentName ?: "File.pkg",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${message.attachmentSize / (1024 * 1024)} MB • Turbo Mesh",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = BthEmerald
                  )
                }
              }

              if (message.transferProgress < 1.0f) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                  progress = { message.transferProgress },
                  color = SpeedTurbo,
                  trackColor = Color.Gray,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                )
                Text(
                  text = "Transferring at ${String.format("%.1f", message.transferSpeedMBs)} MB/s",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = SpeedTurbo
                )
              }
            }
          }

          else -> {
            Text(text = message.content, color = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time and Read receipts
        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Color.LightGray.copy(alpha = 0.8f)
          )
          if (isOut) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = "Read",
              tint = SignalBlue,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}
