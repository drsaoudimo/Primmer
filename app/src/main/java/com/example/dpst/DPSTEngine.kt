package com.example.dpst

import kotlin.math.*

data class StructuralFingerprint(
    val deltaQuadratic: Double,
    val binaryDensity: Double,
    val carryEnergy: Double,
    val structuralMetric: Double
) {
    override fun toString(): String =
        "Δ: %.2f | ρ: %.2f | Ec: %.2f | Sm: %.2f".format(deltaQuadratic, binaryDensity, carryEnergy, structuralMetric)
}

object DPSTEngine {
    fun getFingerprint(n: Long): StructuralFingerprint {
        val root = sqrt(n.toDouble())
        val delta = (root.roundToLong().toDouble().pow(2) - n).absoluteValue
        
        val binaryString = n.toString(2)
        val density = binaryString.count { it == '1' }.toDouble() / binaryString.length
        
        val carryEnergy = (n % 1024).toDouble() / 1024.0
        
        // Structural Metric heuristic
        val structuralMetric = (density * 100) + (1.0 / (delta + 1.0) * 10)
        
        return StructuralFingerprint(delta, density, carryEnergy, structuralMetric)
    }

    fun calculateDerivative(n: Long): Double {
        val f1 = getFingerprint(n)
        val f2 = getFingerprint(n + 1)
        
        return sqrt(
            (f1.deltaQuadratic - f2.deltaQuadratic).pow(2) +
            (f1.binaryDensity - f2.binaryDensity).pow(2) +
            (f1.carryEnergy - f2.carryEnergy).pow(2)
        )
    }
    
    fun factorize(n: Long): Pair<Long, Long>? {
        var d = 2L
        while (d * d <= n) {
            if (n % d == 0L) return Pair(d, n / d)
            d++
        }
        return null
    }
}
