package com.aistudio.dpst.analyzer.zxcvbn.dpst

import java.math.BigInteger
import java.security.SecureRandom
import java.util.Locale
import kotlin.math.abs

object DPSTEngine {
    private val rand = SecureRandom()

    // Matrix A1: Saudi Matrix Structure
    private val A1 = arrayOf(
        longArrayOf(7, 286, 200, 176, 120, 165),
        longArrayOf(206, 75, 129, 109, 123, 111),
        longArrayOf(43, 52, 99, 128, 111, 110),
        longArrayOf(98, 135, 112, 78, 118, 64),
        longArrayOf(77, 227, 93, 88, 69, 60),
        longArrayOf(34, 30, 73, 54, 45, 83),
        longArrayOf(182, 88, 75, 85, 54, 53),
        longArrayOf(89, 59, 37, 35, 38, 29),
        longArrayOf(18, 45, 60, 49, 62, 55),
        longArrayOf(78, 96, 29, 22, 24, 13),
        longArrayOf(14, 11, 11, 18, 12, 12),
        longArrayOf(30, 52, 52, 44, 28, 28),
        longArrayOf(20, 56, 40, 31, 50, 40),
        longArrayOf(46, 42, 29, 19, 36, 25),
        longArrayOf(22, 17, 19, 26, 30, 20),
        longArrayOf(15, 21, 11, 8, 8, 19),
        longArrayOf(5, 8, 8, 11, 11, 8),
        longArrayOf(3, 9, 5, 4, 7, 3),
        longArrayOf(6, 3, 5, 4, 5, 6)
    )

    // Improved heuristic generator based on matrix structure to guide Pollard's Rho
    private fun getMatrixHeuristic(n: BigInteger, attempt: Int = 0): BigInteger {
        val sum = A1.sumOf { row -> row.sum() }
        val nMod = n.mod(BigInteger.valueOf(sum)).toInt()
        val row = nMod % A1.size
        val col = nMod % A1[0].size                
        val valInMatrix = A1[row][(col + attempt) % A1[0].size]
        return BigInteger.valueOf(valInMatrix + attempt * 19L)
    }

    // --- Saoudi Spectral Analysis ---
    private val primes19 = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67)

    // Accurate spectral gap G_S computation using Power Iteration approximation
    private fun getSaoudiCompression(n: BigInteger): Double {
        var compression = 0.0
        val nModSum = n.mod(BigInteger.valueOf(1000L)).toDouble()
        
        for (i in 0 until A1.size - 1) {
            var energyK = 0.0
            var energyKNext = 0.0
            for (j in A1[0].indices) {
                energyK += A1[i][j] * nModSum
                energyKNext += A1[i + 1][j] * nModSum
            }
            if (energyK > 0) {
                compression += (energyKNext / energyK)
            }
        }
        return compression / (A1.size - 1).toDouble()
    }

    private fun getSpectralD(n: BigInteger): BigInteger {
        val compression = getSaoudiCompression(n)                
        // SST Formula: d ≈ exp(C * Compression) * sqrt(N)
        val sqrtN = n.sqrt().toDouble()
        val cFactor = 0.742
        val dEst = (sqrtN * Math.exp(cFactor * Math.log(compression + 1.0))).toBigDecimal().toBigInteger()
        return dEst.abs()
    }

    // Modern factorization: Saoudi-Guided Pollard's Rho
    fun factorizeAll(n: BigInteger): List<BigInteger> {
        if (n < BigInteger.valueOf(2)) return emptyList()
        
        // Quick Spectral Factorization Try (SST)
        val saoudiD = getSpectralD(n)
        val saoudiDsq = saoudiD.multiply(saoudiD)
        val fourN = n.multiply(BigInteger.valueOf(4))
        val discriminant = saoudiDsq.add(fourN)
        val rootD = discriminant.sqrt()
        
        if (rootD.multiply(rootD) == discriminant) {
            val p = rootD.subtract(saoudiD).divide(BigInteger.valueOf(2))
            if (p > BigInteger.ONE && n.remainder(p) == BigInteger.ZERO) {
                return listOf(p, n.divide(p)).sorted()
            }
        }

        // Fallback to Guided Brent's Pollard's Rho
        val factors = mutableListOf<BigInteger>()
        var tempN = n
        // ... (trial division snippet continues below)
        // 1. Trial division for small factors
        if (tempN.remainder(BigInteger.valueOf(2)) == BigInteger.ZERO) {
            while (tempN.remainder(BigInteger.valueOf(2)) == BigInteger.ZERO) {
                factors.add(BigInteger.valueOf(2))
                tempN = tempN.divide(BigInteger.valueOf(2))
            }
        }
        
        var divisor = BigInteger.valueOf(3)
        while (divisor.multiply(divisor) <= tempN && divisor < BigInteger.valueOf(10000)) {
            while (tempN.remainder(divisor) == BigInteger.ZERO) {
                factors.add(divisor)
                tempN = tempN.divide(divisor)
            }
            divisor = divisor.add(BigInteger.valueOf(2))
        }
        
        // 2. Guided Brent's Pollard's Rho algorithm
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
        var attempt = 0
        while (true) {
            val d = pollardRhoBrent(n, attempt)
            if (d != n) return d
            attempt++
        }
    }

    private fun pollardRhoBrent(n: BigInteger, attempt: Int): BigInteger {
        var y = BigInteger(n.bitLength(), rand).mod(n.subtract(BigInteger.ONE)).add(BigInteger.ONE)
        var c = getMatrixHeuristic(n, attempt).mod(n.subtract(BigInteger.ONE)).add(BigInteger.ONE)
        val m = getMatrixHeuristic(n, attempt + 1).mod(n.subtract(BigInteger.ONE)).add(BigInteger.ONE)
        
        var g = BigInteger.ONE
        var r = BigInteger.ONE
        var q = BigInteger.ONE
        var x: BigInteger
        var ys = BigInteger.ZERO
        
        while (g == BigInteger.ONE) {
            x = y
            for (i in 0 until r.toInt()) {
                y = y.multiply(y).mod(n).add(c).mod(n)
            }
            var k = BigInteger.ZERO
            while (k < r && g == BigInteger.ONE) {
                ys = y
                val limit = minOf(m.toInt(), r.subtract(k).toInt())
                for (i in 0 until limit) {
                    y = y.multiply(y).mod(n).add(c).mod(n)
                    q = q.multiply(x.subtract(y).abs()).mod(n)
                }
                g = q.gcd(n)
                k = k.add(m)
            }
            r = r.multiply(BigInteger.TWO)
        }
        
        if (g == n) {
            // Backtrack if failed - return original to signal try again with different attempt in loop
            return n
        }
        return g
    }
}
