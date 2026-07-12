package com.ergonomics.engine

import com.ergonomics.model.*
import kotlin.test.*

class CoreCorrectEngineTest {
    private val engine = CoreCorrectEngine()

    @Test
    fun `neutral needs no correction`() {
        val g = engine.compute(PostureState.NEUTRAL, 0.0, 0.0)
        assertEquals(0.0, g.pelvicTiltAdjustDeg)
        assertEquals(0.0, g.lateralShiftAdjustCm)
        assertTrue(g.verbalGuidance.contains("良好"))
    }

    @Test
    fun `anterior tilt corrects posterior`() {
        val g = engine.compute(PostureState.ANTERIOR_TILT, 30.0, 0.0)
        assertTrue(g.pelvicTiltAdjustDeg < 0, "应向后纠正")
        assertTrue(kotlin.math.abs(g.pelvicTiltAdjustDeg) <= 15.0)
    }

    @Test
    fun `lateral shift to the right`() {
        val g = engine.compute(PostureState.LATERAL_SHIFT, 0.0, 30.0)
        assertTrue(kotlin.math.abs(g.lateralShiftAdjustCm) > 0)
    }

    @Test
    fun `small imbalance below threshold`() {
        val g = engine.compute(PostureState.LATERAL_SHIFT, 0.0, 10.0)  // < 15%
        assertEquals(0.0, g.lateralShiftAdjustCm, 0.01)
    }

    @Test
    fun `pelvic clamp`() {
        val g = engine.compute(PostureState.ANTERIOR_TILT, 80.0, 0.0)
        assertTrue(kotlin.math.abs(g.pelvicTiltAdjustDeg) <= 15.0)
    }

    @Test
    fun `VoicePrompt for large pelvic shift`() {
        val g = engine.compute(PostureState.ANTERIOR_TILT, 40.0, 0.0)
        assertTrue(g.toVoicePrompt().contains("后倾"))
    }

    @Test
    fun `VoicePrompt for lateral shift`() {
        val g = engine.compute(PostureState.LATERAL_SHIFT, 0.0, 30.0)
        assertTrue(g.toVoicePrompt().contains("右"))
    }
}
