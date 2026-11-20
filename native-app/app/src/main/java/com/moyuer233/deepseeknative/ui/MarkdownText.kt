package com.moyuer233.deepseeknative.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyuer233.deepseeknative.ui.theme.TextPrimary

/** 轻量 Markdown：代码块 / 行内代码 / 粗体 / 斜体 / 标题 / 列表 / 链接。 */
@Composable
fun MarkdownText(text: String, color: Color = TextPrimary, scale: Float = 1f) {
    val parts = splitFences(text)
    Column {
        for (part in parts) {
            if (part.isCode) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F6F8),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                part.language.ifBlank { "code" },
                                color = Color(0xFF8A9099),
                                fontSize = 11.sp,
                            )
                        }
                        Text(
                            part.content.trimEnd(),
                            color = TextPrimary,
                            fontSize = (13 * scale).sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 19.sp,
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            } else {
                for (rawLine in part.content.split("\n")) {
                    val line = rawLine.trimEnd()
                    if (line.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        continue
                    }
                    when {
                        line.startsWith("### ") -> Text(inline(line.removePrefix("### ")), color = color, fontSize = (15 * scale).sp, fontWeight = FontWeight.SemiBold)
                        line.startsWith("## ") -> Text(inline(line.removePrefix("## ")), color = color, fontSize = (17 * scale).sp, fontWeight = FontWeight.SemiBold)
                        line.startsWith("# ") -> Text(inline(line.removePrefix("# ")), color = color, fontSize = (19 * scale).sp, fontWeight = FontWeight.SemiBold)
                        line.startsWith("- ") || line.startsWith("* ") -> Row {
                            Text("•", color = color, fontSize = (15 * scale).sp, modifier = Modifier.padding(end = 6.dp))
                            Text(inline(line.drop(2)), color = color, fontSize = (15 * scale).sp, lineHeight = (22 * scale).sp)
                        }
                        Regex("^\\d+\\. ").containsMatchIn(line) -> Text(inline(line), color = color, fontSize = (15 * scale).sp, lineHeight = (22 * scale).sp)
                        else -> Text(inline(line), color = color, fontSize = 15.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

private data class MdPart(val content: String, val isCode: Boolean, val language: String = "")

private fun splitFences(text: String): List<MdPart> {
    val parts = mutableListOf<MdPart>()
    val lines = text.split("\n")
    var buffer = StringBuilder()
    var codeBuffer = StringBuilder()
    var inCode = false
    var language = ""
    fun flushText() {
        if (buffer.isNotEmpty()) {
            parts.add(MdPart(buffer.toString(), false))
            buffer = StringBuilder()
        }
    }
    for (line in lines) {
        if (line.trimStart().startsWith("```")) {
            if (!inCode) {
                flushText()
                inCode = true
                language = line.trim().removePrefix("```").trim()
                codeBuffer = StringBuilder()
            } else {
                parts.add(MdPart(codeBuffer.toString(), true, language))
                inCode = false
            }
        } else if (inCode) {
            codeBuffer.append(line).append("\n")
        } else {
            buffer.append(line).append("\n")
        }
    }
    if (inCode) parts.add(MdPart(codeBuffer.toString(), true, language))
    flushText()
    if (parts.isEmpty()) parts.add(MdPart(text, false))
    return parts
}

/** 行内 **bold**、*italic*、`code`、[text](url)。 */
private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > 0) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > 0) {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFF0F1F3)))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i + 1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}
