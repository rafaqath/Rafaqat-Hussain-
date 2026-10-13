package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionType
import com.example.model.PeerDevice
import com.example.ui.theme.BthCyan
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.SignalGreen
import com.example.util.AppLanguage
import com.example.util.Strings
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarTabScreen(
  isScanning: Boolean,
  peers: List<PeerDevice>,
  discoveredDevices: List<PeerDevice>,
  language: AppLanguage,
  onStartScan: () -> Unit,
  onConnectPeer: (PeerDevice) -> Unit,
  modifier: Modifier = Modifier
) {
  val allPeers = (peers + discoveredDevices).distinctBy { it.id }

  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Radar Canvas Display
    item {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(240.dp)
          .padding(8.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2, size.height / 2)
          val radius = size.minDimension / 2

          // Concentric radar circles
          drawCircle(color = BthEmerald.copy(alpha = 0.15f), radius = radius, center = center, style = Stroke(width = 2f))
          drawCircle(color = BthEmerald.copy(alpha = 0.25f), radius = radius * 0.66f, center = center, style = Stroke(width = 1.5f))
          drawCircle(color = BthEmerald.copy(alpha = 0.35f), radius = radius * 0.33f, center = center, style = Stroke(width = 1.5f))

          // Crosshairs
          drawLine(
            color = BthEmerald.copy(alpha = 0.2f),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1f
          )
          drawLine(
            color = BthEmerald.copy(alpha = 0.2f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1f
          )

          // Rotating Sweep line
          val rad = Math.toRadians(rotation.toDouble())
          val sweepEnd = Offset(
            (center.x + radius * cos(rad)).toFloat(),
            (center.y + radius * sin(rad)).toFloat()
          )
          drawLine(
            brush = Brush.radialGradient(
              listOf(BthCyan, Color.Transparent),
              center = center,
              radius = radius
            ),
            start = center,
            end = sweepEnd,
            strokeWidth = 3f
          )

          // Center user blip
          drawCircle(color = BthEmerald, radius = 6f, center = center)

          // Nearby blips
          val blips = listOf(
            Offset(center.x + radius * 0.4f, center.y - radius * 0.3f),
            Offset(center.x - radius * 0.5f, center.y - radius * 0.4f),
            Offset(center.x + radius * 0.6f, center.y + radius * 0.2f),
            Offset(center.x - radius * 0.3f, center.y + radius * 0.5f)
          )
          blips.forEach { blip ->
            drawCircle(color = SignalGreen, radius = 5f, center = blip)
            drawCircle(color = SignalGreen.copy(alpha = 0.3f), radius = 10f, center = blip)
          }
        }

        // Radar center overlay info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "${allPeers.size}",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 24.sp
            ),
            color = BthEmerald
          )
          Text(
            text = "PEERS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
    }

    // 2. Scan Button & Status
    item {
      Button(
        onClick = onStartScan,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BthEmerald, contentColor = Color.Black),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("radar_scan_button")
      ) {
        if (isScanning) {
          CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(Strings.get("status_scanning", language), fontWeight = FontWeight.Bold)
        } else {
          Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(Strings.get("action_scan", language), fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
    }

    // 3. Discovered List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
      ) {
        Text(
          text = Strings.get("devices_found", language),
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    // 4. Device cards
    items(allPeers, key = { it.id }) { peer ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .testTag("discovered_device_${peer.id}")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                if (peer.connectionType == ConnectionType.WIFI_DIRECT) BthCyan.copy(alpha = 0.2f)
                else BthEmerald.copy(alpha = 0.2f)
              )
          ) {
            Icon(
              imageVector = if (peer.connectionType == ConnectionType.WIFI_DIRECT) Icons.Default.Wifi else Icons.Default.Bluetooth,
              contentDescription = null,
              tint = if (peer.connectionType == ConnectionType.WIFI_DIRECT) BthCyan else BthEmerald,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = peer.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.SignalCellularAlt,
                contentDescription = null,
                tint = SignalGreen,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${peer.rssi} dBm • ${peer.address}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = { onConnectPeer(peer) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (peer.isConnected) SignalGreen.copy(alpha = 0.2f) else BthEmerald,
              contentColor = if (peer.isConnected) SignalGreen else Color.Black
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("connect_peer_btn_${peer.id}")
          ) {
            Text(
              text = if (peer.isConnected) Strings.get("connected", language) else Strings.get("connect", language),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
