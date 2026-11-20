package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.BrandBlue
import com.moyuer233.deepseeknative.ui.theme.TextPrimary
import com.moyuer233.deepseeknative.ui.theme.TextSecondary

/** 官方「手机验证码登录」页 1:1 复刻。 */
@Composable
fun PhoneLoginScreen(onBack: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val phoneFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        phoneFocus.requestFocus()
        keyboard?.show()
    }
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = Color(0xFFF2F3F5), modifier = Modifier.size(40.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "返回", tint = TextPrimary)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("Contact us", color = TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Log in with phone",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Field(
            value = phone,
            onValueChange = { phone = it },
            hint = "Phone number",
            prefix = "+86",
            modifier = Modifier.padding(horizontal = 24.dp).focusRequester(phoneFocus),
            keyboardType = KeyboardType.Phone,
        )
        Spacer(Modifier.height(12.dp))
        Field(
            value = code,
            onValueChange = { code = it },
            hint = "Code",
            modifier = Modifier.padding(horizontal = 24.dp),
            keyboardType = KeyboardType.Number,
            trailing = {
                Text("Send code", color = BrandBlue, fontSize = 15.sp, modifier = Modifier.padding(end = 16.dp))
            },
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { },
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
        ) {
            Text("Log in", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailing: (@Composable () -> Unit)? = null,
) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color(0xFFF2F3F5), modifier = modifier.fillMaxWidth().height(56.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 20.dp)) {
            if (prefix != null) {
                Text(prefix, color = TextPrimary, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(hint, color = Color(0xFF9AA0A9), fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Text("|", color = Color(0xFFD9DCE1), fontSize = 14.sp)
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
    }
}
