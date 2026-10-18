package com.launchad.skip.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.launchad.skip.R
import com.launchad.skip.ui.LaunchAdSkipTheme

class DemoContentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchAdSkipTheme {
                DemoDoneScreen(onFinish = ::finish)
            }
        }
    }
}

@Composable
private fun DemoDoneScreen(onFinish: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.asset_sparkles_3d),
                    contentDescription = null,
                    modifier = Modifier.size(104.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = "测试页已完成",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "返回首页可查看最近跳过记录。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = onFinish) {
                    Text("返回")
                }
            }
        }
    }
}
