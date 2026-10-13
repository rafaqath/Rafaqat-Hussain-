package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileTransferSession
import com.example.model.PeerDevice
import com.example.model.TransferStatus
import com.example.ui.theme.BthCyan
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SpeedTurbo
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun TransfersTabScreen(
  transfers: List<FileTransferSession>,
  peers: List<PeerDevice>,
  language: AppLanguage,
  onSendTestFile: (sizeMB: Int) -> Unit,
  onClearCompleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeTransfer = transfers.firstOrNull { it.status == TransferStatus.TRANSFERRING }
  val currentSpeed = activeTransfer?.speedMBs ?: if (transfers.isNotEmpty()) 71.4f else 0f

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // 1. Live Speed Gauge Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(SpeedTurbo.copy(alpha = 0.2f))
              ) {
                Icon(
                  imageVector = Icons.Default.Bolt,
                  contentDescription = null,
                  tint = SpeedTurbo,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Lightning P2P Transfer Engine",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Wi-Fi Direct Socket • 0 Mobile Data",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = BthEmerald
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Speed Readout
          Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = String.format("%.1f", currentSpeed),
              style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp
              ),
              color = if (activeTransfer != null) SpeedTurbo else BthEmerald
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
              Text(
                text = "MB/s",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (activeTransfer != null) "Transferring Live" else "Peak Socket Throughput",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 2. High-Speed Benchmark Trigger Section
    item {
      Text(
        text = "Quick Send (25MB - 200MB in 1-2 Seconds)",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        QuickSendButton(
          sizeMB = 25,
          label = "25 MB",
          subtitle = "~0.4s",
          onClick = { onSendTestFile(25) },
          modifier = Modifier.weight(1f)
        )
        QuickSendButton(
          sizeMB = 50,
          label = "50 MB",
          subtitle = "~0.8s",
          onClick = { onSendTestFile(50) },
          modifier = Modifier.weight(1f)
        )
        QuickSendButton(
          sizeMB = 100,
          label = "100 MB",
          subtitle = "~1.5s",
          onClick = { onSendTestFile(100) },
          modifier = Modifier.weight(1f)
        )
        QuickSendButton(
          sizeMB = 200,
          label = "200 MB",
          subtitle = "~2.8s",
          onClick = { onSendTestFile(200) },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // 3. Transfers History Header
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Active & Recent Transfers (${transfers.size})",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (transfers.isNotEmpty()) {
          OutlinedButton(
            onClick = onClearCompleted,
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text("Clear", fontSize = 11.sp)
          }
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    // 4. Transfers List
    if (transfers.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Tap any quick send button above to test ultra high-speed transfer!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      items(transfers, key = { it.id }) { session ->
        TransferSessionItem(session = session)
        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}

@Composable
fun QuickSendButton(
  sizeMB: Int,
  label: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = BthEmerald.copy(alpha = 0.15f),
      contentColor = BthEmerald
    ),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp),
    modifier = modifier.testTag("quick_send_${sizeMB}mb")
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun TransferSessionItem(
  session: FileTransferSession,
  modifier: Modifier = Modifier
) {
  val progress = if (session.fileSizeBytes > 0)
    (session.transferredBytes.toFloat() / session.fileSizeBytes).coerceIn(0f, 1f)
  else 0f

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
    label = "progress"
  )

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("transfer_session_${session.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (session.status == TransferStatus.COMPLETED) SignalGreen.copy(alpha = 0.15f)
              else SpeedTurbo.copy(alpha = 0.15f)
            )
        ) {
          Icon(
            imageVector = when {
              session.status == TransferStatus.COMPLETED -> Icons.Default.CheckCircle
              session.isOutgoing -> Icons.Default.FileUpload
              else -> Icons.Default.FileDownload
            },
            contentDescription = null,
            tint = if (session.status == TransferStatus.COMPLETED) SignalGreen else SpeedTurbo,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = session.fileName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${session.peerName} • ${session.fileSizeBytes / (1024 * 1024)} MB",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status / Speed Badge
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = if (session.status == TransferStatus.COMPLETED) "Completed" else "${String.format("%.1f", session.speedMBs)} MB/s",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (session.status == TransferStatus.COMPLETED) SignalGreen else SpeedTurbo
            )
          )
          if (session.status == TransferStatus.TRANSFERRING && session.estimatedSecondsRemaining > 0) {
            Text(
              text = "${session.estimatedSecondsRemaining}s left",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { animatedProgress },
        color = if (session.status == TransferStatus.COMPLETED) SignalGreen else BthEmerald,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
      )
    }
  }
}
