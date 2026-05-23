package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dpst.DPSTEngine
import com.example.dpst.StructuralFingerprint
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    DPSTAnalyzerScreen()
                }
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(32.dp)),
        color = Color(0x08FFFFFF),
        shape = RoundedCornerShape(32.dp),
        content = {
            Column(modifier = Modifier.padding(24.dp), content = content)
        }
    )
}

@Composable
fun DPSTAnalyzerScreen(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var fingerprint by remember { mutableStateOf<StructuralFingerprint?>(null) }
    var derivative by remember { mutableStateOf(0.0) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // ... (Header and Input remain same)
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text("DPST TERMINAL", style = MaterialTheme.typography.labelSmall, color = Indigo400)
            Text("Structural Anatomy Analyzer v1.1.0", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("STRUCTURAL NUMBER ENTRY [N]", style = MaterialTheme.typography.labelSmall, color = Indigo400.copy(alpha = 0.6f))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = Slate100),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo500,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val n = input.toLongOrNull()
                if (n != null) {
                    fingerprint = DPSTEngine.getFingerprint(n)
                    derivative = DPSTEngine.calculateDerivative(n)
                    val factors = DPSTEngine.factorize(n)
                    result = if (factors != null) "FACTORS: ${factors.first} × ${factors.second}" else "PRIME NUMBER"
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
        ) {
            Text("DECONSTRUCT FIELD", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Results Grid
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
    }
}
