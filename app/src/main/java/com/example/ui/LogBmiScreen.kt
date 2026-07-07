package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LogBmiScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val entries by viewModel.bmiEntries.collectAsState()

    // Retrieve default profile values
    val defaultFt = currentUser?.heightFt ?: 5
    val defaultIn = currentUser?.heightIn ?: 7

    // Try to pre-fill weight with their latest logged weight for quick convenience
    val defaultWeight = remember(entries) {
        entries.firstOrNull()?.weightKg?.toString() ?: ""
    }

    var weightInput by remember { mutableStateOf(defaultWeight) }
    var feetInput by remember { mutableStateOf(defaultFt.toString()) }
    var inchesInput by remember { mutableStateOf(defaultIn.toString()) }
    var noteInput by remember { mutableStateOf("") }
    
    var showSuccessBanner by remember { mutableStateOf(false) }
    var bannerMessage by remember { mutableStateOf("") }

    // Live calculation values
    val weightKg = weightInput.toDoubleOrNull() ?: 0.0
    val feet = feetInput.toIntOrNull() ?: 0
    val inches = inchesInput.toIntOrNull() ?: 0

    val heightInches = feet * 12 + inches
    val heightMeters = heightInches * 0.0254
    val liveBmi = if (heightMeters > 0.0 && weightKg > 0.0) {
        weightKg / (heightMeters * heightMeters)
    } else {
        0.0
    }

    // Classify BMI
    val (categoryName, categoryColor, categoryIcon, categoryDesc) = remember(liveBmi) {
        when {
            liveBmi <= 0.0 -> Quad(
                "Enter details", 
                MutedTextDark, 
                Icons.Default.HelpOutline, 
                "Fill in your height and weight above to view your BMI index instantly."
            )
            liveBmi < 18.5 -> Quad(
                "Underweight", 
                WarningOrange, 
                Icons.Default.TrendingDown, 
                "Your BMI is lower than the standard range. Consuming nutrient-rich meals can support physical fitness."
            )
            liveBmi < 25.0 -> Quad(
                "Healthy Weight", 
                SuccessGreen, 
                Icons.Default.CheckCircle, 
                "Excellent! You are in the optimal health range. Maintain your balanced nutrition and activity patterns!"
            )
            liveBmi < 30.0 -> Quad(
                "Overweight", 
                WarningOrange, 
                Icons.Default.TrendingUp, 
                "Your BMI is slightly above the standard range. Focus on sustainable, active routines and balanced foods."
            )
            else -> Quad(
                "Obesity Range", 
                AlertRed, 
                Icons.Default.Warning, 
                "Your BMI is in the obesity category. Consultation with a certified medical provider can support wellness targets."
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "BMI Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Success notification banner
        AnimatedVisibility(visible = showSuccessBanner) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = bannerMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Live calculation gauge card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Live BMI Index",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (liveBmi > 0) String.format("%.1f", liveBmi) else "--.-",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = if (liveBmi > 0) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (liveBmi > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }
                }

                Text(
                    text = categoryDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp).padding(top = 4.dp)
                )
            }
        }

        // Input controls section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Enter Log Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Weight Input in kg
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.toDoubleOrNull() != null || input.endsWith(".")) {
                            weightInput = input
                        }
                    },
                    label = { Text("Weight (kg)") },
                    placeholder = { Text("e.g. 72.5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_weight_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Height Inputs feet/inches
                Text(
                    text = "Height Measurement",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = feetInput,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 1) feetInput = it },
                        label = { Text("Feet (ft)") },
                        placeholder = { Text("5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("log_height_ft_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = inchesInput,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                val v = it.toIntOrNull()
                                if (v == null || v < 12) inchesInput = it
                            }
                        },
                        label = { Text("Inches (in)") },
                        placeholder = { Text("7") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("log_height_in_input"),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                // Custom log note
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { if (it.length <= 40) noteInput = it },
                    label = { Text("Optional short note") },
                    placeholder = { Text("e.g. Morning, Post-workout (max 40 chars)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_note_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Log CTA button
                val isValid = weightKg > 20.0 && weightKg < 350.0 && feet > 1 && feet < 10 && inches >= 0 && inches < 12
                Button(
                    onClick = {
                        if (isValid) {
                            viewModel.addBmiEntry(weightKg, feet, inches, noteInput.trim().ifEmpty { null })
                            bannerMessage = "Weight logged successfully! BMI: ${String.format("%.1f", liveBmi)}"
                            showSuccessBanner = true
                            noteInput = ""
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_log_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AddTask, contentDescription = null)
                        Text(
                            text = "Save Log & Progress",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

// Simple quad helper to clean code
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
