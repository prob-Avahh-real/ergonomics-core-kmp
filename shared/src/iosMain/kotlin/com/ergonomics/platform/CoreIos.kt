// iOS BLE adapter via Kable — Ergonomics Core
package com.ergonomics.platform

import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual class BleAdapter {
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
        disconnectInternal()
        val p = Peripheral(address)
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch { p.connect(); _connectedDevice.value = address }
        peripheral = p
        connectionScope = scope
    }

    actual fun disconnect() {
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
        return p.observe(platform.Foundation.NSUUID(uuidString = charUuid))
            .mapNotNull { it?.toByteArray() }
    }

    actual suspend fun writeCharacteristic(serviceUuid: String, charUuid: String, data: ByteArray) {
        peripheral?.write(platform.Foundation.NSUUID(uuidString = charUuid), data)
    }
}

// PlatformInfo iOS
actual object PlatformInfo {
    actual val name: String = "iOS"
    actual val isAndroid: Boolean = false
    actual val isIos: Boolean = true
}
