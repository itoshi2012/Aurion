package com.aura.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.app.core.AuraController
import com.aura.app.safety.PermissionHelper
import com.aura.app.ui.theme.AuraColors
import com.aura.app.ui.theme.orbColorFor
import com.aura.app.voice.AuraLanguage

@Composable
fun AuraScreen(controller: AuraController) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) controller.onOrbTap() else controller.onPermissionDenied()
    }

    val onOrbTap: () -> Unit = {
        if (PermissionHelper.hasMicrophone(context)) {
            controller.onOrbTap()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val orbColor = orbColorFor(controller.orbState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF02040A), Color(0xFF0A1226), Color(0xFF02040A))
                )
            )
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        Text(
            text = "A U R A",
            color = AuraColors.Cyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AuraLanguage.values().forEach { lang ->
                LanguagePill(
                    label = lang.label,
                    selected = controller.language == lang,
                    onClick = { controller.setLanguage(lang) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrbTap
                ),
            contentAlignment = Alignment.Center
        ) {
            AuraOrb(state = controller.orbState, modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = controller.orbState.label,
            color = orbColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 5.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = controller.statusText,
            color = AuraColors.TextDim,
            fontSize = 13.sp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            if (controller.transcript.isNotEmpty()) {
                Text("YOU", color = AuraColors.TextDim, fontSize = 12.sp, letterSpacing = 3.sp)
                Text(controller.transcript, color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
            }
            if (controller.response.isNotEmpty()) {
                Text("AURA", color = AuraColors.Cyan, fontSize = 12.sp, letterSpacing = 3.sp)
                Text(controller.response, color = Color(0xFFCFF8FF), fontSize = 18.sp)
            }
        }

        Button(
            onClick = { controller.stop() },
            colors = ButtonDefaults.buttonColors(
                containerColor = AuraColors.Stop,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("STOP", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LanguagePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val accent = if (selected) AuraColors.Cyan else AuraColors.TextDim
    Box(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, accent, shape)
            .background(if (selected) AuraColors.Cyan.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(label, color = accent, fontSize = 14.sp)
    }
}
