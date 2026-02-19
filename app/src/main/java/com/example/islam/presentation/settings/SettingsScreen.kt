package com.example.islam.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Supported Aladhan calculation methods (subset)
private val calculationMethods = listOf(
    2 to "ISNA (Kuzey Amerika)",
    3 to "MWL (Dünya Müslümanlar Birliği)",
    5 to "Egypt (Mısır Genel İdaresi)",
    13 to "Diyanet İşleri Başkanlığı"
)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Location Card
        SettingsCard(title = "Konum") {
            OutlinedTextField(
                value = state.cityInput,
                onValueChange = viewModel::onCityInputChange,
                label = { Text("Şehir") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.countryInput,
                onValueChange = viewModel::onCountryInputChange,
                label = { Text("Ülke") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::saveLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }

        // Calculation Method Card
        SettingsCard(title = "Hesaplama Metodu") {
            calculationMethods.forEach { (id, name) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = state.preferences.calculationMethod == id,
                        onClick = { viewModel.setCalculationMethod(id) }
                    )
                    Text(text = name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Notifications Card
        SettingsCard(title = "Bildirimler") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ezan Bildirimleri")
                Switch(
                    checked = state.preferences.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications
                )
            }
        }

        // Appearance Card
        SettingsCard(title = "Görünüm") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Karanlık Tema")
                Switch(
                    checked = state.preferences.darkTheme,
                    onCheckedChange = viewModel::setDarkTheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
