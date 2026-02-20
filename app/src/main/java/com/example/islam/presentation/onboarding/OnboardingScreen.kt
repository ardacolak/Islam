package com.example.islam.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.core.navigation.Screen
import kotlinx.coroutines.launch

// ─── Sayfa modeli ─────────────────────────────────────────────────────────────

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val gradient: List<Color>
)

@Composable
private fun onboardingPages(): List<OnboardingPage> {
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary  = MaterialTheme.colorScheme.tertiary
    return listOf(
        OnboardingPage(
            icon        = Icons.Outlined.Mosque,
            title       = "Hoş Geldiniz",
            subtitle    = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ",
            description = "Allah'ın adıyla başlıyoruz. Bu uygulama günlük ibadet hayatınızı kolaylaştırmak için tasarlandı.",
            gradient    = listOf(primary.copy(alpha = 0.15f), primary.copy(alpha = 0.03f))
        ),
        OnboardingPage(
            icon        = Icons.Outlined.AutoAwesome,
            title       = "Özellikler",
            subtitle    = "Namaz • Zikir • Kıble",
            description = "📿 Namaz vakitleri ve ezan bildirimleri\n🧭 Hassas kıble pusulası\n📿 Dijital zikir sayacı\n⚙️ Kişiselleştirilebilir hesaplama yöntemi",
            gradient    = listOf(secondary.copy(alpha = 0.18f), secondary.copy(alpha = 0.03f))
        ),
        OnboardingPage(
            icon        = Icons.Outlined.LocationOn,
            title       = "İzinler",
            subtitle    = "Size özel deneyim için",
            description = "Doğru namaz vakitleri ve kıble yönü için Konum izni; ezan vaktinde bildirim alabilmek için Bildirim izni isteyeceğiz.\n\nBu izinler yalnızca uygulama içi özellikler için kullanılır, hiçbir bilginiz paylaşılmaz.",
            gradient    = listOf(tertiary.copy(alpha = 0.18f), tertiary.copy(alpha = 0.03f))
        )
    )
}

// ─── Ana ekran ────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pages       = onboardingPages()
    val pagerState  = rememberPagerState { pages.size }
    val scope       = rememberCoroutineScope()
    val isLastPage  = pagerState.currentPage == pages.lastIndex

    // ── İzin başlatıcıları ────────────────────────────────────────────────────
    fun finishOnboarding() {
        viewModel.completeOnboarding()
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    var locationGranted      by remember { mutableStateOf(false) }
    var notificationGranted  by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
        finishOnboarding()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        locationGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        // Konum sonrası bildirim izni iste (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationGranted = true
            finishOnboarding()
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Arka plan gradient — sayfa değişince crossfade ile geçiş
        AnimatedContent(
            targetState = pagerState.currentPage,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            label = "onboarding_bg"
        ) { page ->
            AnimatedGradientBackground(pages[page].gradient)
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // Pager
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                PageContent(page = pages[index])
            }

            // Alt kontroller
            Column(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Sayfa göstergeleri
                PageIndicator(
                    pageCount   = pages.size,
                    currentPage = pagerState.currentPage
                )

                // Sonraki / Başla butonu
                if (isLastPage) {
                    // İzin Ver ve Başla
                    Button(
                        onClick = {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(8.dp),   // ADM button radius
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "İzin Ver ve Başla",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }

                    // Geç (izin vermeden de devam edilebilir)
                    TextButton(
                        onClick = { finishOnboarding() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text  = "Şimdilik Geç",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // İleri butonu
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(8.dp),   // ADM button radius
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text       = "İleri",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Sayfa içeriği ────────────────────────────────────────────────────────────

@Composable
private fun PageContent(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(page) {
        visible = false
        kotlinx.coroutines.delay(100)
        visible = true
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 80.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // İkon — yukarıdan gelir
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            Surface(
                shape  = RoundedCornerShape(20.dp),   // ADM avatar/icon container style
                color  = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(112.dp)
            ) {
                Icon(
                    imageVector        = page.icon,
                    contentDescription = null,
                    modifier           = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        // Metin grubu — biraz gecikmeli gelir
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text      = page.title,
                    style     = MaterialTheme.typography.headlineMedium,
                    color     = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = page.subtitle,
                    style     = MaterialTheme.typography.titleMedium,
                    color     = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text      = page.description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// ─── Sayfa göstergesi ─────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(pageCount) { idx ->
            val isSelected = idx == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,   // ADM: slimmer indicator
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "indicator_width"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)           // ADM: 8dp→6dp
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

// ─── Arka plan gradient animasyonu ────────────────────────────────────────────

@Composable
private fun AnimatedGradientBackground(colors: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = colors)
            )
    )
}
