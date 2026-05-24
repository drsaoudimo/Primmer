package com.aistudio.dpst.analyzer.zxcvbn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.dpst.analyzer.zxcvbn.dpst.DPSTEngine
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
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(stringResource(R.string.terminal_title), style = MaterialTheme.typography.labelSmall, color = Color(0xFF6366F1))
            Text(stringResource(R.string.subtitle), style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(stringResource(R.string.input_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF64748B)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cleanedInput = input.filter { it.isDigit() }
                if (cleanedInput.isEmpty()) {
                    result = context.getString(R.string.error_invalid_input)
                } else {
                    val n = try { BigInteger(cleanedInput) } catch (e: Exception) { null }
                    if (n != null) {
                        isLoading = true
                        scope.launch(Dispatchers.Default) {
                            try {
                                val factors = DPSTEngine.factorizeAll(n)
                                val res = if (factors.isNotEmpty()) factors.joinToString(" × ") { it.toString() } else "PRIME NUMBER"
                                withContext(Dispatchers.Main) {
                                    result = res
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    result = "Error: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }
                    } else {
                        result = context.getString(R.string.error_invalid_input)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.action_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        result?.let { res ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF10B981).copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.result_label), style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
                        Text(text = res, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFF1F5F9))
                    }
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Factors", res)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_to_clipboard), tint = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
