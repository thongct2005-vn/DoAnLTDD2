package com.example.app.ui.profile.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.Profile
import com.example.app.ui.profile.edit.EditProfileActivity


@Composable
fun ProfileHeader(
    onBack:()->Unit,
    profile: Profile,
    onFollowerClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onUpdateProfileClick:(Profile)->Unit
) {
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    val primaryBlue = Color(0xFF3897F0)
    val grayButton = Color(0xFF3A3A3A)
    val borderColor = Color.White.copy(alpha = 0.5f)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar + Tên + Username
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = profile.avatarUrl ?: "https://i.pravatar.cc/150?u=${profile.id}",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(1.dp, textMain.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Chỉ hiện nút thêm ảnh nếu là chính mình
                    if (profile.isOwner) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Thêm ảnh đại diện",
                            tint = primaryBlue,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(bg, CircleShape)
                                .padding(2.dp)
                                .size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = profile.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textMain
                )

                Text(
                    text = "@${profile.username}",
                    fontSize = 13.sp,
                    color = textMain.copy(alpha = 0.6f)
                )
            }

            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatItem(profile.postCount.toString(), "bài viết", onClick = {})
                ProfileStatItem(profile.followerCount.toString(), "người theo dõi", onClick = { onFollowerClick(profile.id) })
                ProfileStatItem(profile.followingCount.toString(), "đang theo dõi", onClick = { onFollowingClick(profile.id) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== NÚT HÀNH ĐỘNG CHÍNH =====
        if (profile.isOwner) {
            // 1. Là chính mình → Chỉnh sửa hồ sơ
            OutlinedButton(

                onClick = { onUpdateProfileClick(profile) },

                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Medium)
            }

        } else {
            // 2. Là người khác → 3 trạng thái theo dõi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút Theo dõi / Đang theo dõi
                Button(
                    onClick = {
                        if (profile.isFollowing) {
                            onUnfollowClick()
                        } else {
                            onFollowClick()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (profile.isFollowing) grayButton else primaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (profile.isFollowing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Đang theo dõi", fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Theo dõi", fontWeight = FontWeight.Medium)
                        }
                    }
                }


                // Nút More (tin nhắn, chặn, báo cáo...)
                OutlinedButton(
                    onClick = { /* TODO: Mở menu hành động */ },
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Thêm hành động",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}