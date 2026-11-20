package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

/** 会话页骨架：顶栏 + 消息区 + 输入卡（消息流下一步接）。 */
@Composable
fun ChatScreen(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = TextPrimary)
            }
            Text(
                title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreHoriz, contentDescription = "更多", tint = TextPrimary)
            }
        }
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEAF0FE),
                        modifier = Modifier.widthIn(max = 300.dp),
                    ) {
                        Text("hello", color = TextPrimary, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                }
            }
            item {
                Spacer(Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { },
                ) {
                    Text("已思考（用时 1 秒）", color = Color(0xFF9AA0A9), fontSize = 14.sp)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9AA0A9), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Hello! How can I help you today?",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MessageAction(Icons.Filled.ContentCopy)
                    MessageAction(Icons.Filled.ThumbUp)
                    MessageAction(Icons.Filled.ThumbDown)
                    MessageAction(Icons.Filled.VolumeUp)
                    MessageAction(Icons.Filled.Share)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Filled.Refresh, contentDescription = "重新生成", tint = Color(0xFF9AA0A9), modifier = Modifier.size(20.dp))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF7F8FA),
                modifier = Modifier.weight(1f),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("发消息或按住说话", color = Color(0xFF9AA0A9), fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("深度思考", color = TextPrimary, fontSize = 13.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("智能搜索", color = TextPrimary, fontSize = 13.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.Add, contentDescription = "附件", tint = TextPrimary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Filled.GraphicEq, contentDescription = "语音", tint = TextPrimary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun MessageAction(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = Color(0xFF9AA0A9),
        modifier = Modifier.size(20.dp).clickable { },
    )
    Spacer(Modifier.width(20.dp))
}
