package com.example.app.ui.comment.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.AsyncImage
import com.example.myapplication.domain.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputBar(
    replyingTo: CommentItem?,
    onCancelReply: () -> Unit,
    onSend: (String, Uri?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        selectedImageUri = it
    }

    BackHandler(enabled = showEmojiPicker || selectedImageUri != null) {
        if (showEmojiPicker) showEmojiPicker = false
        else selectedImageUri = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
    ) {
        replyingTo?.let {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F2F5)).padding(12.dp, 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đang trả lời ${it.comment.userName}", color = Color(0xFF1877F2), fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp).clickable { onCancelReply() })
            }
        }

        selectedImageUri?.let { uri ->
            Box(modifier = Modifier.padding(12.dp, 8.dp).size(120.dp).clip(RoundedCornerShape(12.dp))) {
                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp).clickable { selectedImageUri = null },
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
        }

        Box(modifier = Modifier.padding(12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F2F5)).padding(12.dp)) {
            if (text.isEmpty()) Text("Viết bình luận...", color = Color.Gray, fontSize = 14.sp)
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 500) text = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { if (it.isFocused) showEmojiPicker = false },
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                maxLines = 6
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selectedImageUri == null) {
                IconButton(onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Outlined.Image, null, tint = Color.Gray)
                }
            }

            IconButton(onClick = {
                if (showEmojiPicker) {
                    showEmojiPicker = false
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } else {
                    keyboardController?.hide()
                    showEmojiPicker = true
                }
            }) {
                Icon(Icons.Outlined.InsertEmoticon, null, tint = if (showEmojiPicker) Color(0xFF1877F2) else Color.Gray)
            }

            Spacer(Modifier.weight(1f))

            val canSend = text.isNotBlank() || selectedImageUri != null
            IconButton(
                onClick = {
                    val cleanedText = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
                    if (cleanedText.isNotBlank() || selectedImageUri != null) {
                        onSend(cleanedText, selectedImageUri)
                        text = ""
                        selectedImageUri = null
                    }
                    showEmojiPicker = false
                    keyboardController?.hide()
                },
                enabled = canSend
            ) {
                Icon(Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = if (canSend) Color(0xFF1877F2) else Color.LightGray)
            }
        }

        if (showEmojiPicker) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White)) {
                AndroidView(
                    factory = { ctx ->
                        EmojiPickerView(ctx).apply {
                            emojiGridRows = 5f
                            emojiGridColumns = 8
                            setOnEmojiPickedListener { text += it.emoji }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}