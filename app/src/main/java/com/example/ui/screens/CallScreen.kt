package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.model.CallState
import com.example.ui.theme.BthDarkBackground
import com.example.ui.theme.BthEmerald
import com.example.ui.theme.BthTeal
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SignalGreen
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun CallScreen(
  callSession: CallSession,
  language: AppLanguage,
  onAccept: () -> Unit,
  onEndCall: () -> Unit,
  onToggleMute: () -> Unit,
  onToggleSpeaker: () -> Unit,
  onToggleCameraFlip: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onEndCall() }

  val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
  val ringScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ringScale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(BthDarkBackground)
  ) {
    if (callSession.isVideo && callSession.state == CallState.CONNECTED) {
      // Video Call Viewport
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
            )
          )
      ) {
        // Video watermark
        Text(
          text = "P2P DIRECT VIDEO STREAM (HD 720p)",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = BthEmerald.copy(alpha = 0.7f)
          ),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 50.dp)
        )

        // Self View PiP (Picture in Picture)
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 70.dp, end = 16.dp)
            .size(width = 100.dp, height = 140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(2.dp, BthEmerald, RoundedCornerShape(12.dp))
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Cameraswitch,
              contentDescription = null,
              tint = BthEmerald,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (callSession.isCameraFront) "Front Cam" else "Rear Cam",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = Color.White
            )
          }
        }
      }
    }

    // Call details overlay
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 60.dp, bottom = 40.dp)
    ) {
      // Encryption badge
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(Color.Black.copy(alpha = 0.4f))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = BthEmerald,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = Strings.get("call_connected", language),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(30.dp))

      // Peer Avatar with pulsation
      Box(contentAlignment = Alignment.Center) {
        if (callSession.state == CallState.OUTGOING_RINGING || callSession.state == CallState.INCOMING_RINGING) {
          Box(
            modifier = Modifier
              .size((120 * ringScale).dp)
              .clip(CircleShape)
              .background(BthEmerald.copy(alpha = 0.15f))
          )
        }
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(BthTeal)
        ) {
          Text(
            text = callSession.peerName.take(2).uppercase(),
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = callSession.peerName,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = Color.White
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = when (callSession.state) {
          CallState.OUTGOING_RINGING -> Strings.get("outgoing_call", language)
          CallState.INCOMING_RINGING -> Strings.get("incoming_call", language)
          CallState.CONNECTED -> {
            val min = callSession.durationSeconds / 60
            val sec = callSession.durationSeconds % 60
            "${if (callSession.isVideo) "Video Call" else "Voice Call"} • ${String.format("%02d:%02d", min, sec)}"
          }
          CallState.ENDED -> "Call Ended"
        },
        style = MaterialTheme.typography.bodyLarge,
        color = if (callSession.state == CallState.CONNECTED) SignalGreen else Color.LightGray
      )

      Spacer(modifier = Modifier.weight(1f))

      // Bottom Call Controls
      Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
        modifier = Modifier
          .padding(horizontal = 24.dp)
          .navigationBarsPadding()
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 10.dp)
        ) {
          if (callSession.state == CallState.INCOMING_RINGING) {
            // Incoming Accept Button
            IconButton(
              onClick = onAccept,
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SignalGreen)
                .testTag("accept_call_button")
            ) {
              Icon(imageVector = Icons.Default.Call, contentDescription = "Accept", tint = Color.Black)
            }
          } else {
            // Mute Button
            IconButton(
              onClick = onToggleMute,
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (callSession.isMuted) Color.White else Color.DarkGray)
                .testTag("toggle_mute_button")
            ) {
              Icon(
                imageVector = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = Strings.get("mute", language),
                tint = if (callSession.isMuted) Color.Black else Color.White
              )
            }

            // Speaker Button
            IconButton(
              onClick = onToggleSpeaker,
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (callSession.isSpeakerOn) BthEmerald else Color.DarkGray)
                .testTag("toggle_speaker_button")
            ) {
              Icon(
                imageVector = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = Strings.get("speaker", language),
                tint = if (callSession.isSpeakerOn) Color.Black else Color.White
              )
            }

            if (callSession.isVideo) {
              // Flip Camera Button
              IconButton(
                onClick = onToggleCameraFlip,
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(Color.DarkGray)
                  .testTag("flip_camera_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Cameraswitch,
                  contentDescription = Strings.get("flip_camera", language),
                  tint = Color.White
                )
              }
            }
          }

          // Hangup / End Button
          IconButton(
            onClick = onEndCall,
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(ErrorRed)
              .testTag("end_call_button")
          ) {
            Icon(imageVector = Icons.Default.CallEnd, contentDescription = Strings.get("end_call", language), tint = Color.White)
          }
        }
      }
    }
  }
}
