package com.aistudio.dpst.analyzer.zxcvbn.dpst

import java.math.BigInteger
import java.security.SecureRandom
import java.util.Locale
import kotlin.math.abs

data class StructuralFingerprint(
    val deltaQuadratic: String,
    val binaryDensity: Double,
    val carryEnergy: Double,
    val structuralMetric: Double
) {
    override fun toString(): String =
        String.format(Locale.US, "Δ: $deltaQuadratic | ρ: %.2f | Ec: %.2f | Sm: %.2f", deltaQuadratic, binaryDensity, carryEnergy, structuralMetric)
}

object DPSTEngine {
    private val rand = SecureRandom()

    fun getFingerprint(n: BigInteger): StructuralFingerprint {
        val root = n.sqrt()
        val delta = root.pow(2).subtract(n).abs()
        
        val binaryString = n.toString(2)
        val density = binaryString.count { it == '1' }.toDouble() / binaryString.length
        
        val carryEnergy = n.remainder(BigInteger.valueOf(1024)).toDouble() / 1024.0
        
        val structuralMetric = (density * 100) + (1.0 / (delta.toDouble() + 1.0) * 10)
        
        return StructuralFingerprint(delta.toString(), density, carryEnergy, structuralMetric)
    }

    fun calculateDerivative(n: BigInteger): Double {
        val f1 = getFingerprint(n)
        val f2 = getFingerprint(n.add(BigInteger.ONE))
        
        return abs(f1.structuralMetric - f2.structuralMetric)
    }

    // Modern factorization: Trial division for small factors, then Pollard's Rho
    fun factorizeAll(n: BigInteger): List<BigInteger> {
        if (n < BigInteger.valueOf(2)) return emptyList()
        val factors = mutableListOf<BigInteger>()
        var tempN = n
        
        // 1. Trial division for small factors
        var d = BigInteger.valueOf(2)
        while (d.multiply(d) <= tempN && d < BigInteger.valueOf(10000)) {
            while (tempN.remainder(d) == BigInteger.ZERO) {
                factors.add(d)
                tempN = tempN.divide(d)
            }
            d = d.add(BigInteger.ONE)
        }
        
        // 2. Pollard's Rho algorithm for larger factors
        if (tempN > BigInteger.ONE) {
            recursivePollardRho(tempN, factors)
        }
        
        return factors.sorted()
    }

    private fun recursivePollardRho(n: BigInteger, factors: MutableList<BigInteger>) {
        if (n == BigInteger.ONE) return
        if (n.isProbablePrime(10)) {
            factors.add(n)
            return
        }
        
        var d = pollardRho(n)
        recursivePollardRho(d, factors)
        recursivePollardRho(n.divide(d), factors)
    }

    private fun pollardRho(n: BigInteger): BigInteger {
        if (n.remainder(BigInteger.valueOf(2)) == BigInteger.ZERO) return BigInteger.valueOf(2)
        
        var x = BigInteger(n.bitLength(), rand).mod(n)
        var y = x
        var c = BigInteger(n.bitLength(), rand).mod(n)
        var d = BigInteger.ONE
        
        while (d == BigInteger.ONE) {
            x = (x.multiply(x).mod(n).add(c)).mod(n)
            y = (y.multiply(y).mod(n).add(c)).mod(n)
            y = (y.multiply(y).mod(n).add(c)).mod(n)
            d = x.subtract(y).abs().gcd(n)
            
            if (d == n) return pollardRho(n) // Failure, retry
        }
        return d
    }
}

// Extension to use native sqrt if available (API 33+), otherwise fallback
fun BigInteger.sqrt(): BigInteger {
    if (this < BigInteger.ZERO) throw IllegalArgumentException("Negative number")
    if (this == BigInteger.ZERO || this == BigInteger.ONE) return this
    var x = this.divide(BigInteger.valueOf(2))
    var prevX: BigInteger
    do {
        prevX = x
        x = this.divide(x).add(x).divide(BigInteger.valueOf(2))
    } while (x.subtract(prevX).abs() > BigInteger.ONE)
    return x
}
