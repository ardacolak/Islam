package com.example.islam.core.i18n

import com.example.islam.domain.model.Prayer

// ─────────────────────────────────────────────────────────────────────────────
// Uygulama geneli çeviri modeli
// ─────────────────────────────────────────────────────────────────────────────

data class AppStrings(
    // Navigasyon
    val navHome: String,
    val navPrayerTimes: String,
    val navDhikr: String,
    val navQibla: String,
    val navSettings: String,

    // Namaz isimleri
    val prayerImsak: String,
    val prayerFajr: String,
    val prayerSunrise: String,
    val prayerDhuhr: String,
    val prayerAsr: String,
    val prayerMaghrib: String,
    val prayerIsha: String,

    // Ana ekran
    val nextPrayer: String,
    val remainingTime: String,
    val todaysTimes: String,
    val streakDays: String,         // format: "%d Günlük Seri"
    val streakComplete: String,
    val streakCongrats: String,
    val noInternetTitle: String,
    val noInternetDesc: String,
    val errorTitle: String,
    val retryButton: String,

    // Namaz ekranı
    val todaysPrayers: String,
    val completed: String,
    val refresh: String,

    // Zikir ekranı
    val dhikrCounter: String,
    val reset: String,
    val dhikrCompleted: String,

    // Kıble ekranı
    val qiblaDirection: String,
    val qiblaAligned: String,
    val direction: String,
    val qibla: String,
    val deviation: String,
    val noSensor: String,
    val magnetometerRequired: String,

    // Ayarlar ekranı
    val settings: String,
    val location: String,
    val city: String,
    val country: String,
    val save: String,
    val calculationMethodTitle: String,
    val calculationMethodDesc: String,
    val schoolTitle: String,
    val schoolDesc: String,
    val notificationsTitle: String,
    val azanNotifications: String,
    val appearance: String,
    val darkTheme: String,
    val language: String,
    val batteryOptActive: String,
    val batteryOptFix: String,

    // Hesaplama metotları (ID → isim)
    val calcMethods: List<Pair<Int, String>>,
    val schoolOptions: List<Pair<Int, String>>,

    // Onboarding
    val onboardingWelcomeTitle: String,
    val onboardingWelcomeSubtitle: String,
    val onboardingWelcomeDesc: String,
    val onboardingFeaturesTitle: String,
    val onboardingFeaturesSubtitle: String,
    val onboardingFeaturesDesc: String,
    val onboardingPermissionsTitle: String,
    val onboardingPermissionsSubtitle: String,
    val onboardingPermissionsDesc: String,
    val next: String,
    val grantAndStart: String,
    val skipForNow: String,

    // İzin kartları
    val locationPermTitle: String,
    val locationPermDesc: String,
    val locationPermButton: String,
    val notificationPermTitle: String,
    val notificationPermDesc: String,
    val notificationPermButton: String,
    val exactAlarmPermTitle: String,
    val exactAlarmPermDesc: String,
    val exactAlarmPermButton: String,
) {
    fun prayerName(prayer: Prayer): String = when (prayer) {
        Prayer.IMSAK   -> prayerImsak
        Prayer.FAJR    -> prayerFajr
        Prayer.SUNRISE -> prayerSunrise
        Prayer.DHUHR   -> prayerDhuhr
        Prayer.ASR     -> prayerAsr
        Prayer.MAGHRIB -> prayerMaghrib
        Prayer.ISHA    -> prayerIsha
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Türkçe
// ─────────────────────────────────────────────────────────────────────────────

val TurkishStrings = AppStrings(
    navHome        = "Ana Sayfa",
    navPrayerTimes = "Vakitler",
    navDhikr       = "Zikir",
    navQibla       = "Kıble",
    navSettings    = "Ayarlar",

    prayerImsak   = "İmsak",
    prayerFajr    = "Sabah",
    prayerSunrise = "Güneş",
    prayerDhuhr   = "Öğle",
    prayerAsr     = "İkindi",
    prayerMaghrib = "Akşam",
    prayerIsha    = "Yatsı",

    nextPrayer    = "Sonraki Namaz",
    remainingTime = "Kalan Süre",
    todaysTimes   = "Bugünün Vakitleri",
    streakDays    = "%d Günlük Seri",
    streakComplete = "Bugün namazlarını tamamla!",
    streakCongrats = "Tebrikler, devam et!",
    noInternetTitle = "İnternet Bağlantısı Yok",
    noInternetDesc  = "Namaz vakitleri yüklenemiyor. Bağlantınızı kontrol edip tekrar deneyin.",
    errorTitle   = "Bir Hata Oluştu",
    retryButton  = "Tekrar Dene",

    todaysPrayers = "Bugünkü Namazlar",
    completed     = "Kılındı",
    refresh       = "Yenile",

    dhikrCounter  = "Zikirmatik",
    reset         = "Sıfırla",
    dhikrCompleted = "مَاشَاءَ اللّٰهُ  •  Tamamlandı! 🌿",

    qiblaDirection      = "Kıble Yönü",
    qiblaAligned        = "✅ Kıbleye yöneldiniz!",
    direction           = "Yön",
    qibla               = "Kıble",
    deviation           = "Sapma",
    noSensor            = "Pusula sensörü bulunamadı",
    magnetometerRequired = "Bu özellik manyetometre sensörü gerektirmektedir.",

    settings              = "Ayarlar",
    location              = "Konum",
    city                  = "Şehir",
    country               = "Ülke",
    save                  = "Kaydet",
    calculationMethodTitle = "Hesaplama Metodu",
    calculationMethodDesc  = "Namaz vakti hesaplama standardı",
    schoolTitle           = "Mezhep / İkindi Vakti",
    schoolDesc            = "Hanefi mezhebinde ikindi vakti daha geç başlar",
    notificationsTitle    = "Bildirimler",
    azanNotifications     = "Ezan Bildirimleri",
    appearance            = "Görünüm",
    darkTheme             = "Karanlık Tema",
    language              = "Dil",
    batteryOptActive      = "Pil Optimizasyonu Aktif",
    batteryOptFix         = "Pil Optimizasyonunu Kapat",

    calcMethods = listOf(
        2  to "ISNA (Kuzey Amerika)",
        3  to "MWL (Dünya Müslümanlar Birliği)",
        5  to "Egypt (Mısır Genel İdaresi)",
        13 to "Diyanet İşleri Başkanlığı"
    ),
    schoolOptions = listOf(
        0 to "Şafii (Standart)",
        1 to "Hanefi (İkindi geç başlar)"
    ),

    onboardingWelcomeTitle    = "Hoş Geldiniz",
    onboardingWelcomeSubtitle = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ",
    onboardingWelcomeDesc     = "Allah'ın adıyla başlıyoruz. Bu uygulama günlük ibadet hayatınızı kolaylaştırmak için tasarlandı.",
    onboardingFeaturesTitle    = "Özellikler",
    onboardingFeaturesSubtitle = "Namaz • Zikir • Kıble",
    onboardingFeaturesDesc     = "📿 Namaz vakitleri ve ezan bildirimleri\n🧭 Hassas kıble pusulası\n📿 Dijital zikir sayacı\n⚙️ Kişiselleştirilebilir hesaplama yöntemi",
    onboardingPermissionsTitle    = "İzinler",
    onboardingPermissionsSubtitle = "Size özel deneyim için",
    onboardingPermissionsDesc     = "Doğru namaz vakitleri ve kıble yönü için Konum izni; ezan vaktinde bildirim alabilmek için Bildirim izni isteyeceğiz.\n\nBu izinler yalnızca uygulama içi özellikler için kullanılır, hiçbir bilginiz paylaşılmaz.",
    next          = "İleri",
    grantAndStart = "İzin Ver ve Başla",
    skipForNow    = "Şimdilik Geç",

    locationPermTitle  = "Konum İzni Gerekli",
    locationPermDesc   = "Namaz vakitlerini hesaplayabilmek ve kıble yönünü belirleyebilmek için cihazınızın konumuna ihtiyaç duyulmaktadır.",
    locationPermButton = "Konuma İzin Ver",
    notificationPermTitle  = "Bildirim İzni",
    notificationPermDesc   = "Ezan vakitlerinde bildirim alabilmek için bildirim iznine ihtiyaç vardır.",
    notificationPermButton = "Bildirimlere İzin Ver",
    exactAlarmPermTitle  = "Tam Alarm İzni",
    exactAlarmPermDesc   = "Ezan vakitlerinde tam zamanında bildirim verebilmek için Ayarlar → Alarmlar & Hatırlatıcılar bölümünden izin vermeniz gerekiyor.",
    exactAlarmPermButton = "Ayarlara Git"
)

// ─────────────────────────────────────────────────────────────────────────────
// English
// ─────────────────────────────────────────────────────────────────────────────

val EnglishStrings = AppStrings(
    navHome        = "Home",
    navPrayerTimes = "Prayer Times",
    navDhikr       = "Dhikr",
    navQibla       = "Qibla",
    navSettings    = "Settings",

    prayerImsak   = "Imsak",
    prayerFajr    = "Fajr",
    prayerSunrise = "Sunrise",
    prayerDhuhr   = "Dhuhr",
    prayerAsr     = "Asr",
    prayerMaghrib = "Maghrib",
    prayerIsha    = "Isha",

    nextPrayer    = "Next Prayer",
    remainingTime = "Remaining",
    todaysTimes   = "Today's Prayer Times",
    streakDays    = "%d-Day Streak",
    streakComplete = "Complete today's prayers!",
    streakCongrats = "Congratulations, keep it up!",
    noInternetTitle = "No Internet Connection",
    noInternetDesc  = "Cannot load prayer times. Check your connection and try again.",
    errorTitle   = "An Error Occurred",
    retryButton  = "Retry",

    todaysPrayers = "Today's Prayers",
    completed     = "Completed",
    refresh       = "Refresh",

    dhikrCounter  = "Dhikr Counter",
    reset         = "Reset",
    dhikrCompleted = "مَاشَاءَ اللّٰهُ  •  Completed! 🌿",

    qiblaDirection      = "Qibla Direction",
    qiblaAligned        = "✅ You're facing the Qibla!",
    direction           = "Direction",
    qibla               = "Qibla",
    deviation           = "Deviation",
    noSensor            = "Compass sensor not found",
    magnetometerRequired = "This feature requires a magnetometer sensor.",

    settings              = "Settings",
    location              = "Location",
    city                  = "City",
    country               = "Country",
    save                  = "Save",
    calculationMethodTitle = "Calculation Method",
    calculationMethodDesc  = "Prayer time calculation standard",
    schoolTitle           = "School / Asr Time",
    schoolDesc            = "In Hanafi school, Asr starts later",
    notificationsTitle    = "Notifications",
    azanNotifications     = "Azan Notifications",
    appearance            = "Appearance",
    darkTheme             = "Dark Theme",
    language              = "Language",
    batteryOptActive      = "Battery Optimization Active",
    batteryOptFix         = "Disable Battery Optimization",

    calcMethods = listOf(
        2  to "ISNA (North America)",
        3  to "MWL (Muslim World League)",
        5  to "Egypt (Egyptian General Authority)",
        13 to "Diyanet (Turkey)"
    ),
    schoolOptions = listOf(
        0 to "Shafi'i (Standard)",
        1 to "Hanafi (Asr starts later)"
    ),

    onboardingWelcomeTitle    = "Welcome",
    onboardingWelcomeSubtitle = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ",
    onboardingWelcomeDesc     = "We begin in the name of Allah. This app was designed to make your daily worship easier.",
    onboardingFeaturesTitle    = "Features",
    onboardingFeaturesSubtitle = "Prayer • Dhikr • Qibla",
    onboardingFeaturesDesc     = "📿 Prayer times and azan notifications\n🧭 Precise qibla compass\n📿 Digital dhikr counter\n⚙️ Customizable calculation method",
    onboardingPermissionsTitle    = "Permissions",
    onboardingPermissionsSubtitle = "For a personalized experience",
    onboardingPermissionsDesc     = "We'll request Location permission for accurate prayer times and qibla direction, and Notification permission for azan alerts.\n\nThese permissions are only used for in-app features and no data is shared.",
    next          = "Next",
    grantAndStart = "Grant & Start",
    skipForNow    = "Skip for now",

    locationPermTitle  = "Location Permission Required",
    locationPermDesc   = "Location access is needed to calculate prayer times and determine the qibla direction.",
    locationPermButton = "Allow Location",
    notificationPermTitle  = "Notification Permission",
    notificationPermDesc   = "Notification access is needed to receive azan alerts.",
    notificationPermButton = "Allow Notifications",
    exactAlarmPermTitle  = "Exact Alarm Permission",
    exactAlarmPermDesc   = "To deliver azan notifications at the exact time, please grant permission from Settings → Alarms & Reminders.",
    exactAlarmPermButton = "Go to Settings"
)

// ─────────────────────────────────────────────────────────────────────────────
// العربية
// ─────────────────────────────────────────────────────────────────────────────

val ArabicStrings = AppStrings(
    navHome        = "الرئيسية",
    navPrayerTimes = "أوقات الصلاة",
    navDhikr       = "الذكر",
    navQibla       = "القبلة",
    navSettings    = "الإعدادات",

    prayerImsak   = "الإمساك",
    prayerFajr    = "الفجر",
    prayerSunrise = "الشروق",
    prayerDhuhr   = "الظهر",
    prayerAsr     = "العصر",
    prayerMaghrib = "المغرب",
    prayerIsha    = "العشاء",

    nextPrayer    = "الصلاة القادمة",
    remainingTime = "الوقت المتبقي",
    todaysTimes   = "أوقات صلاة اليوم",
    streakDays    = "سلسلة %d يوم",
    streakComplete = "أكمل صلوات اليوم!",
    streakCongrats = "مبروك، استمر!",
    noInternetTitle = "لا يوجد اتصال بالإنترنت",
    noInternetDesc  = "لا يمكن تحميل أوقات الصلاة. تحقق من اتصالك وحاول مرة أخرى.",
    errorTitle   = "حدث خطأ",
    retryButton  = "إعادة المحاولة",

    todaysPrayers = "صلوات اليوم",
    completed     = "مكتملة",
    refresh       = "تحديث",

    dhikrCounter  = "عداد الذكر",
    reset         = "إعادة",
    dhikrCompleted = "مَاشَاءَ اللّٰهُ  •  مكتمل! 🌿",

    qiblaDirection      = "اتجاه القبلة",
    qiblaAligned        = "✅ أنت تواجه القبلة!",
    direction           = "الاتجاه",
    qibla               = "القبلة",
    deviation           = "الانحراف",
    noSensor            = "لم يتم العثور على مستشعر البوصلة",
    magnetometerRequired = "تتطلب هذه الميزة مستشعر مقياس المغناطيس.",

    settings              = "الإعدادات",
    location              = "الموقع",
    city                  = "المدينة",
    country               = "البلد",
    save                  = "حفظ",
    calculationMethodTitle = "طريقة الحساب",
    calculationMethodDesc  = "معيار حساب وقت الصلاة",
    schoolTitle           = "المذهب / وقت العصر",
    schoolDesc            = "في المذهب الحنفي، تبدأ صلاة العصر متأخرة",
    notificationsTitle    = "الإشعارات",
    azanNotifications     = "إشعارات الأذان",
    appearance            = "المظهر",
    darkTheme             = "الوضع الداكن",
    language              = "اللغة",
    batteryOptActive      = "تحسين البطارية نشط",
    batteryOptFix         = "تعطيل تحسين البطارية",

    calcMethods = listOf(
        2  to "ISNA (أمريكا الشمالية)",
        3  to "رابطة العالم الإسلامي",
        5  to "الهيئة المصرية العامة",
        13 to "رئاسة الشؤون الدينية (تركيا)"
    ),
    schoolOptions = listOf(
        0 to "الشافعي (معيار)",
        1 to "الحنفي (العصر يبدأ متأخراً)"
    ),

    onboardingWelcomeTitle    = "مرحباً",
    onboardingWelcomeSubtitle = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ",
    onboardingWelcomeDesc     = "نبدأ بسم الله. صُمِّم هذا التطبيق لتسهيل حياتك العبادية اليومية.",
    onboardingFeaturesTitle    = "المميزات",
    onboardingFeaturesSubtitle = "الصلاة • الذكر • القبلة",
    onboardingFeaturesDesc     = "📿 أوقات الصلاة وإشعارات الأذان\n🧭 بوصلة قبلة دقيقة\n📿 عداد ذكر رقمي\n⚙️ طريقة حساب قابلة للتخصيص",
    onboardingPermissionsTitle    = "الأذونات",
    onboardingPermissionsSubtitle = "لتجربة شخصية",
    onboardingPermissionsDesc     = "سنطلب إذن الموقع لأوقات الصلاة الدقيقة واتجاه القبلة، وإذن الإشعارات لتنبيهات الأذان.\n\nتُستخدم هذه الأذونات فقط لميزات التطبيق ولا تُشارك أي بيانات.",
    next          = "التالي",
    grantAndStart = "منح الإذن والبدء",
    skipForNow    = "تخطي الآن",

    locationPermTitle  = "مطلوب إذن الموقع",
    locationPermDesc   = "يلزم الوصول إلى الموقع لحساب أوقات الصلاة وتحديد اتجاه القبلة.",
    locationPermButton = "السماح بالموقع",
    notificationPermTitle  = "إذن الإشعارات",
    notificationPermDesc   = "يلزم الوصول إلى الإشعارات لتلقي تنبيهات الأذان.",
    notificationPermButton = "السماح بالإشعارات",
    exactAlarmPermTitle  = "إذن التنبيه الدقيق",
    exactAlarmPermDesc   = "لتقديم إشعارات الأذان في الوقت المحدد، يرجى منح الإذن من الإعدادات ← المنبهات والتذكيرات.",
    exactAlarmPermButton = "الذهاب إلى الإعدادات"
)

fun stringsFor(languageCode: String): AppStrings = when (languageCode) {
    "en" -> EnglishStrings
    "ar" -> ArabicStrings
    else -> TurkishStrings
}
