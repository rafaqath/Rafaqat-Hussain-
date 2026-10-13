package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.ConnectionType
import com.example.model.PeerDevice
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.BthTeal
import com.example.ui.theme.SignalGreen
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun ChatsTabScreen(
  peers: List<PeerDevice>,
  messagesMap: Map<String, List<ChatMessage>>,
  searchQuery: String,
  language: AppLanguage,
  onSearchChange: (String) -> Unit,
  onOpenChat: (String) -> Unit,
  onStartVoiceCall: (String) -> Unit,
  onStartVideoCall: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val filteredPeers = peers.filter {
    it.name.contains(searchQuery, ignoreCase = true) ||
    it.statusMessage.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Search Box
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      placeholder = { Text(Strings.get("search_hint", language), fontSize = 13.sp) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      singleLine = true,
      shape = RoundedCornerShape(24.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BthEmerald,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("chat_search_field")
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredPeers.isEmpty()) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No nearby peers found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(filteredPeers, key = { it.id }) { peer ->
          val lastMsg = messagesMap[peer.id]?.lastOrNull()
          PeerChatItem(
            peer = peer,
            lastMessage = lastMsg,
            language = language,
            onClick = { onOpenChat(peer.id) },
            onCall = { onStartVoiceCall(peer.id) },
            onVideoCall = { onStartVideoCall(peer.id) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
fun PeerChatItem(
  peer: PeerDevice,
  lastMessage: ChatMessage?,
  language: AppLanguage,
  onClick: () -> Unit,
  onCall: () -> Unit,
  onVideoCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("peer_item_${peer.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      // Avatar with status dot
      Box {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(peer.avatarColor))
        ) {
          Text(
            text = peer.name.take(2).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          )
        }
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp)
            .align(Alignment.BottomEnd)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape)
              .background(if (peer.isConnected) SignalGreen else Color.Gray)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = peer.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          // Protocol badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(
                if (peer.connectionType == ConnectionType.WIFI_DIRECT)
                  BthTeal.copy(alpha = 0.2f)
                else
                  BthEmerald.copy(alpha = 0.15f)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Icon(
              imageVector = if (peer.connectionType == ConnectionType.WIFI_DIRECT) Icons.Default.Wifi else Icons.Default.Bluetooth,
              contentDescription = null,
              tint = if (peer.connectionType == ConnectionType.WIFI_DIRECT) BthTeal else BthEmerald,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (peer.connectionType == ConnectionType.WIFI_DIRECT) "Wi-Fi Direct" else "Bluetooth",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = lastMessage?.content ?: peer.statusMessage,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Direct Call actions
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onCall,
          modifier = Modifier
            .size(36.dp)
            .testTag("call_btn_${peer.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Voice Call",
            tint = BthEmerald,
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = onVideoCall,
          modifier = Modifier
            .size(36.dp)
            .testTag("video_btn_${peer.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Video Call",
            tint = BthTeal,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
