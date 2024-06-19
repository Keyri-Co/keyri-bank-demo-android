package com.keyri.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keyri.demo.ui.theme.KeyriDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TODO: Finalize styling
        // TODO: Add navigation
        // TODO: Add icons
        // TODO: Add libraries: Keyri, biometrics, etc.
        // TODO: Create first screens

        setContent {
            KeyriDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                        // TODO: Move this text to separate fun, do styled component for next items
                        Text(
                            text = "Android",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        KeyriButton(Modifier, text = "Sign up", onClick = {

                        })
                    }
                }
            }
        }
    }
}

@Composable
fun KeyriButton(modifier: Modifier, enabled: Boolean = true, text: String, onClick: () -> Unit) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(text = text)
    }
}
