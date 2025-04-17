package com.inumaki.chouten.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.chouten.theme.ChoutenTheme

@Composable
fun TitleCard(title: String, description: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .padding(12.dp)
            .background(ChoutenTheme.colors.overlay, shape = RoundedCornerShape(12.dp))
            .border(0.5.dp, ChoutenTheme.colors.border, shape = RoundedCornerShape(12.dp))
    ) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            description,
            fontSize = 12.sp
        )
    }
}