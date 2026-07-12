// Android BLE adapter via Kable — Ergonomics Core
package com.ergonomics.platform

import android.annotation.SuppressLint
import android.content.Context
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@SuppressLint("MissingPermission")
actual class BleAdapter(private val context: Context) {
    private val _connectedDevice = MutableStateFlow<String?>(null)
    actual val connectedDevice: StateFlow<String?> = _connectedDevice

    private var peripheral: Peripheral? = null
    private var connectionScope: CoroutineScope? = null
    private val connectMutex = Mutex()

    actual fun scan(): Flow<ScanResult> = Scanner {}
        .advertisements
        .map { ad -> ScanResult(ad.name, ad.address, ad.rssi) }
        .distinctUntilChanged { it.address }

    actual suspend fun connect(address: String) = connectMutex.withLock {
        // 原子化: 先断开已有连接，再建新连接
        disconnectInternal()
        val p = Peripheral(address)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch { p.connect(); _connectedDevice.value = address }
        peripheral = p
        connectionScope = scope
    }

    actual fun disconnect() {
        // 非 suspend 入口 — 用 runBlocking 快速获取锁 (断开操作极快)
        kotlinx.coroutines.runBlocking { connectMutex.withLock { disconnectInternal() } }
    }

    private fun disconnectInternal() {
        connectionScope?.cancel()
        connectionScope = null
        peripheral = null
        _connectedDevice.value = null
    }

    actual fun observeNotifications(serviceUuid: String, charUuid: String): Flow<ByteArray> {
        val p = peripheral ?: return emptyFlow()
        return p.observe(java.util.UUID.fromString(charUuid)).mapNotNull { it }
    }

    actual suspend fun writeCharacteristic(serviceUuid: String, charUuid: String, data: ByteArray) {
        peripheral?.write(java.util.UUID.fromString(charUuid), data)
    }
}

// PlatformInfo Android
actual object PlatformInfo {
    actual val name: String = "Android"
    actual val isAndroid: Boolean = true
    actual val isIos: Boolean = false
}
