package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Profile
import com.example.data.model.ProgressLog
import com.example.ui.localization.LocalStrings
import com.example.ui.viewmodel.MathQuestCategory
import com.example.ui.viewmodel.MathQuestViewModel
import com.example.ui.viewmodel.QuestScreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Avatar visual config mappings
data class AvatarConfig(val key: String, val emoji: String, val color: Color, val labelKey: String)

val AVATARS_LIST = listOf(
    AvatarConfig("bunny", "🐰", Color(0xFFE8D7FF), "avatar_bunny"),
    AvatarConfig("fox", "🦊", Color(0xFFFFE0CC), "avatar_fox"),
    AvatarConfig("bear", "🐻", Color(0xFFD6F0FF), "avatar_bear"),
    AvatarConfig("lion", "🦁", Color(0xFFFFF4CC), "avatar_lion"),
    AvatarConfig("owl", "🦉", Color(0xFFD4F5E6), "avatar_owl"),
    AvatarConfig("panda", "🐼", Color(0xFFFEE1EB), "avatar_panda")
)

@Composable
fun MainGameApp(viewModel: MathQuestViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    // Establish current language in state
    val lang = activeProfile?.language ?: "sq"

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFBF0), Color(0xFFFFF0DC))
                    )
                )
        ) {
            when (val screen = currentScreen) {
                is QuestScreen.ProfileSelection -> {
                    ProfileSelectionScreen(viewModel)
                }
                is QuestScreen.Home -> {
                    HomeScreen(viewModel, activeProfile, lang)
                }
                is QuestScreen.ActiveAdventure -> {
                    AdventureScreen(viewModel, screen.questType, activeProfile, lang)
                }
                is QuestScreen.StickerShop -> {
                    StickerShopScreen(viewModel, activeProfile, lang)
                }
                is QuestScreen.ParentGate -> {
                    ParentGateScreen(viewModel, lang)
                }
                is QuestScreen.ParentDashboard -> {
                    ParentDashboardScreen(viewModel, lang)
                }
            }
        }
    }
}

