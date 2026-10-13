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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.ChatGroup
import com.example.model.ChatMessage
import com.example.model.PeerDevice
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.BthTeal
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun GroupsTabScreen(
  groups: List<ChatGroup>,
  peers: List<PeerDevice>,
  messagesMap: Map<String, List<ChatMessage>>,
  language: AppLanguage,
  onOpenGroupChat: (String) -> Unit,
  onCreateGroup: (String, String, List<String>) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCreateDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      // Mesh Broadcast Explainer Banner
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = BthEmerald.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(14.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Hub,
            contentDescription = null,
            tint = BthEmerald,
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "P2P Multi-Device Mesh Broadcast",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Send messages, images & files to multiple nearby phones simultaneously without routers or cellular towers.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(groups, key = { it.id }) { group ->
          val lastMsg = messagesMap[group.id]?.lastOrNull()
          GroupItem(
            group = group,
            lastMessage = lastMsg,
            onClick = { onOpenGroupChat(group.id) }
          )
          Spacer(modifier = Modifier.height(10.dp))
        }
      }
    }

    // FAB to create new group
    FloatingActionButton(
      onClick = { showCreateDialog = true },
      containerColor = BthEmerald,
      contentColor = Color.Black,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("create_group_fab")
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = Strings.get("new_group", language))
    }

    if (showCreateDialog) {
      CreateGroupDialog(
        peers = peers,
        language = language,
        onDismiss = { showCreateDialog = false },
        onConfirm = { name, desc, members ->
          onCreateGroup(name, desc, members)
          showCreateDialog = false
        }
      )
    }
  }
}

@Composable
fun GroupItem(
  group: ChatGroup,
  lastMessage: ChatMessage?,
  onClick: () -> Unit,
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
      .testTag("group_item_${group.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(Color(group.groupAvatarColor))
      ) {
        Icon(
          imageVector = Icons.Default.Group,
          contentDescription = null,
          tint = Color.Black,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = group.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          Text(
            text = "${group.memberPeerIds.size + 1} devices",
            style = MaterialTheme.typography.labelSmall,
            color = BthEmerald
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = lastMessage?.let { "${it.senderName}: ${it.content}" } ?: group.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun CreateGroupDialog(
  peers: List<PeerDevice>,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (String, String, List<String>) -> Unit
) {
  var groupName by remember { mutableStateOf("") }
  var groupDesc by remember { mutableStateOf("") }
  val selectedMembers = remember { mutableStateListOf<String>().apply { addAll(peers.map { it.id }) } }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(Strings.get("create_group", language), fontWeight = FontWeight.Bold) },
    text = {
      Column {
        OutlinedTextField(
          value = groupName,
          onValueChange = { groupName = it },
          label = { Text(Strings.get("group_name_hint", language)) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BthEmerald),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_group_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = groupDesc,
          onValueChange = { groupDesc = it },
          label = { Text("Description") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BthEmerald),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "${Strings.get("members", language)} (${selectedMembers.size}/${peers.size})",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(modifier = Modifier.height(140.dp)) {
          items(peers) { peer ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (selectedMembers.contains(peer.id)) {
                    selectedMembers.remove(peer.id)
                  } else {
                    selectedMembers.add(peer.id)
                  }
                }
                .padding(vertical = 4.dp)
            ) {
              Checkbox(
                checked = selectedMembers.contains(peer.id),
                onCheckedChange = { checked ->
                  if (checked) selectedMembers.add(peer.id) else selectedMembers.remove(peer.id)
                },
                colors = CheckboxDefaults.colors(checkedColor = BthEmerald)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = peer.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (groupName.isNotBlank()) {
            onConfirm(groupName, groupDesc.ifBlank { "P2P Mesh Broadcast Group" }, selectedMembers.toList())
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = BthEmerald, contentColor = Color.Black),
        modifier = Modifier.testTag("confirm_create_group_button")
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
