package com.example.islam.presentation.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.util.BatteryOptimizationHelper

// ─── Dil seçenekleri ─────────────────────────────────────────────────────────

private val languageOptions = listOf(
    "tr" to "Türkçe",
    "en" to "English",
    "ar" to "العربية"
)

// ─── Ana ekran ────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val strings = LocalStrings.current

    val batteryExempted by produceState(initialValue = true, key1 = Unit) {
        value = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text       = strings.settings,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // ── Pil Optimizasyonu Uyarı Kartı ─────────────────────────────────────
        if (!batteryExempted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryOptimizationCard(
                titleText       = strings.batteryOptActive,
                buttonText      = strings.batteryOptFix,
                explanation     = BatteryOptimizationHelper.getExplanationText(),
                onFixClick      = {
                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                }
            )
        }

        // ── Konum Kartı ───────────────────────────────────────────────────────
        SettingsCard(title = strings.location) {
            OutlinedTextField(
                value         = state.cityInput,
                onValueChange = viewModel::onCityInputChange,
                label         = { Text(strings.city) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = state.countryInput,
                onValueChange = viewModel::onCountryInputChange,
                label         = { Text(strings.country) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = viewModel::saveLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.save)
            }
        }

        // ── Hesaplama Metodu Dropdown ─────────────────────────────────────────
        SettingsCard(title = strings.calculationMethodTitle) {
            Text(
                text  = strings.calculationMethodDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            SettingsDropdown(
                label       = strings.calculationMethodTitle,
                options     = strings.calcMethods,
                selectedId  = state.preferences.calculationMethod,
                onSelected  = { viewModel.setCalculationMethod(it) }
            )
        }

        // ── Mezhep / İkindi Vakti Dropdown ───────────────────────────────────
        SettingsCard(title = strings.schoolTitle) {
            Text(
                text  = strings.schoolDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            SettingsDropdown(
                label       = strings.schoolTitle,
                options     = strings.schoolOptions,
                selectedId  = state.preferences.school,
                onSelected  = { viewModel.setSchool(it) }
            )
        }

        // ── Dil Seçici ────────────────────────────────────────────────────────
        SettingsCard(title = strings.language) {
            LanguageDropdown(
                selectedCode = state.preferences.language,
                onSelected   = { viewModel.setLanguage(it) }
            )
        }

        // ── Bildirimler Kartı ─────────────────────────────────────────────────
        SettingsCard(title = strings.notificationsTitle) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(strings.azanNotifications)
                Switch(
                    checked         = state.preferences.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications
                )
            }
        }

        // ── Görünüm Kartı ─────────────────────────────────────────────────────
        SettingsCard(title = strings.appearance) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(strings.darkTheme)
                Switch(
                    checked         = state.preferences.darkTheme,
                    onCheckedChange = viewModel::setDarkTheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── Dil Dropdown ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedCode: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = languageOptions.firstOrNull { it.first == selectedCode }?.second ?: ""
    val strings = LocalStrings.current

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = selectedLabel,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(strings.language) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languageOptions.forEach { (code, name) ->
                DropdownMenuItem(
                    text    = { Text(name, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ─── Genel Dropdown Bileşeni ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    options: List<Pair<Int, String>>,
    selectedId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = selectedLabel,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text     = { Text(name, style = MaterialTheme.typography.bodyMedium) },
                    onClick  = {
                        onSelected(id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ─── Pil Optimizasyonu Uyarı Kartı ───────────────────────────────────────────

@Composable
private fun BatteryOptimizationCard(
    titleText: String,
    buttonText: String,
    explanation: String,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BatteryAlert,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text       = titleText,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick  = onFixClick,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = buttonText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Genel ayar kartı ────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
