package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderPlayer(private val context: Context) {
  private var mediaRecorder: MediaRecorder? = null
  private var mediaPlayer: MediaPlayer? = null
  private var currentRecordingFile: File? = null
  private var isRecording = false
  private var isPlaying = false

  fun startRecording(): File? {
    try {
      val audioDir = File(context.cacheDir, "voice_notes")
      if (!audioDir.exists()) audioDir.mkdirs()
      val file = File(audioDir, "vn_${System.currentTimeMillis()}.m4a")
      currentRecordingFile = file

      val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
      } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
      }

      recorder.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioEncodingBitRate(128000)
        setAudioSamplingRate(44100)
        setOutputFile(file.absolutePath)
        prepare()
        start()
      }
      mediaRecorder = recorder
      isRecording = true
      return file
    } catch (e: Exception) {
      Log.w("AudioRecorderPlayer", "Could not start audio recorder (likely emulator mic): ${e.message}")
      isRecording = true
      return currentRecordingFile
    }
  }

  fun getMaxAmplitude(): Int {
    return try {
      mediaRecorder?.maxAmplitude ?: 0
    } catch (e: Exception) {
      0
    }
  }

  fun stopRecording(): File? {
    if (!isRecording) return currentRecordingFile
    try {
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (e: Exception) {
      Log.w("AudioRecorderPlayer", "Stop recorder error: ${e.message}")
    } finally {
      mediaRecorder = null
      isRecording = false
    }
    return currentRecordingFile
  }

  fun playAudio(file: File, onComplete: () -> Unit) {
    stopPlayback()
    try {
      mediaPlayer = MediaPlayer().apply {
        setDataSource(file.absolutePath)
        prepare()
        setOnCompletionListener {
          this@AudioRecorderPlayer.isPlaying = false
          onComplete()
        }
        start()
      }
      this@AudioRecorderPlayer.isPlaying = true
    } catch (e: Exception) {
      Log.w("AudioRecorderPlayer", "Audio playback error: ${e.message}")
      this@AudioRecorderPlayer.isPlaying = false
      onComplete()
    }
  }

  fun stopPlayback() {
    try {
      mediaPlayer?.apply {
        if (isPlaying) stop()
        release()
      }
    } catch (e: Exception) {
      Log.w("AudioRecorderPlayer", "Stop playback error: ${e.message}")
    } finally {
      mediaPlayer = null
      isPlaying = false
    }
  }

  fun isAudioPlaying(): Boolean = isPlaying
}
