package com.launchad.skip.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.launchad.skip.ui.LaunchAdSkipTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class DemoSplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchAdSkipTheme {
                DemoAdScreen(
                    onSkip = {
                        startActivity(Intent(this, DemoContentActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun DemoAdScreen(onSkip: () -> Unit) {
    var showSkip by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2_000)
        showSkip = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF102E31))
            .systemBarsPadding()
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "测试广告",
                color = Color(0xFFF5B942),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "内置开屏页",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "用于验证辅助功能是否会在延迟出现的“跳过广告”控件上执行一次点击。",
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    color = Color(0xFF214F52),
                    shape = RoundedCornerShape(8.dp),
                ) {}
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxSize(),
                    color = Color(0xFF2B5D60),
                    shape = RoundedCornerShape(8.dp),
                ) {}
            }
        }

        if (showSkip) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5B942))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onSkip() })
                    }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("5s", color = Color(0xFF241600))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "跳过广告",
                    color = Color(0xFF241600),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
