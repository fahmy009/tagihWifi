package com.example.tagihinternet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun DeveloperFooter() {
    val uriHandler = LocalUriHandler.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Padding diperkecil
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Muhammad Fahmy © 2026",
            style = MaterialTheme.typography.labelSmall, // Font diperkecil
            color = Color.Gray.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialIconCDN(
                "https://cdn-icons-png.flaticon.com/128/733/733547.png", // FB
                "https://www.facebook.com/muhammadfahmy009"
            )
            SocialIconCDN(
                "https://cdn-icons-png.flaticon.com/128/3046/3046121.png", // TikTok
                "https://www.tiktok.com/@muhammadfahmyan"
            )
            SocialIconCDN(
                "https://cdn-icons-png.flaticon.com/128/2111/2111463.png", // IG
                "https://www.instagram.com/fahmy082"
            )
            SocialIconCDN(
                "https://cdn-icons-png.flaticon.com/128/1384/1384060.png", // YT
                "https://youtube.com/@muhammadfahmy009?si=w8T8bgR2UJ7TkI9L"
            )
        }
    }
}

@Composable
fun SocialIconCDN(iconUrl: String, profileUrl: String) {
    val uriHandler = LocalUriHandler.current
    AsyncImage(
        model = iconUrl,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp) // Ukuran icon diperkecil dari 32dp ke 20dp
            .clickable { uriHandler.openUri(profileUrl) },
        contentScale = ContentScale.Fit
    )
}
