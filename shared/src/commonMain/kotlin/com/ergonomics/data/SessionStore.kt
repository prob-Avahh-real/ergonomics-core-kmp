package com.ergonomics.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/// 会话存储接口 — 跨产品通用
interface ISessionStore<T> {
    suspend fun add(item: T)
    suspend fun today(): List<T>
    suspend fun recent(count: Int): List<T>
    suspend fun count(): Int
    suspend fun clear()
}

/// 通用环形缓冲区存储 — 线程安全
///
/// add() 被 BLE 回调线程调用，today()/recent() 被 UI 采集协程调用，
/// Mutex 保证对内部环形缓冲区的所有读写串行化。
class InMemoryStore<T>(private val maxSize: Int = 70000) : ISessionStore<T> {
    private val buffer = ArrayDeque<Entry<T>>(maxSize)
    private val mutex = Mutex()

    private data class Entry<T>(val timestamp: Long, val item: T)

    override suspend fun add(item: T) = mutex.withLock {
        val ts = currentTimeMs()
        if (buffer.size >= maxSize) buffer.removeFirst()
        buffer.addLast(Entry(ts, item))
    }

    override suspend fun today(): List<T> = mutex.withLock {
        val dayStart = todayStartMs()
        buffer.filter { it.timestamp >= dayStart }.map { it.item }
    }

    override suspend fun recent(count: Int): List<T> = mutex.withLock {
        buffer.takeLast(count).map { it.item }
    }

    override suspend fun count(): Int = mutex.withLock { buffer.size }

    override suspend fun clear() = mutex.withLock { buffer.clear() }

    private fun todayStartMs(): Long {
        val now = currentTimeMs()
        return now - (now % 86_400_000L)
    }

    companion object {
        fun currentTimeMs(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}
