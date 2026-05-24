package com.aistudio.dpst.analyzer.zxcvbn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.dpst.analyzer.zxcvbn.dpst.DPSTEngine
import com.aistudio.dpst.analyzer.zxcvbn.dpst.StructuralFingerprint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

val BackgroundDark = Color(0xFF0A0A0C)
val Indigo400 = Color(0xFF818CF8)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Slate100 = Color(0xFFF1F5F9)
val Slate500 = Color(0xFF64748B)
val Emerald500 = Color(0xFF10B981)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    secondary = Indigo400,
    background = BackgroundDark,
    surface = BackgroundDark,
    onPrimary = Slate100,
    onBackground = Slate100,
    onSurface = Slate100
)

@Composable
fun DPSTTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DPSTTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
                    DPSTAnalyzerScreen()
                }
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(32.dp)),
        color = Color(0x08FFFFFF),
        shape = RoundedCornerShape(32.dp),
        content = { Column(modifier = Modifier.padding(24.dp), content = content) }
    )
}

@Composable
fun DPSTAnalyzerScreen(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var fingerprint by remember { mutableStateOf<StructuralFingerprint?>(null) }
    var derivative by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text("DPST TERMINAL", style = MaterialTheme.typography.labelSmall, color = Indigo400)
            Text("Structural Anatomy Analyzer v1.2.0", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("STRUCTURAL NUMBER ENTRY [N]", style = MaterialTheme.typography.labelSmall, color = Indigo400.copy(alpha = 0.6f))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = Slate100),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val n = try { input.toBigInteger() } catch (e: Exception) { null }
                if (n != null) {
                    isLoading = true
                    scope.launch(Dispatchers.Default) {
                        try {
                            android.util.Log.d("DPST", "Starting calculation for $n")
                            val f = DPSTEngine.getFingerprint(n)
                            android.util.Log.d("DPST", "Fingerprint done: $f")
                            val d = DPSTEngine.calculateDerivative(n)
                            android.util.Log.d("DPST", "Derivative done: $d")
                            val factors = DPSTEngine.factorizeAll(n)
                            android.util.Log.d("DPST", "Factors done: $factors")
                            val res = if (factors.isNotEmpty()) factors.joinToString(" × ") { it.toString() } else "PRIME NUMBER"
                            withContext(Dispatchers.Main) {
                                fingerprint = f
                                derivative = d
                                result = res
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("DPST", "Error", e)
                            withContext(Dispatchers.Main) {
                                result = "Error: ${e.message}"
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("DECONSTRUCT FIELD", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show fingerprint if available
        fingerprint?.let {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("STRUCTURAL DERIVATIVE", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Text("%.4f".format(derivative), style = MaterialTheme.typography.headlineSmall, color = Indigo400)
                    }
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("FINGERPRINT", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Text(it.toString(), style = MaterialTheme.typography.bodySmall, color = Slate100, maxLines = 1)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Always try to show results
        result?.let { res ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Emerald500.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("FACTORS RESOLVED", style = MaterialTheme.typography.labelSmall, color = Emerald500)
                    Text(text = res, style = MaterialTheme.typography.headlineMedium, color = Slate100)
                }
            }
        }
    }
}
