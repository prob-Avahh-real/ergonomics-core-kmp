package com.ergonomics.model

import kotlin.test.*

class ModelTest {

    @Test
    fun `PostureQuality has 5 levels`() = assertEquals(5, PostureQuality.entries.size)

    @Test
    fun `PostureState has 6 states`() = assertEquals(6, PostureState.entries.size)

    @Test
    fun `FatigueLevel has 4 levels`() = assertEquals(4, FatigueLevel.entries.size)

    @Test
    fun `BodyType multipliers are monotonic`() {
        val m = BodyType.entries.map { it.loadMultiplier }
        for (i in 1 until m.size) assertTrue(m[i] >= m[i - 1])
    }

    @Test
    fun `CorrectionGuidance voice prompt for pelvic`() {
        val g = CorrectionGuidance(0, PostureState.ANTERIOR_TILT, pelvicTiltAdjustDeg = -5.0)
        assertTrue(g.toVoicePrompt().contains("后倾"))
    }

    @Test
    fun `CorrectionGuidance voice prompt for lateral`() {
        val g = CorrectionGuidance(0, PostureState.LATERAL_SHIFT, lateralShiftAdjustCm = 2.0)
        assertTrue(g.toVoicePrompt().contains("右"))
    }

    @Test
    fun `UserProfile defaults`() {
        val p = UserProfile()
        assertEquals(170, p.heightCm)
        assertEquals(65, p.weightKg)
        assertEquals(BodyType.AVERAGE, p.bodyType)
    }
}
