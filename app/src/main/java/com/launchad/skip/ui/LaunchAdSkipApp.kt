package com.launchad.skip.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.launchad.skip.BuildConfig
import com.launchad.skip.R
import com.launchad.skip.data.DefaultRules
import com.launchad.skip.data.SkipEvent
import com.launchad.skip.data.SkipResult
import com.launchad.skip.data.SkipRule
import com.launchad.skip.demo.DemoSplashActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppScreen {
    HOME,
    RULES,
    HELP,
}

@Composable
fun LaunchAdSkipApp(
    serviceEnabled: Boolean,
    protectionEnabled: Boolean,
    rules: List<SkipRule>,
    events: List<SkipEvent>,
    onProtectionEnabledChange: (Boolean) -> Unit,
    onAddRule: (String) -> Unit,
    onRuleEnabledChange: (SkipRule, Boolean) -> Unit,
    onDeleteRule: (SkipRule) -> Unit,
    onResetRules: () -> Unit,
) {
    var screenName by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }
    val screen = AppScreen.valueOf(screenName)

    BackHandler(enabled = screen != AppScreen.HOME) {
        screenName = AppScreen.HOME.name
    }

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            serviceEnabled = serviceEnabled,
            protectionEnabled = protectionEnabled,
            rules = rules,
            events = events,
            onProtectionEnabledChange = onProtectionEnabledChange,
            onOpenRules = { screenName = AppScreen.RULES.name },
            onOpenHelp = { screenName = AppScreen.HELP.name },
        )

        AppScreen.RULES -> RulesScreen(
            rules = rules,
            onBack = { screenName = AppScreen.HOME.name },
            onAddRule = onAddRule,
            onRuleEnabledChange = onRuleEnabledChange,
            onDeleteRule = onDeleteRule,
            onResetRules = onResetRules,
        )

        AppScreen.HELP -> HelpScreen(
            onBack = { screenName = AppScreen.HOME.name },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    serviceEnabled: Boolean,
    protectionEnabled: Boolean,
    rules: List<SkipRule>,
    events: List<SkipEvent>,
    onProtectionEnabledChange: (Boolean) -> Unit,
    onOpenRules: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    val context = LocalContext.current
    val enabledRules = rules.count(SkipRule::enabled)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "开屏跳过",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onOpenHelp) {
                        Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = "设置说明")
                    }
                    IconButton(onClick = onOpenRules) {
                        Icon(Icons.Rounded.Tune, contentDescription = "高级设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            item {
                ServiceStatus(
                    serviceEnabled = serviceEnabled,
                    protectionEnabled = protectionEnabled,
                    ruleCount = enabledRules,
                    onOpenSettings = { openAccessibilitySettings(context) },
                    onToggle = { enabled ->
                        onProtectionEnabledChange(enabled)
                        if (enabled && !serviceEnabled) {
                            openAccessibilitySettings(context)
                        }
                    },
                )
            }

            item {
                SectionHeading(
                    title = "最近跳过",
                    trailing = if (events.isEmpty()) null else "${events.size} 条",
                )
            }

            if (events.isEmpty()) {
                item {
                    EmptyHistory()
                }
            } else {
                item {
                    Column {
                        val visibleEvents = events.take(8)
                        visibleEvents.forEachIndexed { index, event ->
                            SkipEventRow(event)
                            if (index < visibleEvents.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 44.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "规则与跳过记录仅保存在本机",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceStatus(
    serviceEnabled: Boolean,
    protectionEnabled: Boolean,
    ruleCount: Int,
    onOpenSettings: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val running = serviceEnabled && protectionEnabled
    val targetContainer = when {
        running -> MaterialTheme.colorScheme.primaryContainer
        serviceEnabled -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val targetForeground = when {
        running -> MaterialTheme.colorScheme.onPrimaryContainer
        serviceEnabled -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(durationMillis = 220),
        label = "service container",
    )
    val foreground by animateColorAsState(
        targetValue = targetForeground,
        animationSpec = tween(durationMillis = 220),
        label = "service foreground",
    )

    Surface(
        color = container,
        contentColor = foreground,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = foreground.copy(alpha = 0.11f),
                        contentColor = foreground,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = when {
                                running -> "运行中"
                                serviceEnabled -> "已暂停"
                                else -> "需要授权"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = when {
                            running -> "自动跳过正在运行"
                            serviceEnabled -> "自动跳过已暂停"
                            else -> "需要开启辅助功能"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            running -> "自动处理应用启动页，无需逐个选择"
                            serviceEnabled -> "打开右侧开关即可恢复"
                            else -> "只需授权一次，之后自动生效"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Crossfade(
                    targetState = serviceEnabled,
                    modifier = Modifier.size(92.dp),
                    animationSpec = tween(durationMillis = 180),
                    label = "service art",
                ) { isRunning ->
                    Image(
                        painter = painterResource(
                            if (isRunning) R.drawable.asset_shield_3d else R.drawable.asset_warning_3d,
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Text(
                text = "全部应用 · $ruleCount 条规则",
                style = MaterialTheme.typography.labelLarge,
                color = foreground.copy(alpha = 0.82f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "自动跳过",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            running -> "开关已打开"
                            serviceEnabled -> "开关已关闭"
                            else -> "授权后自动生效"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = foreground.copy(alpha = 0.76f),
                    )
                }
                Switch(
                    checked = running,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = foreground,
                        checkedTrackColor = foreground.copy(alpha = 0.38f),
                        uncheckedThumbColor = foreground.copy(alpha = 0.72f),
                        uncheckedTrackColor = foreground.copy(alpha = 0.16f),
                        uncheckedBorderColor = foreground.copy(alpha = 0.45f),
                    ),
                )
            }

            if (!serviceEnabled) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = foreground,
                        contentColor = container,
                    ),
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("开启辅助功能")
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SkipEventRow(event: SkipEvent) {
    val clicked = event.result == SkipResult.CLICKED
    val blockedJump = event.result == SkipResult.BLOCKED_JUMP
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (clicked || blockedJump) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (clicked || blockedJump) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        clicked -> Icons.Rounded.CheckCircle
                        blockedJump -> Icons.Rounded.Shield
                        else -> Icons.Rounded.WarningAmber
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.appLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatTime(event.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = when {
                    clicked -> "已点击“${event.matchedText}”"
                    blockedJump -> "已返回，拦下摇一摇跳转"
                    else -> "已识别“${event.matchedText}”，控件不可点击"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.History, contentDescription = null)
            }
        }
        Column {
            Text("还没有跳过记录", style = MaterialTheme.typography.bodyLarge)
            Text(
                "运行一次内置测试后会显示在这里",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RulesScreen(
    rules: List<SkipRule>,
    onBack: () -> Unit,
    onAddRule: (String) -> Unit,
    onRuleEnabledChange: (SkipRule, Boolean) -> Unit,
    onDeleteRule: (SkipRule) -> Unit,
    onResetRules: () -> Unit,
) {
    val context = LocalContext.current
    var keyword by rememberSaveable { mutableStateOf("") }
    val enabledCount = rules.count(SkipRule::enabled)
    val submitRule = {
        if (keyword.isNotBlank()) {
            onAddRule(keyword)
            keyword = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("高级设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onResetRules) {
                        Icon(Icons.Rounded.Restore, contentDescription = "恢复默认规则")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .imePadding(),
        ) {
            Text(
                text = "默认规则已经可以直接使用。",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "识别到摇一摇广告并突然跳出时，会自动尝试返回。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "共 ${rules.size} 条 · 启用 $enabledCount 条",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("添加跳过关键词") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitRule() }),
                )
                FilledIconButton(
                    onClick = submitRule,
                    enabled = keyword.isNotBlank(),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "添加规则")
                }
            }
            Text(
                text = "按完整词组匹配，英文不会被拆成单个单词",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (rules.isEmpty()) {
                    item {
                        EmptyRules()
                    }
                } else {
                    items(rules, key = { "${it.keyword}-${it.builtin}" }) { rule ->
                        RuleRow(
                            rule = rule,
                            onEnabledChange = { onRuleEnabledChange(rule, it) },
                            onDelete = { onDeleteRule(rule) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                        )
                    }
                }
                if (BuildConfig.DEBUG) {
                    item {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(context, DemoSplashActivity::class.java),
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("运行内置测试")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("设置说明") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = "首次使用完成一次授权即可，之后不需要每次打开应用。",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                HelpStep(
                    number = "1",
                    title = "开启辅助功能",
                    body = "进入系统设置 > 辅助功能 > 已下载的应用，找到“开屏跳过”并打开。授权后可返回首页。",
                )
            }

            item {
                Button(
                    onClick = { openAccessibilitySettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("打开辅助功能设置")
                }
            }

            item {
                HelpStep(
                    number = "2",
                    title = "允许后台运行",
                    body = "vivo / OriginOS：设置 > 电池 > 后台耗电管理，允许后台高耗电；再到 i管家 > 应用管理 > 权限管理 > 自启动中允许“开屏跳过”。最近任务中可锁定应用，退出时按返回或 Home，不要上滑强制结束，否则部分系统会收回无障碍权限。",
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { openAppSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("打开应用权限与电池设置")
                    }
                    OutlinedButton(
                        onClick = { openBatteryOptimizationSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("打开系统电池优化设置")
                    }
                }
            }

            item {
                HelpStep(
                    number = "3",
                    title = "暂停或彻底关闭",
                    body = "不想使用时，在首页关闭“自动跳过”总开关。若需要收回系统权限，再到辅助功能设置中关闭“开屏跳过”。",
                )
            }

            item {
                Text(
                    text = "建议只保留一个广告跳过或自动点击类辅助服务，同时开启多个可能造成重复点击。不同系统版本的设置名称可能略有差异。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HelpStep(
    number: String,
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(50),
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: SkipRule,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (rule.enabled) 1f else 0.58f)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = rule.keyword,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (rule.builtin) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "内置",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Text(
                text = if (rule.enabled) "正在参与匹配" else "已暂停",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = rule.enabled,
            onCheckedChange = onEnabledChange,
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Rounded.DeleteOutline,
                contentDescription = "删除 ${rule.keyword}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyRules() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "没有启用的规则",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "添加关键词或恢复默认规则",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date(timestamp))
}

@Preview(
    name = "首页",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    backgroundColor = 0xFFF5F7F3,
)
@Composable
private fun HomePreview() {
    LaunchAdSkipTheme {
        HomeScreen(
            serviceEnabled = true,
            protectionEnabled = true,
            rules = DefaultRules.all,
            events = listOf(
                SkipEvent(
                    packageName = "com.example.video",
                    appLabel = "示例视频",
                    matchedText = "跳过广告",
                    timestamp = System.currentTimeMillis(),
                    result = SkipResult.CLICKED,
                ),
            ),
            onProtectionEnabledChange = {},
            onOpenRules = {},
            onOpenHelp = {},
        )
    }
}

@Preview(
    name = "规则",
    widthDp = 390,
    heightDp = 844,
    showBackground = true,
    backgroundColor = 0xFFF5F7F3,
)
@Composable
private fun RulesPreview() {
    LaunchAdSkipTheme {
        RulesScreen(
            rules = DefaultRules.all,
            onBack = {},
            onAddRule = {},
            onRuleEnabledChange = { _, _ -> },
            onDeleteRule = {},
            onResetRules = {},
        )
    }
}
