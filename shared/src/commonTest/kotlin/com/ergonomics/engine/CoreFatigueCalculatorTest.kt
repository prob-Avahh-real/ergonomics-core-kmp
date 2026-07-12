package com.ergonomics.engine

import com.ergonomics.model.*
import kotlin.test.*

class CoreFatigueCalculatorTest {
    private val calc = CoreFatigueCalculator()

    @Test
    fun `perfect posture — zero fatigue`() {
        val r = calc.calculate(sittingDurationSeconds = 0, asymmetryIndex = 1.0, tiltDeviationDeg = 0.0)
        assertEquals(0.0, r.fatigueIndex, 0.1)
        assertEquals(FatigueLevel.COMFORTABLE, r.level)
    }

    @Test
    fun `moderate sitting produces mild fatigue`() {
        val r = calc.calculate(sittingDurationSeconds = 3600, asymmetryIndex = 0.8, tiltDeviationDeg = 10.0)
        assertTrue(r.fatigueIndex in 20.0..40.0, "应为轻度: ${r.fatigueIndex}")
        assertEquals(FatigueLevel.MILD, r.level)
    }

    @Test
    fun `severe exhaustion reaches critical`() {
        val r = calc.calculate(sittingDurationSeconds = 14400, asymmetryIndex = 0.3, tiltDeviationDeg = 40.0)
        assertEquals(FatigueLevel.SEVERE, r.level)
    }

    @Test
    fun `clamped to 0-100 range`() {
        val r = calc.calculate(sittingDurationSeconds = 86400, asymmetryIndex = 0.0, tiltDeviationDeg = 100.0)
        assertTrue(r.fatigueIndex in 0.0..100.0)
    }

    @Test
    fun `known conditions increase fatigue`() {
        val normal = calc.calculate(3600, 0.8, 10.0)
        val patient = calc.calculate(3600, 0.8, 10.0, knownConditions = listOf("DDD"))
        assertTrue(patient.fatigueIndex > normal.fatigueIndex)
    }

    @Test
    fun `cumulative strain never exceeds 200`() {
        val r = calc.calculate(500000, 0.0, 100.0)
        assertTrue(r.cumulativeStrain <= 200.0)
    }

    @Test
    fun `deterministic output`() {
        val r1 = calc.calculate(1800, 0.9, 5.0)
        val r2 = calc.calculate(1800, 0.9, 5.0)
        assertEquals(r1.fatigueIndex, r2.fatigueIndex)
    }
}
