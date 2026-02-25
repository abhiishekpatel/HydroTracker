package com.example.hydrotracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydrotracker.ui.auth.AuthViewModel
import com.example.hydrotracker.ui.theme.HydroBlue
import com.example.hydrotracker.ui.theme.HydroBlueContainer
import com.example.hydrotracker.ui.theme.HydroCardBg
import com.example.hydrotracker.ui.theme.HydroTextPrimary
import com.example.hydrotracker.ui.theme.HydroTextSecondary
import com.example.hydrotracker.ui.theme.LightBackground

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    bottomPadding: Dp = 0.dp,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    val progress = if (uiState.dailyGoalMl > 0) {
        (uiState.todayTotalMl.toFloat() / uiState.dailyGoalMl).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding + 16.dp)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HydroTextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar + name row
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = HydroCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circle with initial
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(HydroBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = uiState.name.ifEmpty { "User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HydroTextPrimary
                    )
                    Text(
                        text = uiState.email,
                        fontSize = 13.sp,
                        color = HydroTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Today's intake card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = HydroCardBg,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Today's Intake", fontSize = 13.sp, color = HydroTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${uiState.todayTotalMl}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroBlue
                    )
                    Text(
                        text = " / ${uiState.dailyGoalMl} ml",
                        fontSize = 14.sp,
                        color = HydroTextSecondary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = HydroBlue,
                    trackColor = HydroBlueContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Edit profile section
        if (isEditing) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = HydroCardBg,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Edit Profile", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HydroTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.editName,
                        onValueChange = { profileViewModel.updateEditName(it) },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (uiState.editGoalMl > 0) uiState.editGoalMl.toString() else "",
                        onValueChange = { profileViewModel.updateEditGoal(it.toIntOrNull() ?: 0) },
                        label = { Text("Daily Goal (ml)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                profileViewModel.saveProfile()
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HydroBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action buttons
        Button(
            onClick = { isEditing = !isEditing },
            colors = ButtonDefaults.buttonColors(containerColor = HydroBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isEditing) "Cancel Edit" else "Edit Profile",
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                profileViewModel.logout { authViewModel.logout() }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Logout", fontWeight = FontWeight.SemiBold)
        }
    }
}
