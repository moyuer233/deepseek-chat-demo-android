package com.moyuer233.deepseeknative.ui

import com.moyuer233.deepseeknative.NativeCopy

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors
import androidx.compose.ui.platform.LocalContext
import com.moyuer233.deepseeknative.data.ChatExport
import com.moyuer233.deepseeknative.data.DshAuth
import com.moyuer233.deepseeknative.data.PendingArchive
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.moyuer233.deepseeknative.R
import com.moyuer233.deepseeknative.ui.theme.BrandBlue


@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = remember {
        listOf(
            Triple(NativeCopy.onboardingTitle1, NativeCopy.onboardingDesc1, 0),
            Triple(NativeCopy.onboardingTitle2, NativeCopy.onboardingDesc2, 1),
            Triple(NativeCopy.onboardingTitle3, NativeCopy.onboardingDesc3, 2),
            Triple(NativeCopy.onboardingTitle4, NativeCopy.onboardingDesc4, 3),
        )
    }
    // pageCount 跟着 pages 走，增删页不会越界/空白
    val pagerState = rememberPagerState(pageCount = { pages.size })
    // 页面内容用 currentPage（滑动中就跟手），底部控件用 settledPage（停下才切），避免中途文案互换
    val index = pagerState.currentPage
    val settledPage = pagerState.settledPage
    var authorized by remember { mutableStateOf(DshAuth.token(context) != null) }
    var chatImportStatus by remember { mutableStateOf("") }
    var chatImporting by remember { mutableStateOf(false) }
    val chatImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val token = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra(ChatLoginActivity.EXTRA_TOKEN)
        } else null
        if (token.isNullOrEmpty()) {
            chatImporting = false
            chatImportStatus = NativeCopy.importCancelled
            return@rememberLauncherForActivityResult
        }
        chatImporting = true
        Thread {
            try {
                val exported = ChatExport.exportAndDownload(token, context.cacheDir)
                PendingArchive.save(context, exported.file.absolutePath, exported.fileName, exported.bytes)
                (context as? Activity)?.runOnUiThread {
                    chatImporting = false
                    chatImportStatus = NativeCopy.importReady
                    Toast.makeText(context, NativeCopy.importDoneToast, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    chatImporting = false
                    chatImportStatus = NativeCopy.importFailedPrefix + (e.message ?: "")
                }
            }
        }.start()
    }

    val oauthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authorized = result.resultCode == Activity.RESULT_OK
        if (authorized) {
            Toast.makeText(context, NativeCopy.authSuccessToast, Toast.LENGTH_SHORT).show()
        } else {
            val error = result.data?.getStringExtra(OAuthActivity.EXTRA_ERROR)
            Toast.makeText(context, NativeCopy.authFailedPrefix + (error ?: NativeCopy.authCancel), Toast.LENGTH_LONG).show()
        }
    }
    fun startOauth() {
        Thread {
            try {
                val url = DshAuth.init(context).getString("authorizeUrl")
                (context as? Activity)?.runOnUiThread {
                    oauthLauncher.launch(Intent(context, OAuthActivity::class.java).putExtra(OAuthActivity.EXTRA_URL, url))
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, NativeCopy.authInitFailedPrefix + (e.message ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    // 系统返回键/返回手势：先退回上一页引导，第一页时才交给系统（退出）
    androidx.activity.compose.BackHandler(enabled = settledPage > 0 && !pagerState.isScrollInProgress) {
        scope.launch { pagerState.animateScrollToPage(settledPage - 1) }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(LocalAppColors.current.background).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 用 Pager 占满中间整块空间：这样在页面任何位置横向滑动都能切页，
        // 而不是只有文字那一小块能滑（之前只 wrap content，用户几乎滑不动）
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            pageSpacing = 0.dp,
        ) { page ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    // 可滚动：横屏 / 大字体下内容超高时不会被裁掉、也不会压到下面的蓝点和按钮
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (page == 0) {
                        Image(painter = painterResource(R.drawable.ic_deepseek_logo), contentDescription = null, modifier = Modifier.size(84.dp))
                        Spacer(Modifier.height(12.dp))
                    }
                    // 标题与描述都吃满宽度再居中：只靠 wrap content 时，
                    // 多行文本的每行虽然 textAlign 居中，但整块在父容器里的位置会随内容宽度变化，看起来像没居中
                    Text(
                        pages[page].first,
                        color = LocalAppColors.current.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        pages[page].second,
                        color = LocalAppColors.current.textSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 20.dp)) {
            pages.indices.forEach { i ->
                // 每个点都占固定 14dp 的格子，只让内部圆点变大小/颜色，
                // 否则「当前页变大」会把整行挤得左右抖动，看起来就是「蓝点位置偏移」
                // 未激活点用主题边框色，深色模式下才不会接近白、和激活点混在一起
                val inactiveDot = LocalAppColors.current.border
                val dotColor by animateColorAsState(if (i == settledPage) BrandBlue else inactiveDot, label = "dotColor")
                Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(if (i == settledPage) 8.dp else 6.dp)
                            .background(dotColor, CircleShape),
                    )
                }
            }
        }
        // 固定高度的「可选按钮」槽位：没有按钮的页面也占同样空间，
        // 否则第 2/3 页多出一个按钮会把上面的蓝点整体顶上去，看起来就是「蓝点位置偏移」。
        // 用 min 高度而不是固定高度：状态文案较长时可以多占一行，不会被 Surface 裁掉
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (pages[settledPage].third) {
                1 -> OutlinedButton(
                    onClick = {
                        if (!chatImporting) {
                            chatImportLauncher.launch(Intent(context, ChatLoginActivity::class.java))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                ) {
                    Text(
                        when {
                            chatImporting -> NativeCopy.importingFromChat
                            chatImportStatus.isNotEmpty() -> chatImportStatus
                            else -> NativeCopy.importChats
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                2 -> OutlinedButton(
                    onClick = { startOauth() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (authorized) NativeCopy.authorized else NativeCopy.authorize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        val primaryInteraction = remember { MutableInteractionSource() }
        val primaryPressed by primaryInteraction.collectIsPressedAsState()
        val primaryScale by animateFloatAsState(if (primaryPressed) 0.97f else 1f, label = "primaryScale")
        Button(
            onClick = {
                // 用 targetPage 判断，连点时不会两次都指向同一页（第二下没反应）
                val target = pagerState.targetPage
                if (target < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(target + 1) }
                } else {
                    onDone()
                }
            },
            interactionSource = primaryInteraction,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp).scale(primaryScale),
        ) {
            Text(if (settledPage < pages.lastIndex) NativeCopy.next else NativeCopy.start, fontSize = 16.sp)
        }
        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
        ) {
            Text(
                NativeCopy.skip,
                color = LocalAppColors.current.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
