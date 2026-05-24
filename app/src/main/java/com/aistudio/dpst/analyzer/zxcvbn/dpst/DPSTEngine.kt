package com.aistudio.dpst.analyzer.zxcvbn.dpst

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

data class StructuralFingerprint(
    val deltaQuadratic: String,
    val binaryDensity: Double,
    val carryEnergy: Double,
    val structuralMetric: String
) {
    override fun toString(): String =
        "Δ: $deltaQuadratic | ρ: %.2f | Ec: %.2f | Sm: $structuralMetric".format(binaryDensity, carryEnergy)
}

object DPSTEngine {
    fun getFingerprint(n: BigInteger): StructuralFingerprint {
        val root = sqrtBigInt(n)
        val delta = root.pow(2).subtract(n).abs()
        
        val binaryString = n.toString(2)
        val density = binaryString.count { it == '1' }.toDouble() / binaryString.length
        
        val carryEnergy = n.remainder(BigInteger.valueOf(1024)).toDouble() / 1024.0
        
        val structuralMetric = (density * 100) + (1.0 / (delta.toDouble() + 1.0) * 10)
        
        return StructuralFingerprint(delta.toString(), density, carryEnergy, "%.2f".format(structuralMetric))
    }

    fun calculateDerivative(n: BigInteger): Double {
        val f1 = getFingerprint(n)
        val f2 = getFingerprint(n.add(BigInteger.ONE))
        
        return abs(f1.structuralMetric.toDouble() - f2.structuralMetric.toDouble())
    }

    private fun sqrtBigInt(n: BigInteger): BigInteger {
        if (n < BigInteger.ZERO) throw IllegalArgumentException("Negative number")
        if (n == BigInteger.ZERO || n == BigInteger.ONE) return n
        var x = n.divide(BigInteger.valueOf(2))
        var prevX: BigInteger
        do {
            prevX = x
            x = n.divide(x).add(x).divide(BigInteger.valueOf(2))
        } while (x.subtract(prevX).abs() > BigInteger.ONE)
        return x
    }
    
    fun factorizeAll(n: BigInteger): List<BigInteger> {
        if (n < BigInteger.valueOf(2)) return emptyList()
        val factors = mutableListOf<BigInteger>()
        var tempN = n
        
        // 1. تقسيم العوامل الأولية الصغيرة (تحسين السرعة)
        var d = BigInteger.valueOf(2)
        while (d.multiply(d) <= tempN) {
            while (tempN.remainder(d) == BigInteger.ZERO) {
                factors.add(d)
                tempN = tempN.divide(d)
            }
            d = d.add(BigInteger.ONE)
            
            // DPST Optimization: إضافة قيد زمني أو منطقي إذا لزم الأمر
            if (d > BigInteger.valueOf(10000) && tempN > BigInteger.ONE) {
                // إذا لم نجد عوامل صغيرة، نفترض أن المتبقي أولي أو يتطلب بحثاً بنيوياً
                break
            }
        }
        
        if (tempN > BigInteger.ONE) {
            factors.add(tempN)
        }
        
        return factors
    }
}
