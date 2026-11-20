package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

@Composable
private fun ManageTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = Color(0xFFEDEEF0), modifier = Modifier.size(36.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = TextPrimary)
            }
        }
        Text(
            title,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(36.dp))
    }
}

@Composable
private fun ManageCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun ManageRow(title: String, value: String? = null, icon: ImageVector? = null, danger: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
        }
        Text(title, color = if (danger) Color(0xFFD93025) else TextPrimary, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        if (value != null) {
            Text(value, color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        if (!danger) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFC7CBD1), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ManageLabel(text: String) {
    Text(
        text,
        color = Color(0xFF8A9099),
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 8.dp),
    )
}

/** 官方「账号管理」页 1:1 复刻（静态版）。 */
@Composable
fun AccountManageScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).statusBarsPadding()) {
        ManageTopBar("账号管理", onBack)
        ManageLabel("账户")
        ManageCard {
            ManageRow("手机号", value = "193******64", icon = Icons.Filled.Phone)
        }
        Spacer(Modifier.height(16.dp))
        ManageCard {
            ManageRow("未成年人模式", value = "已关闭")
        }
        ManageLabel("安全")
        ManageCard {
            ManageRow("登录设备管理", icon = Icons.Filled.Smartphone)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "注销账号",
            color = Color(0xFFD93025),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { }.padding(vertical = 20.dp),
        )
    }
}

/** 官方「数据管理」页 1:1 复刻（静态版）。 */
@Composable
fun DataManageScreen(onBack: () -> Unit) {
    var optimize by remember { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).statusBarsPadding()) {
        ManageTopBar("数据管理", onBack)
        ManageCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("数据用于优化体验", color = TextPrimary, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = optimize,
                    onCheckedChange = { optimize = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF34C759)),
                )
            }
        }
        Text(
            "允许我们将你的对话内容用于优化 DeepSeek 的使用体验。我们保障你的数据隐私安全。",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
        ManageCard {
            ManageRow("我共享的链接")
        }
        Spacer(Modifier.height(16.dp))
        ManageCard {
            ManageRow("导出所有历史对话")
        }
        Spacer(Modifier.height(16.dp))
        ManageCard {
            ManageRow("删除所有历史对话", danger = true)
        }
    }
}


/** 官方「服务协议」页 1:1 复刻（列表项后续接 WebView/文档）。 */
@Composable
fun AgreementScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7)).statusBarsPadding()) {
        ManageTopBar("服务协议", onBack)
        ManageCard {
            ManageRow("用户协议")
            ManageRow("隐私政策")
            ManageRow("第三方信息共享清单")
            ManageRow("个人信息收集清单")
            ManageRow("应用权限")
        }
    }
}
