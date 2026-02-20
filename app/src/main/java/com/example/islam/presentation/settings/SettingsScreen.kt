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
import com.example.islam.core.util.BatteryOptimizationHelper

// ─── Hesaplama metodu listesi (Aladhan method ID → kullanıcı dostu ad) ────────

private val calculationMethods = listOf(
    2  to "ISNA (Kuzey Amerika)",
    3  to "MWL (Dünya Müslümanlar Birliği)",
    5  to "Egypt (Mısır Genel İdaresi)",
    13 to "Diyanet İşleri Başkanlığı"
)

// ─── Mezhep / İkindi yöntemi listesi ─────────────────────────────────────────

private val schoolOptions = listOf(
    0 to "Şafii (Standart)",
    1 to "Hanefi (İkindi geç başlar)"
)

// ─── Ana ekran ────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            text       = "Ayarlar",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // ── Pil Optimizasyonu Uyarı Kartı ─────────────────────────────────────
        if (!batteryExempted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryOptimizationCard(
                explanation = BatteryOptimizationHelper.getExplanationText(),
                onFixClick  = {
                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                }
            )
        }

        // ── Konum Kartı ───────────────────────────────────────────────────────
        SettingsCard(title = "Konum") {
            OutlinedTextField(
                value         = state.cityInput,
                onValueChange = viewModel::onCityInputChange,
                label         = { Text("Şehir") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = state.countryInput,
                onValueChange = viewModel::onCountryInputChange,
                label         = { Text("Ülke") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = viewModel::saveLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }

        // ── Hesaplama Metodu Dropdown ─────────────────────────────────────────
        SettingsCard(title = "Hesaplama Metodu") {
            Text(
                text  = "Namaz vakti hesaplama standardı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))

            SettingsDropdown(
                label       = "Hesaplama Metodu",
                options     = calculationMethods,
                selectedId  = state.preferences.calculationMethod,
                onSelected  = { viewModel.setCalculationMethod(it) }
            )
        }

        // ── Mezhep / İkindi Vakti Dropdown ───────────────────────────────────
        SettingsCard(title = "Mezhep / İkindi Vakti") {
            Text(
                text  = "Hanefi mezhebinde ikindi vakti daha geç başlar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))

            SettingsDropdown(
                label       = "Mezhep",
                options     = schoolOptions,
                selectedId  = state.preferences.school,
                onSelected  = { viewModel.setSchool(it) }
            )
        }

        // ── Bildirimler Kartı ─────────────────────────────────────────────────
        SettingsCard(title = "Bildirimler") {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Ezan Bildirimleri")
                Switch(
                    checked         = state.preferences.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications
                )
            }
        }

        // ── Görünüm Kartı ─────────────────────────────────────────────────────
        SettingsCard(title = "Görünüm") {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Karanlık Tema")
                Switch(
                    checked         = state.preferences.darkTheme,
                    onCheckedChange = viewModel::setDarkTheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))
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
                    text       = "Pil Optimizasyonu Aktif",
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
                    text       = "Pil Optimizasyonunu Kapat",
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
