package com.moyuer233.deepseeknative.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.moyuer233.deepseeknative.R
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.LocalAppColors


@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableStateOf(0) }
    var importedCount by remember { mutableStateOf(0) }
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
    val pages = listOf(
        Triple(NativeCopy.onboardingTitle1, NativeCopy.onboardingDesc1, 0),
        Triple(NativeCopy.onboardingTitle2, NativeCopy.onboardingDesc2, 1),
        Triple(NativeCopy.onboardingTitle3, NativeCopy.onboardingDesc3, 2),
        Triple(NativeCopy.onboardingTitle4, NativeCopy.onboardingDesc4, 3),
    )
    Column(
        modifier = Modifier.fillMaxSize().background(LocalAppColors.current.background).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (slideInHorizontally { it / 6 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 6 } + fadeOut())
            },
            label = "onboardingPage",
        ) { page ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (page == 0) {
                    Image(painter = painterResource(R.drawable.ic_deepseek_logo), contentDescription = null, modifier = Modifier.size(84.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    pages[page].first,
                    color = LocalAppColors.current.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    pages[page].second,
                    color = LocalAppColors.current.textSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 36.dp),
                )
            }
        }
        Spacer(Modifier.weight(1.2f))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 20.dp)) {
            pages.indices.forEach { i ->
                val dotSize by animateDpAsState(if (i == index) 8.dp else 6.dp, label = "dotSize")
                val dotColor by animateColorAsState(if (i == index) BrandBlue else Color(0xFFD5D8DE), label = "dotColor")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(dotSize)
                        .background(dotColor, CircleShape),
                )
            }
        }
        if (pages[index].third == 1) {
            OutlinedButton(
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
                    }
                )
            }
        }
        if (pages[index].third == 2) {
            OutlinedButton(
                onClick = { startOauth() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
            ) { Text(if (authorized) NativeCopy.authorized else NativeCopy.authorize) }
        }
        val primaryInteraction = remember { MutableInteractionSource() }
        val primaryPressed by primaryInteraction.collectIsPressedAsState()
        val primaryScale by animateFloatAsState(if (primaryPressed) 0.97f else 1f, label = "primaryScale")
        Button(
            onClick = { if (index < pages.lastIndex) index++ else onDone() },
            interactionSource = primaryInteraction,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp).scale(primaryScale),
        ) {
            Text(if (index < pages.lastIndex) NativeCopy.next else NativeCopy.start, fontSize = 16.sp)
        }
        TextButton(onClick = onDone, modifier = Modifier.navigationBarsPadding().padding(bottom = 24.dp)) {
            Text(NativeCopy.skip, color = LocalAppColors.current.textSecondary, fontSize = 14.sp)
        }
    }
}
