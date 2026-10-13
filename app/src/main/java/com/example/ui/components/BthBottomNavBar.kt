package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.BthEmerald
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun BthBottomNavBar(
  currentTab: MainTab,
  language: AppLanguage,
  activeTransfersCount: Int,
  onSelectTab: (MainTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    windowInsets = WindowInsets.navigationBars,
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = modifier
  ) {
    // 1. Chats
    NavigationBarItem(
      selected = currentTab == MainTab.CHATS,
      onClick = { onSelectTab(MainTab.CHATS) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.CHATS) Icons.Default.Chat else Icons.Outlined.Chat,
          contentDescription = Strings.get("chats", language),
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(text = Strings.get("chats", language), fontSize = 11.sp)
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = BthEmerald.copy(alpha = 0.2f),
        selectedIconColor = BthEmerald,
        selectedTextColor = BthEmerald
      ),
      modifier = Modifier.testTag("nav_tab_chats")
    )

    // 2. Groups
    NavigationBarItem(
      selected = currentTab == MainTab.GROUPS,
      onClick = { onSelectTab(MainTab.GROUPS) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.GROUPS) Icons.Default.Group else Icons.Outlined.Group,
          contentDescription = Strings.get("groups", language),
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(text = Strings.get("groups", language), fontSize = 11.sp)
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = BthEmerald.copy(alpha = 0.2f),
        selectedIconColor = BthEmerald,
        selectedTextColor = BthEmerald
      ),
      modifier = Modifier.testTag("nav_tab_groups")
    )

    // 3. Transfers Hub
    NavigationBarItem(
      selected = currentTab == MainTab.TRANSFERS,
      onClick = { onSelectTab(MainTab.TRANSFERS) },
      icon = {
        if (activeTransfersCount > 0) {
          BadgedBox(badge = { Badge { Text("$activeTransfersCount") } }) {
            Icon(
              imageVector = if (currentTab == MainTab.TRANSFERS) Icons.Default.Speed else Icons.Outlined.Speed,
              contentDescription = Strings.get("transfers", language),
              modifier = Modifier.size(24.dp)
            )
          }
        } else {
          Icon(
            imageVector = if (currentTab == MainTab.TRANSFERS) Icons.Default.Speed else Icons.Outlined.Speed,
            contentDescription = Strings.get("transfers", language),
            modifier = Modifier.size(24.dp)
          )
        }
      },
      label = {
        Text(text = Strings.get("transfers", language), fontSize = 11.sp)
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = BthEmerald.copy(alpha = 0.2f),
        selectedIconColor = BthEmerald,
        selectedTextColor = BthEmerald
      ),
      modifier = Modifier.testTag("nav_tab_transfers")
    )

    // 4. Calls
    NavigationBarItem(
      selected = currentTab == MainTab.CALLS,
      onClick = { onSelectTab(MainTab.CALLS) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.CALLS) Icons.Default.Call else Icons.Outlined.Call,
          contentDescription = Strings.get("calls", language),
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(text = Strings.get("calls", language), fontSize = 11.sp)
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = BthEmerald.copy(alpha = 0.2f),
        selectedIconColor = BthEmerald,
        selectedTextColor = BthEmerald
      ),
      modifier = Modifier.testTag("nav_tab_calls")
    )

    // 5. Radar
    NavigationBarItem(
      selected = currentTab == MainTab.RADAR,
      onClick = { onSelectTab(MainTab.RADAR) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.RADAR) Icons.Default.Radar else Icons.Outlined.Radar,
          contentDescription = Strings.get("radar", language),
          modifier = Modifier.size(24.dp)
        )
      },
      label = {
        Text(text = Strings.get("radar", language), fontSize = 11.sp)
      },
      colors = NavigationBarItemDefaults.colors(
        indicatorColor = BthEmerald.copy(alpha = 0.2f),
        selectedIconColor = BthEmerald,
        selectedTextColor = BthEmerald
      ),
      modifier = Modifier.testTag("nav_tab_radar")
    )
  }
}