// ==========================================
// 1. PROFILE SELECTION / ACCOUNT MANAGER
// ==========================================
@Composable
fun ProfileSelectionScreen(viewModel: MathQuestViewModel) {
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Hardcode language for selection before profile creation is completed
    var selectedGlobalLang by remember { mutableStateOf("sq") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Header
        Text(
            text = "MathQuest 🏝️",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF8A00),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = LocalStrings.get("app_tagline", selectedGlobalLang),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B5841),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Profiles Box Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .border(3.dp, Color(0xFFFFB356), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (profiles.isEmpty()) {
                    Spacer(modifier = Modifier.weight(0.3f))
                    Text(
                        text = "🎉",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = LocalStrings.get("no_profiles", selectedGlobalLang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B6C4F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.weight(0.7f))
                } else {
                    Text(
                        text = LocalStrings.get("change_profile", selectedGlobalLang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5A442E),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(profiles) { profile ->
                            val avatarMatch = AVATARS_LIST.find { it.key == profile.avatar }
                            val avatarBg = avatarMatch?.color ?: Color(0xFFF2F2F2)
                            val avatarEmoji = avatarMatch?.emoji ?: "👦"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_item_${profile.name}")
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFF9F0))
                                    .border(2.dp, Color(0xFFFFEAC5), RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.switchActiveProfile(profile.id)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = avatarEmoji, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A341E)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Badge(containerColor = Color(0xFFCEEEFF), contentColor = Color(0xFF003755)) {
                                            Text(
                                                text = "${profile.age} vjeç/yrs",
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                        Badge(containerColor = Color(0xFFFFE0B2), contentColor = Color(0xFFE65100)) {
                                            Text(
                                                text = profile.difficulty,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    }
                                }

                                // Active indicator
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Select child",
                                    tint = Color(0xFFFF8A00),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Create profile trigger
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .testTag("btn_open_create_profile")
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = LocalStrings.get("btn_add_child", selectedGlobalLang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Global quick selector prior to login
        Row(
            modifier = Modifier.padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { selectedGlobalLang = "sq" },
                label = { Text("🇦🇱 Shqip", fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedGlobalLang == "sq") Color(0xFFFFECC8) else Color.White
                )
            )
            AssistChip(
                onClick = { selectedGlobalLang = "en" },
                label = { Text("🇬🇧 English", fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedGlobalLang == "en") Color(0xFFFFECC8) else Color.White
                )
            )

            IconButton(
                onClick = { viewModel.prepareParentsGate() },
                modifier = Modifier
                    .background(Color(0xFFE6E2D5), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Parents hub shortcut",
                    tint = Color(0xFF535044)
                )
            }
        }
    }

    // FORM DIALOG TO CREATE NEW ACCOUNT
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            var childName by remember { mutableStateOf("") }
            var childAge by remember { mutableStateOf("") }
            var childDifficulty by remember { mutableStateOf("EASY") }
            var childAvatar by remember { mutableStateOf("bunny") }
            var childLang by remember { mutableStateOf(selectedGlobalLang) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color(0xFF4CAF50), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = LocalStrings.get("btn_create_profile", childLang),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF33691E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // TextField Name
                        OutlinedTextField(
                            value = childName,
                            onValueChange = { childName = it },
                            label = { Text(LocalStrings.get("enter_name", childLang)) },
                            placeholder = { Text(LocalStrings.get("enter_name_hint", childLang)) },
                            modifier = Modifier.fillMaxWidth().testTag("add_child_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF2E1C0C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF2E1C0C),
                                unfocusedTextColor = Color(0xFF2E1C0C),
                                focusedLabelColor = Color(0xFFE65100),
                                unfocusedLabelColor = Color(0xFF5A442E),
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFFDCD6C7),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // TextField Age
                        OutlinedTextField(
                            value = childAge,
                            onValueChange = { childAge = it },
                            label = { Text(LocalStrings.get("enter_age", childLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("add_child_age_input"),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF2E1C0C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF2E1C0C),
                                unfocusedTextColor = Color(0xFF2E1C0C),
                                focusedLabelColor = Color(0xFFE65100),
                                unfocusedLabelColor = Color(0xFF5A442E),
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFFDCD6C7),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Picker Label
                        Text(
                            text = LocalStrings.get("select_avatar", childLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF5A442E),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        // Avatar Selection Row Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AVATARS_LIST.forEach { avatar ->
                                val isSelected = childAvatar == avatar.key
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) avatar.color else Color(0xFFF7F4EB))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF8A00) else Color(0xFFDCD6C7),
                                            shape = CircleShape
                                        )
                                        .clickable { childAvatar = avatar.key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = avatar.emoji, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Difficulty Segment
                        Text(
                            text = LocalStrings.get("difficulty", childLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF5A442E),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            val isSelected = childDifficulty == diff
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { childDifficulty = diff }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { childDifficulty = diff },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (diff) {
                                        "EASY" -> LocalStrings.get("diff_easy", childLang)
                                        "MEDIUM" -> LocalStrings.get("diff_medium", childLang)
                                        else -> LocalStrings.get("diff_hard", childLang)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Choose Language
                        Text(
                            text = LocalStrings.get("language", childLang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF5A442E),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { childLang = "sq" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (childLang == "sq") Color(0xFFFF9100) else Color(0xFFE6E2D5)
                                )
                            ) {
                                Text("🇦🇱 Shqip", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { childLang = "en" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (childLang == "en") Color(0xFFFF9100) else Color(0xFFE6E2D5)
                                )
                            ) {
                                Text("🇬🇧 English", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (childName.isNotBlank()) {
                                    val age = childAge.toIntOrNull() ?: 6
                                    viewModel.createAndSelectProfile(
                                        name = childName,
                                        age = age,
                                        avatar = childAvatar,
                                        difficulty = childDifficulty,
                                        language = childLang
                                    )
                                    showCreateDialog = false
                                }
                            },
                            modifier = Modifier
                                .testTag("submit_profile_creation")
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(
                                "Krijo profilin / Start! 🚀",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. HOME SCREEN (THE CHILDREN PLAYROOM)
// ==========================================
@Composable
fun HomeScreen(viewModel: MathQuestViewModel, activeProfile: Profile?, lang: String) {
    if (activeProfile == null) return

    val currentAvatarConfig = AVATARS_LIST.find { it.key == activeProfile.avatar }
    val avatarBg = currentAvatarConfig?.color ?: Color(0xFFFFD180)
    val avatarEmoji = currentAvatarConfig?.emoji ?: "🐰"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular avatar bubble
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                        .border(2.dp, Color(0xFFFF9800), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatarEmoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Detail metadata labels
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${LocalStrings.get("welcome", lang)} ${activeProfile.name}! 👋",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4E342E)
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = Color(0xFFFF8F00), contentColor = Color.White) {
                            Text(text = "Lvl ${activeProfile.currentLevel}", fontWeight = FontWeight.Bold)
                        }
                        Badge(containerColor = Color(0xFFCE93D8), contentColor = Color(0xFF311B92)) {
                            Text(
                                text = when (activeProfile.difficulty) {
                                    "EASY" -> "Niveli 1"
                                    "MEDIUM" -> "Niveli 2"
                                    else -> "Niveli 3"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Balance of Gold Coins
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF6D6))
                        .border(1.5.dp, Color(0xFFFBC02D), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🪙", fontSize = 18.sp)
                    Text(
                        text = activeProfile.coins.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }

        // QUICK DIFFICULTY CHIP SELECTOR (Lehtë, Mesme, Vështirë)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            shape = RoundedCornerShape(16.dp),
            border = borderStrokeHelper(Color(0xFFFFB74D))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎮 " + (if (lang == "sq") "Zgjidh Nivelin e Aventurave:" else "Choose Adventure Level:"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE65100)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EASY", "MEDIUM", "HARD").forEach { level ->
                        val isSel = activeProfile.difficulty == level
                        val label = when (level) {
                            "EASY" -> if (lang == "sq") "Niveli 1 (Klasik)" else "Level 1 (Easy)"
                            "MEDIUM" -> if (lang == "sq") "Niveli 2 (Shumëzim)" else "Level 2 (Mult)"
                            else -> if (lang == "sq") "Niveli 3 (Pjesëtim)" else "Level 3 (Div)"
                        }
                        val bgColor = if (isSel) Color(0xFFFF9100) else Color.White
                        val textColor = if (isSel) Color.White else Color(0xFF5D4037)
                        val borderCol = if (isSel) Color(0xFFFF6D00) else Color(0xFFFFE0B2)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.5.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable { viewModel.updateDifficulty(level) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // QUICK TOGGLE SETTINGS UNDER TITLE CARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.navigateTo(QuestScreen.ProfileSelection) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B5841)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Switch profile logo", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(LocalStrings.get("change_profile", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Quick offline language toggles right on screen
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (lang == "sq") Color(0xFFFFECC8) else Color.White)
                        .border(1.5.dp, if (lang == "sq") Color(0xFFFF8A00) else Color(0xFFDCD6C7), CircleShape)
                        .clickable { viewModel.updateLanguage("sq") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🇦🇱", fontSize = 20.sp)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (lang == "en") Color(0xFFFFECC8) else Color.White)
                        .border(1.5.dp, if (lang == "en") Color(0xFFFF8A00) else Color(0xFFDCD6C7), CircleShape)
                        .clickable { viewModel.updateLanguage("en") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🇬🇧", fontSize = 20.sp)
                }
            }
        }

        // SCROLLABLE LIST OF ADVENTURES (CHAPTERS)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Addition Chapter
            item {
                AdventureChapterCard(
                    title = LocalStrings.get("quest_addition", lang),
                    description = LocalStrings.get("quest_addition_desc", lang),
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))),
                    category = MathQuestCategory.ADDITION,
                    emoji = "🌲🐰🍒",
                    onPlay = { viewModel.startQuest(MathQuestCategory.ADDITION) }
                )
            }

            // Subtraction Chapter
            item {
                AdventureChapterCard(
                    title = LocalStrings.get("quest_subtraction", lang),
                    description = LocalStrings.get("quest_subtraction_desc", lang),
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2))),
                    category = MathQuestCategory.SUBTRACTION,
                    emoji = "🌊🐠🫧",
                    onPlay = { viewModel.startQuest(MathQuestCategory.SUBTRACTION) }
                )
            }

            // Multiplication Chapter
            item {
                AdventureChapterCard(
                    title = LocalStrings.get("quest_multiplication", lang),
                    description = LocalStrings.get("quest_multiplication_desc", lang),
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF4DB6AC), Color(0xFF00796B))),
                    category = MathQuestCategory.MULTIPLICATION,
                    emoji = "🏔️🐻‍❄️💎",
                    onPlay = { viewModel.startQuest(MathQuestCategory.MULTIPLICATION) }
                )
            }

            // Division Chapter
            item {
                AdventureChapterCard(
                    title = LocalStrings.get("quest_division", lang),
                    description = LocalStrings.get("quest_division_desc", lang),
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFFBA68C8), Color(0xFF7B1FA2))),
                    category = MathQuestCategory.DIVISION,
                    emoji = "🛰️👽🌟",
                    onPlay = { viewModel.startQuest(MathQuestCategory.DIVISION) }
                )
            }
        }

        // BOTTOM NAVIGATION DOCK
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rewards sticker shop button
            Button(
                onClick = { viewModel.navigateTo(QuestScreen.StickerShop) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("btn_reward_shop")
                    .weight(1f)
                    .height(54.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = LocalStrings.get("my_stickers", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Parents checkpoint
            Button(
                onClick = { viewModel.prepareParentsGate() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("btn_parents")
                    .weight(1f)
                    .height(54.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = LocalStrings.get("parents_hub", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AdventureChapterCard(
    title: String,
    description: String,
    gradient: Brush,
    category: MathQuestCategory,
    emoji: String,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .border(2.5.dp, Color.White, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF1F8E9),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onPlay,
                        modifier = Modifier.testTag("btn_play_${category.name}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Play 🏝️️",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4E342E)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. ADVENTURE SCREEN (THE MATHEMATICAL GAME ENGINE)
// ==========================================
@Composable
fun AdventureScreen(
    viewModel: MathQuestViewModel,
    questType: MathQuestCategory,
    activeProfile: Profile?,
    lang: String
) {
    if (activeProfile == null) return

    val currentConfig = AVATARS_LIST.find { it.key == activeProfile.avatar }
    val avatarBg = currentConfig?.color ?: Color(0xFFFFF9C4)
    val avatarEmoji = currentConfig?.emoji ?: "🐰"

    val questions = viewModel.currentQuestions.value
    val currentIdx = viewModel.currentQuestionIndex.value
    val maxQuestions = questions.size

    val activeItem = questions.getOrNull(currentIdx)

    // Celebrity completed overlay boolean
    val isFinishVisible = viewModel.showQuestCompletedSummary.value
    val pointsTally = viewModel.activeQuestCoinsEarned.value
    val correctness = viewModel.correctAnswersInSession.value

    if (isFinishVisible) {
        QuestCompletedSummaryOverlay(
            viewModel = viewModel,
            correctCount = correctness,
            totalCount = maxQuestions,
            coinsCount = pointsTally,
            lang = lang
        )
        return
    }

    if (activeItem == null) {
        // Fallback safety
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER ROW WITH EXIT AND TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier
                    .background(Color(0xFFEAE5D8), CircleShape)
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = LocalStrings.get("quit_adventure", lang),
                    tint = Color(0xFF4E342E)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (questType) {
                        MathQuestCategory.ADDITION -> LocalStrings.get("quest_addition", lang)
                        MathQuestCategory.SUBTRACTION -> LocalStrings.get("quest_subtraction", lang)
                        MathQuestCategory.MULTIPLICATION -> LocalStrings.get("quest_multiplication", lang)
                        MathQuestCategory.DIVISION -> LocalStrings.get("quest_division", lang)
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A341E)
                )
                Text(
                    text = "${LocalStrings.get("question_counter", lang)} ${currentIdx + 1} / $maxQuestions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B7763)
                )
            }

            Spacer(modifier = Modifier.width(42.dp)) // Equalizer space
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PROGRESS DOTS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until maxQuestions) {
                val isCompleted = i < currentIdx
                val isActive = i == currentIdx
                val color = when {
                    isCompleted -> Color(0xFF4CAF50)
                    isActive -> Color(0xFFFF9100)
                    else -> Color(0xFFEAE5D8)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VISUAL ELEMENT COUNT DESIGN (THE KID CARTOON HELP CANVAS)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .border(2.5.dp, Color(0xFFFFE57F), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Renders the math visual objects dynamically
                MathVisualizer(
                    num1 = activeItem.num1,
                    num2 = activeItem.num2,
                    operation = activeItem.operation,
                    category = questType
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The numerical problem question displayed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${activeItem.num1} ${activeItem.operation} ${activeItem.num2} = ",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4E342E)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                        border = borderStrokeHelper(Color(0xFFB0BEC5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "?",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CONGRATULATIONS / TRIAL FEEDBACK DIALOG ON ANSWER SELECTION
        val isSelectionOverlay = viewModel.showQuizSuccessOverlay.value
        val isCorrectSubmitted = viewModel.isAnswerCorrectState.value

        if (isSelectionOverlay) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .border(
                        3.dp,
                        if (isCorrectSubmitted) Color(0xFF00C853) else Color(0xFFFF5252),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrectSubmitted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Character avatar jump bouncing representation
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatarEmoji, fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isCorrectSubmitted) {
                                LocalStrings.get("correct_answer", lang)
                            } else {
                                LocalStrings.get("wrong_answer", lang)
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCorrectSubmitted) Color(0xFF2E7D32) else Color(0xFFC62828),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (isCorrectSubmitted) {
                                viewModel.nextQuestionOrFinish(questType)
                            } else {
                                // Close and allow retrying
                                viewModel.showQuizSuccessOverlay.value = false
                                viewModel.selectedAnswerState.value = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_close_feedback_overlay"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrectSubmitted) Color(0xFF4CAF50) else Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isCorrectSubmitted) "Vazhdo ✨" else "Provo Përsëri 🔁",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // THREE INTERACTIVE ANSWERS SELECTION BUBBLES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activeItem.options.forEachIndexed { optIndex, choice ->
                    val colorTheme = when (optIndex) {
                        0 -> Color(0xFFEF5350) // Coral red candy bubble
                        1 -> Color(0xFF42A5F5) // Sky marine pill
                        else -> Color(0xFFFFCA28) // Banana honey gold shield
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("answer_option_$choice")
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .border(3.dp, colorTheme.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.submitAnswer(choice)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(74.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = choice.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

// THE CARTON MATH OBJECTS VISUALIZER WRAPPER
@Composable
fun MathVisualizer(
    num1: Int,
    num2: Int,
    operation: String,
    category: MathQuestCategory
) {
    val leftVisualEmoji = when (category) {
        MathQuestCategory.ADDITION -> "🍒"
        MathQuestCategory.SUBTRACTION -> "🫧"
        MathQuestCategory.MULTIPLICATION -> "💎"
        MathQuestCategory.DIVISION -> "🍪"
    }

    val rightVisualEmoji = when (category) {
        MathQuestCategory.ADDITION -> "🍌"
        MathQuestCategory.SUBTRACTION -> "🫧"
        MathQuestCategory.MULTIPLICATION -> "💎"
        MathQuestCategory.DIVISION -> "🍪"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (category) {
            MathQuestCategory.ADDITION -> {
                if (num1 <= 12 && num2 <= 12) {
                    // Renders num1 berries + num2 berries in parallel columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pile
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FlowGridHelper(count = num1, emoji = leftVisualEmoji)
                            Text("(${num1})", color = Color(0xFF455A64), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Text(
                            text = " + ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Right Pile
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FlowGridHelper(count = num2, emoji = rightVisualEmoji)
                            Text("(${num2})", color = Color(0xFF455A64), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Large numbers layout safety
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "🎈 ⭐ 🍭 ⭐ 🎈", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aventurë me numra të mëdhenj!",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Counting helper: Solve the big number math task!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            MathQuestCategory.SUBTRACTION -> {
                if (num1 <= 15) {
                    // For subtraction, draw num1 items, but draw a cross or darker scale on num2 of them with wrapped layout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val maxItemsPerRow = 5
                        val totalRows = (num1 + maxItemsPerRow - 1) / maxItemsPerRow
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (r in 0 until totalRows) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val startIdx = r * maxItemsPerRow + 1
                                    val endIdx = minOf(startIdx + maxItemsPerRow - 1, num1)
                                    for (i in startIdx..endIdx) {
                                        val crossedOut = i > (num1 - num2)
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = leftVisualEmoji,
                                                fontSize = 24.sp,
                                                modifier = Modifier.scale(if (crossedOut) 0.5f else 1f)
                                            )
                                            if (crossedOut) {
                                                Text(
                                                    text = "✖️",
                                                    color = Color.Red,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "(${num1} po heqim/minus ${num2})", 
                            color = Color(0xFF455A64), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "🎈 ✨ 🧸 ✨ 🎈", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Zgjidhe zbritjen e madhe!",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Subtract and find the correct answer!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            MathQuestCategory.MULTIPLICATION -> {
                if (num1 <= 6 && num2 <= 6) {
                    // For multiplication (A x B), draw A rows, each row containing B crystal items
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (row in 1..num1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📦 Grupi $row: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8D6E63))
                                for (col in 1..num2) {
                                    Text(text = leftVisualEmoji, fontSize = 22.sp)
                                }
                            }
                        }
                        Text(
                            text = "(${num1} grupe me nga ${num2})",
                            color = Color(0xFF455A64),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "🏔️ 💎 🐾 💎 🏔️", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shumëzim i madh fantastik!",
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Great multiplication adventure! Think fast!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            MathQuestCategory.DIVISION -> {
                if (num1 <= 20 && num2 <= 5) {
                    // For division, partition num1 space cookies, dividing into num2 separate dishes/compartments
                    val partitionSize = if (num2 > 0) num1 / num2 else num1
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (dish in 1..num2) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                    border = borderStrokeHelper(Color(0xFF81C784)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(text = "🛸", fontSize = 15.sp)
                                        for (item in 1..partitionSize) {
                                            Text(text = leftVisualEmoji, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = "($num1 pjata ndarë në $num2 pjata = $partitionSize)",
                            color = Color(0xFF455A64),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "🚀 🌟 🛸 🌟 🚀", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ndarje kozmike e numrave!",
                            color = Color(0xFF7B1FA2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Share the cookies equally in space! Solve the division!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Flow Grid drawer for items up to 10
@Composable
fun FlowGridHelper(count: Int, emoji: String) {
    Column {
        val rows = (count + 2) / 3
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val start = r * 3
                val remaining = (count - start).coerceIn(0, 3)
                for (i in 0 until remaining) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
    }
}

// CELEBRATIVE QUEST SUMMARY SCREEN overlay
@Composable
fun QuestCompletedSummaryOverlay(
    viewModel: MathQuestViewModel,
    correctCount: Int,
    totalCount: Int,
    coinsCount: Int,
    lang: String
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(28.dp))
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glitter Celebration Trophy Emojis
            Text(text = "👑🏆🎉", fontSize = 60.sp, modifier = Modifier.padding(bottom = 12.dp))

            Text(
                text = LocalStrings.get("congrats", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFE65100),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Score Banner Bubble
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Score: $correctCount / $totalCount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF57F17)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${LocalStrings.get("earned_coins", lang)} ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E342E)
                        )
                        Text(
                            text = "🪙 +$coinsCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Flawless performance badge unlock
            if (correctCount == totalCount) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = borderStrokeHelper(Color(0xFF81C784)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "💯", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = LocalStrings.get("earned_badge", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back Home Button
            Button(
                onClick = {
                    viewModel.showQuestCompletedSummary.value = false
                    viewModel.navigateTo(QuestScreen.Home)
                },
                modifier = Modifier
                    .testTag("btn_back_playroom")
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = LocalStrings.get("back_home", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

// ==========================================
// 4. STICKER REWARD STORES (VIRTUAL TOYS SHOP)
// ==========================================
@Composable
fun StickerShopScreen(viewModel: MathQuestViewModel, activeProfile: Profile?, lang: String) {
    if (activeProfile == null) return

    val totalCoins = activeProfile.coins
    // Unlocked lists parsed
    val unlockedStickers = activeProfile.unlockedStickers.split(",").filter { it.isNotBlank() }.toSet()
    val unlockedBadges = activeProfile.unlockedBadges.split(",").filter { it.isNotBlank() }.toSet()

    var activeTabOfShop by remember { mutableStateOf("SHOP") } // "SHOP", "REWARDS"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER NAV ROW
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier
                    .background(Color(0xFFEAE5D8), CircleShape)
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color(0xFF4E342E)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = LocalStrings.get("sticker_shop", lang),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4A341E),
                modifier = Modifier.weight(1f)
            )

            // Balance indicator gold cloud bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF6D6))
                    .border(1.5.dp, Color(0xFFFBC02D), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🪙", fontSize = 18.sp)
                Text(
                    text = totalCoins.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE65100)
                )
            }
        }

        // SEGMENT TABS SWITCHER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { activeTabOfShop = "SHOP" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTabOfShop == "SHOP") Color(0xFFFF9100) else Color(0xFFE2DDD1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = LocalStrings.get("lbl_shop_tab", lang),
                    fontWeight = FontWeight.Bold,
                    color = if (activeTabOfShop == "SHOP") Color.White else Color(0xFF4E342E)
                )
            }

            Button(
                onClick = { activeTabOfShop = "REWARDS" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTabOfShop == "REWARDS") Color(0xFFFF9100) else Color(0xFFE2DDD1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = LocalStrings.get("lbl_rewards_tab", lang),
                    fontWeight = FontWeight.Bold,
                    color = if (activeTabOfShop == "REWARDS") Color.White else Color(0xFF4E342E)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TAB VIEWER BODY
        if (activeTabOfShop == "SHOP") {
            // Stickers Toy shop items
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalStrings.get("sticker_shop_desc", lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6D5C50),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.stickerShopList) { toy ->
                        val isOwned = unlockedStickers.contains(toy.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toy_item_${toy.id}")
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFFFECC8), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = toy.emoji, fontSize = 38.sp)
                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = toy.id.replace("_", " ").uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4E342E)
                                )
                                Text(
                                    text = "${toy.cost} ${LocalStrings.get("coins", lang)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8F00)
                                )
                            }

                            if (isOwned) {
                                Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                                    Text(
                                        text = LocalStrings.get("unlocked", lang),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(6.dp)
                                        )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.purchaseSticker(toy)
                                    },
                                    modifier = Modifier.testTag("btn_buy_${toy.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${LocalStrings.get("buy_for", lang)} 🪙",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MERIT BADGES TALLY COLLECTION
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 Badget e Sukseseve të mia",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val badgeDetails = listOf(
                        Triple("first_step", "Hapi i Parë (First Step)", "Mirënjohje për krijimin e profilit! 🎈"),
                        Triple("addition_hero", "Heroi i Mbledhjes (Addition Hero)", "Zgjidhe aventurën pa asnjë gabim! 🍒"),
                        Triple("subtraction_hero", "Mjeshtri i Zbritjes (Subtraction King)", "Kalove zbritjen me saktësi 100%! 🫧"),
                        Triple("multiplication_hero", "Kampion Shumëzimi (Multiplication Pro)", "Plotësove kristalet e akullit të sakta! 💎"),
                        Triple("division_hero", "Gjeniu Kozmik (Division Alien)", "Ndarje e raketës plotësisht e përsosur! 🛸"),
                        Triple("silver_coin_collector", "Mbledhësi i Argjendtë (Coin Collector)", "Kursimi i parë prej rreth 100 monedhash! 🪙")
                    )

                    items(badgeDetails) { b ->
                        val unlocked = unlockedBadges.contains(b.first)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (unlocked) Color(0xFFFFFFFF) else Color(0xFFEDEBE7)
                            ),
                            border = borderStrokeHelper(if (unlocked) Color(0xFFFFD54F) else Color(0xFFE0E0E0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (unlocked) "⭐" else "🔒",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = b.second,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = if (unlocked) Color(0xFF3E2723) else Color(0xFF757575)
                                    )
                                    Text(
                                        text = b.third,
                                        fontSize = 11.sp,
                                        color = if (unlocked) Color(0xFF5D4037) else Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BACKPLATE CHEST VIEWER ALWAYS DISPLAYED IN REWARDS BOX
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
            border = borderStrokeHelper(Color(0xFFFFEE58))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📦 ${LocalStrings.get("lbl_toys_box", lang)} (${unlockedStickers.size} lodra/toys):",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFFF57F17)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (unlockedStickers.isEmpty()) {
                    Text(
                        text = "Arka është boshe. Bli lodrën e parë në dyqan! Empty box.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(unlockedStickers.toList()) { stId ->
                            val match = viewModel.stickerShopList.find { it.id == stId }
                            if (match != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = match.emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. THE PARENT SECURITY LOCKED ADMISSION GATE
// ==========================================
@Composable
fun ParentGateScreen(viewModel: MathQuestViewModel, lang: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .border(3.dp, Color(0xFFFF9100), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LocalStrings.get("parents_gate_title", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6D00)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = LocalStrings.get("parents_gate_desc", lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Authentication equation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${viewModel.parentChallengeNum1} × ${viewModel.parentChallengeNum2} = ",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF37474F)
                    )

                    OutlinedTextField(
                        value = viewModel.parentChallengeInput,
                        onValueChange = { viewModel.parentChallengeInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(90.dp)
                            .testTag("parent_gate_answer_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF2E1C0C),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF2E1C0C),
                            unfocusedTextColor = Color(0xFF2E1C0C),
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF78909C),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                if (viewModel.parentChallengeErrorMessage.isNotBlank()) {
                    Text(
                        text = viewModel.parentChallengeErrorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.goBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anulo 🚪", color = Color(0xFF37474F))
                    }

                    Button(
                        onClick = { viewModel.submitParentsGateAnswer() },
                        modifier = Modifier
                            .testTag("parent_gate_submit_btn")
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = LocalStrings.get("btn_submit", lang),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. PARENTS DASHBOARD (PROGRESS LOGGER COGNITION)
// ==========================================
@Composable
fun ParentDashboardScreen(viewModel: MathQuestViewModel, lang: String) {
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val rawLogs by viewModel.parentProgressLogs.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf<Profile?>(null) }
    var selectedProfileForStats by remember { mutableStateOf(activeProfile) }

    // Helper statistics derived
    val solvedCount = rawLogs.size
    val averageAccuracy = if (solvedCount > 0) {
        val totalCorrect = rawLogs.sumOf { it.correctCount }
        val totalQuestions = rawLogs.sumOf { it.totalCount }
        (totalCorrect * 100) / totalQuestions
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TOP CONTROL LEVERAGE
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(QuestScreen.Home) },
                modifier = Modifier
                    .background(Color(0xFFEAE5D8), CircleShape)
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Leave Parent dashboard",
                    tint = Color(0xFF4E342E)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = LocalStrings.get("parent_title", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3E2723)
                )
                Text(
                    text = LocalStrings.get("parent_subtitle", lang),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // HORIZONTAL CHIP FILTER OF CHILDREN ACCOUNT DETAILS
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fëmijët: ",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(end = 6.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(profiles) { pro ->
                    val isChosen = selectedProfileForStats?.id == pro.id
                    val animal = AVATARS_LIST.find { it.key == pro.avatar }?.emoji ?: "🐰"

                    AssistChip(
                        onClick = {
                            selectedProfileForStats = pro
                            // Reload reports list
                            viewModel.loadProgressLogsForDashboard()
                        },
                        label = { Text("${animal} ${pro.name}") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isChosen) Color(0xFFFFECC8) else Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val pro = selectedProfileForStats
        if (pro == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Asnjë fëmijë i regjistruar / No accounts")
            }
        } else {
            // STATS DETAILS CARDS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = borderStrokeHelper(Color(0xFFE2DDD1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Statistikat e ${pro.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5D4037)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ACCURACY CIRCULAR PROGRESS BOX
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$averageAccuracy%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFF57F17)
                                )
                                Text(
                                    text = LocalStrings.get("accuracy", lang),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // COMPLETED COUNT BOX
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = solvedCount.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = LocalStrings.get("quests_completed", lang),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DIFFICULTY TOGGLE BUTTONS
                    Text(
                        text = LocalStrings.get("lbl_difficulty", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { levelMode ->
                            val isSelected = pro.difficulty == levelMode
                            Button(
                                onClick = {
                                    // Change of difficulty directly from dashboard
                                    val updatedPro = pro.copy(difficulty = levelMode)
                                    viewModel.updateDifficulty(levelMode)
                                    selectedProfileForStats = updatedPro
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFFF9100) else Color(0xFFECEFF1)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = levelMode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF37474F)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // RECENT LOG EXERCISES LOGS
            Text(
                text = LocalStrings.get("history_logs", lang),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color(0xFF3E2723),
                modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = borderStrokeHelper(Color(0xFFE2DDD1))
            ) {
                if (rawLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = LocalStrings.get("no_logs", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rawLogs) { l ->
                            ProgressLogItemRow(l, lang)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DANGER ZONE DELETE PROFILE ACTION
            Button(
                onClick = { showDeleteConfirmDialog = pro },
                modifier = Modifier
                    .fillModifierWithTestTag("btn_delete_profile_${pro.name}")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = LocalStrings.get("btn_delete", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // CONFIRM DELETE ACCOUNT DIALOG
    val profileToDelete = showDeleteConfirmDialog
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Fshij llogarinë / Remove child Profile?", fontWeight = FontWeight.Black) },
            text = { Text(LocalStrings.get("delete_profile_warn", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProfile(profileToDelete)
                        showDeleteConfirmDialog = null
                        selectedProfileForStats = null
                    },
                    modifier = Modifier.testTag("confirm_delete_profile_action"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("VAZHDOS (Delete)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Prapë (Cancel)", color = Color.DarkGray)
                }
            }
        )
    }
}

@Composable
fun ProgressLogItemRow(log: ProgressLog, lang: String) {
    val dateString = try {
        val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    } catch (e: Exception) {
        "recent"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F7F3))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quest Icon
        val symbol = when (log.operationType) {
            "ADDITION" -> "➕"
            "SUBTRACTION" -> "➖"
            "MULTIPLICATION" -> "✖️"
            else -> "➗"
        }
        Text(text = symbol, fontSize = 20.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${log.operationType} (${log.difficulty})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF3E2723)
            )
            Text(
                text = "${LocalStrings.get("lbl_timestamp", lang)} $dateString",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        val accuracyPercent = (log.correctCount * 100) / log.totalCount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${log.correctCount} / ${log.totalCount}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (accuracyPercent > 70) Color(0xFF2E7D32) else Color(0xFFEF6C00)
            )
            Text(
                text = "$accuracyPercent%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}

// Border Stroke Helper wrapper for safe material rendering
private fun borderStrokeHelper(color: Color) = androidx.compose.foundation.BorderStroke(2.dp, color)

// Extension functions helper to prevent path or tag typings
private fun Modifier.fillModifierWithTestTag(tag: String): Modifier {
    return this.fillMaxWidth().testTag(tag)
}
