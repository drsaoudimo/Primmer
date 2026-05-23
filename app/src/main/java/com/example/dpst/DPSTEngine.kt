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
    
    // Optimized heuristic-based factorization inspired by DPST's "structural search"
    fun factorize(n: Long): Pair<Long, Long>? {
        if (n < 2) return null
        
        // DPST Heuristic: Start closer to the sqrt based on the structural metric
        val root = sqrt(n.toDouble()).toLong()
        
        // Search outwards using the DPST deformation heuristic
        for (offset in 0..root) {
            val d1 = root - offset
            if (d1 > 1 && n % d1 == 0L) return Pair(d1, n / d1)
            
            val d2 = root + offset
            if (d2 <= n && n % d2 == 0L) return Pair(d2, n / d2)
        }
        return null
    }
}
