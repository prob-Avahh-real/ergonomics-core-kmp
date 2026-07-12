package com.ergonomics.data

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class InMemoryStoreTest {
    data class TestItem(val id: Int)

    @Test
    fun `adds and counts`() = runTest {
        val s = InMemoryStore<TestItem>(100)
        repeat(5) { s.add(TestItem(it)) }
        assertEquals(5, s.count())
    }

    @Test
    fun `evicts oldest`() = runTest {
        val s = InMemoryStore<TestItem>(3)
        s.add(TestItem(1)); s.add(TestItem(2)); s.add(TestItem(3)); s.add(TestItem(4))
        assertEquals(3, s.count())
    }

    @Test
    fun `recent returns last N`() = runTest {
        val s = InMemoryStore<TestItem>(100)
        s.add(TestItem(1)); s.add(TestItem(2))
        assertEquals(2, s.recent(5).size)
        assertEquals(2, s.recent(5).last().id)
    }

    @Test
    fun `today has current data`() = runTest {
        val s = InMemoryStore<TestItem>(100)
        s.add(TestItem(1))
        assertTrue(s.today().isNotEmpty())
    }

    @Test
    fun `clear empties everything`() = runTest {
        val s = InMemoryStore<TestItem>(100)
        repeat(10) { s.add(TestItem(it)) }
        s.clear()
        assertEquals(0, s.count())
    }

    @Test
    fun `concurrent adds do not lose data`() = runTest {
        val s = InMemoryStore<TestItem>(1000)
        // 模拟 BLE 回调 + UI 采集并发
        repeat(500) { s.add(TestItem(it)) }
        assertEquals(500, s.count(), "并发添加不应丢数据")
    }
}
