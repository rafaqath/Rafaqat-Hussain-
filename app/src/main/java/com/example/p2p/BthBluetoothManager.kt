package com.example.p2p

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.example.model.ConnectionType
import com.example.model.PeerDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class BthBluetoothManager(
  private val context: Context,
  private val scope: CoroutineScope
) {
  companion object {
    private const val TAG = "BthBluetoothManager"
    private const val APP_NAME = "BTH_CHAT_P2P"
    val BTH_SERVICE_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
  }

  private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

  private val _discoveredDevices = MutableStateFlow<List<PeerDevice>>(emptyList())
  val discoveredDevices: StateFlow<List<PeerDevice>> = _discoveredDevices.asStateFlow()

  private val _isScanning = MutableStateFlow(false)
  val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

  private var serverSocket: BluetoothServerSocket? = null
  private var isListening = false

  private val receiver = object : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(c: Context?, intent: Intent?) {
      when (intent?.action) {
        BluetoothDevice.ACTION_FOUND -> {
          val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
          } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
          }
          val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (-65).toShort()).toInt()

          device?.let {
            val name = it.name ?: "Nearby P2P Device"
            val address = it.address
            val peer = PeerDevice(
              id = address,
              name = name,
              address = address,
              isBonded = it.bondState == BluetoothDevice.BOND_BONDED,
              isConnected = false,
              rssi = rssi,
              connectionType = ConnectionType.BLUETOOTH,
              statusMessage = "Bluetooth Available"
            )
            updateDiscoveredDevice(peer)
          }
        }
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
          _isScanning.value = false
        }
      }
    }
  }

  init {
    val filter = IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_FOUND)
      addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
    try {
      context.registerReceiver(receiver, filter)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to register receiver: ${e.message}")
    }
  }

  @SuppressLint("MissingPermission")
  fun loadPairedDevices(): List<PeerDevice> {
    val pairedList = mutableListOf<PeerDevice>()
    try {
      bluetoothAdapter?.bondedDevices?.forEach { device ->
        pairedList.add(
          PeerDevice(
            id = device.address,
            name = device.name ?: "Paired BTH Device",
            address = device.address,
            isBonded = true,
            isConnected = false,
            rssi = -50,
            connectionType = ConnectionType.BLUETOOTH,
            statusMessage = "Paired Peer"
          )
        )
      }
    } catch (e: Exception) {
      Log.w(TAG, "Cannot read paired devices: ${e.message}")
    }
    return pairedList
  }

  @SuppressLint("MissingPermission")
  fun startDiscovery() {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
      Log.w(TAG, "Bluetooth not enabled or not supported")
      return
    }
    try {
      if (bluetoothAdapter.isDiscovering) {
        bluetoothAdapter.cancelDiscovery()
      }
      val started = bluetoothAdapter.startDiscovery()
      _isScanning.value = started
    } catch (e: Exception) {
      Log.w(TAG, "Error starting discovery: ${e.message}")
      _isScanning.value = false
    }
  }

  @SuppressLint("MissingPermission")
  fun stopDiscovery() {
    try {
      bluetoothAdapter?.cancelDiscovery()
      _isScanning.value = false
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping discovery: ${e.message}")
    }
  }

  @SuppressLint("MissingPermission")
  fun startServerListener(onClientConnected: (BluetoothSocket) -> Unit) {
    if (isListening || bluetoothAdapter == null) return
    isListening = true
    scope.launch(Dispatchers.IO) {
      try {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, BTH_SERVICE_UUID)
        while (isListening) {
          try {
            val socket = serverSocket?.accept()
            if (socket != null) {
              onClientConnected(socket)
            }
          } catch (e: IOException) {
            break
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Server listener failed: ${e.message}")
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun connectToDevice(deviceAddress: String, onConnected: (BluetoothSocket) -> Unit, onError: (String) -> Unit) {
    scope.launch(Dispatchers.IO) {
      try {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
          onError("Device not found")
          return@launch
        }
        val socket = device.createRfcommSocketToServiceRecord(BTH_SERVICE_UUID)
        bluetoothAdapter.cancelDiscovery()
        socket.connect()
        onConnected(socket)
      } catch (e: Exception) {
        onError("Bluetooth connection failed: ${e.localizedMessage}")
      }
    }
  }

  private fun updateDiscoveredDevice(peer: PeerDevice) {
    val current = _discoveredDevices.value.toMutableList()
    val index = current.indexOfFirst { it.address == peer.address }
    if (index >= 0) {
      current[index] = peer
    } else {
      current.add(peer)
    }
    _discoveredDevices.value = current
  }

  fun unregister() {
    isListening = false
    try {
      serverSocket?.close()
      context.unregisterReceiver(receiver)
    } catch (e: Exception) {
      Log.w(TAG, "Cleanup error: ${e.message}")
    }
  }
}
