package com.example.ui.screens.prayer

import android.Manifest
import android.os.Build
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AppPreferences
import com.example.data.service.AdhanPlaybackService
import com.example.data.service.AdhanScheduler
import com.example.data.service.PrayerPermissionPolicy
import com.example.data.location.GlobalLocationEngine
import com.example.data.location.LocationData
import com.example.data.location.LocationError
import com.example.data.location.TimezoneCrossingEvent
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.JuristicMethod
import com.example.data.model.LocalizedStrings
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesCalculator
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.theme.PrayerCardActive
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@SuppressLint("MissingPermission")
@Composable
fun PrayerTimesScreen(
    strings: LocalizedStrings,
    currentCity: CityLocation,
    appPreferences: AppPreferences,
    onCityChanged: (CityLocation) -> Unit
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone(currentCity.timeZoneId))) }

    var showCityDialog by remember { mutableStateOf(false) }
    var showCustomCoordsDialog by remember { mutableStateOf(false) }
    var showGpsDisabledDialog by remember { mutableStateOf(false) }
    var showCalcMethodDialog by remember { mutableStateOf(false) }
    var isAcquiringGps by remember { mutableStateOf(false) }

    var permissionDeniedBanner by remember { mutableStateOf(false) }
    var travelCrossingEvent by remember { mutableStateOf<TimezoneCrossingEvent?>(null) }

    var calculationMethod by remember { mutableStateOf(appPreferences.getCalculationMethod()) }
    var juristicMethod by remember { mutableStateOf(appPreferences.getJuristicMethod()) }
    var notificationsEnabled by remember { mutableStateOf(appPreferences.isPrayerNotificationsEnabled()) }
    var allAdhanEnabled by remember { mutableStateOf(appPreferences.isPrayerNotificationsEnabled()) }

    // Live clock ticker in geographical timezone
    LaunchedEffect(currentCity, calculationMethod, juristicMethod) {
        AdhanScheduler.scheduleAll(context, currentCity)
    }

    LaunchedEffect(currentCity.timeZoneId) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance(TimeZone.getTimeZone(currentCity.timeZoneId))
        }
    }

    val requestGpsLocation: () -> Unit = {
        if (!GlobalLocationEngine.isLocationServiceEnabled(context)) {
            showGpsDisabledDialog = true
        } else {
            isAcquiringGps = true
            permissionDeniedBanner = false
            GlobalLocationEngine.fetchGpsLocation(
                context = context,
                onSuccess = { newLoc ->
                    isAcquiringGps = false
                    val previousLoc = appPreferences.getCachedLocation()
                    val crossing = GlobalLocationEngine.detectTravelAcrossTimezone(previousLoc, newLoc)
                    if (crossing != null && crossing.oldTimezone != crossing.newTimezone) {
                        travelCrossingEvent = crossing
                    }
                    appPreferences.saveLocation(newLoc)
                    onCityChanged(CityLocation(newLoc.name, newLoc.country, newLoc.latitude, newLoc.longitude, newLoc.timeZoneId))
                    Toast.makeText(context, "${strings.currentLocation}: ${newLoc.name}", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    isAcquiringGps = false
                    when (err) {
                        is LocationError.PermissionDenied -> {
                            permissionDeniedBanner = true
                        }
                        is LocationError.GpsDisabled -> {
                            showGpsDisabledDialog = true
                        }
                        is LocationError.LocationUnavailable -> {
                            Toast.makeText(context, "GPS location unavailable, using offline cache", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    // Android 13+ requires a runtime notification permission. Ask only when the user enables prayer alerts.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        allAdhanEnabled = granted
        appPreferences.setPrayerNotificationsEnabled(granted)
        AdhanScheduler.scheduleAll(context, currentCity)
        if (!granted) {
            Toast.makeText(
                context,
                "Notification permission is off. Enable it in Android Settings to receive prayer alerts.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val setPrayerAlertsEnabled: (Boolean) -> Unit = { enabled ->
        val permissionGranted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (enabled && PrayerPermissionPolicy.shouldRequestPostNotifications(Build.VERSION.SDK_INT, permissionGranted)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationsEnabled = enabled
            allAdhanEnabled = enabled
            appPreferences.setPrayerNotificationsEnabled(enabled)
            AdhanScheduler.scheduleAll(context, currentCity)
        }
    }

    // Permission launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            permissionDeniedBanner = false
            requestGpsLocation()
        } else {
            permissionDeniedBanner = true
            Toast.makeText(context, "Location permission not granted. Using offline cache.", Toast.LENGTH_SHORT).show()
        }
    }

    // Precise prayer times calculated using the geographical timezone and DST rules
    val calculatedPrayers = remember(currentCity, calculationMethod, juristicMethod, currentTime.get(Calendar.MINUTE)) {
        PrayerTimesCalculator.calculate(
            latitude = currentCity.latitude,
            longitude = currentCity.longitude,
            timeZoneId = currentCity.timeZoneId,
            calendar = currentTime,
            method = calculationMethod,
            juristic = juristicMethod
        )
    }

    // Countdown formatting
    val remSec = (calculatedPrayers.remainingMillis / 1000).coerceAtLeast(0L)
    val remHours = remSec / 3600
    val remMinutes = (remSec % 3600) / 60
    val remSeconds = remSec % 60
    val countdownFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", remHours, remMinutes, remSeconds)

    val nextPrayerName = when (calculatedPrayers.nextPrayer.type) {
        PrayerType.FAJR -> strings.fajr
        PrayerType.SUNRISE -> strings.sunrise
        PrayerType.DHUHR -> strings.dhuhr
        PrayerType.ASR -> strings.asr
        PrayerType.MAGHRIB -> strings.maghrib
        PrayerType.ISHA -> strings.isha
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_times_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Timezone Travel Crossing Alert Banner
        if (travelCrossingEvent != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IslamicGold.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Timezone Boundary Crossed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${travelCrossingEvent!!.oldTimezone} ➔ ${travelCrossingEvent!!.newTimezone} (${travelCrossingEvent!!.distanceKm.toInt()} km traveled). Prayer schedule updated.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { travelCrossingEvent = null }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Dismiss", tint = IslamicEmerald, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Location Permission Denied Banner
        if (permissionDeniedBanner) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPS Permission Denied",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Using offline cached location: ${currentCity.name}. Tap to grant permission.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Header Card with Next Prayer Countdown
        item {
            IslamicOrnamentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_countdown_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.nextPrayer,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextPrayerName,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = IslamicGoldBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = calculatedPrayers.nextPrayer.timeFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live countdown clock
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = IslamicGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "- $countdownFormatted",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${strings.currentLocation}: ${currentCity.name} (${currentCity.timeZoneId})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Location & GPS Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCityDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IslamicEmerald.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = IslamicEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentCity.name}${if (currentCity.country.isNotEmpty()) ", " + currentCity.country else ""}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "Geo Timezone: ${currentCity.timeZoneId} • Tap to search worldwide",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Auto GPS detect button
                    IconButton(
                        onClick = {
                            if (GlobalLocationEngine.hasLocationPermission(context)) {
                                requestGpsLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("gps_detect_button")
                    ) {
                        if (isAcquiringGps) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = IslamicEmerald)
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Detect GPS Location",
                                tint = IslamicEmerald
                            )
                        }
                    }
                }
            }
        }

        // Prayer Times List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.todayPrayerTimes,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            setPrayerAlertsEnabled(!notificationsEnabled)
                        }
                    ) {
                        Icon(
                            imageVector = if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = if (notificationsEnabled) strings.notificationsEnabled else strings.notificationsDisabled,
                            tint = if (notificationsEnabled) IslamicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Six Prayer Time Items (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
        val prayerList = calculatedPrayers.asList()
        items(prayerList) { item ->
            val icon = getPrayerIcon(item.type)
            val name = when (item.type) {
                PrayerType.FAJR -> strings.fajr
                PrayerType.SUNRISE -> strings.sunrise
                PrayerType.DHUHR -> strings.dhuhr
                PrayerType.ASR -> strings.asr
                PrayerType.MAGHRIB -> strings.maghrib
                PrayerType.ISHA -> strings.isha
            }

            PrayerRowCard(
                name = name,
                item = item,
                icon = icon,
                isNext = item.isNext
            )
        }


        // New Adhan control panel: master switch, per-prayer switches and safe test playback.
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Adhan & Prayer Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "Automatic call to prayer for the five daily prayers",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = allAdhanEnabled,
                            onCheckedChange = { setPrayerAlertsEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        PrayerType.FAJR to strings.fajr,
                        PrayerType.DHUHR to strings.dhuhr,
                        PrayerType.ASR to strings.asr,
                        PrayerType.MAGHRIB to strings.maghrib,
                        PrayerType.ISHA to strings.isha
                    ).forEach { (prayer, label) ->
                        var enabled by remember(prayer) {
                            mutableStateOf(appPreferences.isAdhanEnabled(prayer))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = enabled,
                                enabled = allAdhanEnabled,
                                onCheckedChange = { checked ->
                                    enabled = checked
                                    appPreferences.setAdhanEnabled(prayer, checked)
                                    AdhanScheduler.scheduleAll(context, currentCity)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                runCatching {
                                    AdhanPlaybackService.playTest(context, PrayerType.DHUHR)
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Adhan test could not start. Please reopen the app.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            )
                        ) {
                            Text("▶  Test Adhan")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { AdhanPlaybackService.stop(context) }
                        ) {
                            Text("Stop")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Keep notifications and Alarms & reminders enabled so Adhan can run while the app is closed.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            TextButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        )
                                    }
                                }
                            ) { Text("Enable exact prayer alarms") }
                        }
                    }
                }
            }
        }

        // Calculation Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCalcMethodDialog = true }
                    .testTag("calc_method_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.calculationMethod,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = calculationMethod.title,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = juristicMethod.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Edit",
                        color = IslamicGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Worldwide City Selection & Search Dialog (Completely removes 10-city restriction)
    if (showCityDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val searchResults = remember(searchQuery) {
            GlobalLocationEngine.searchWorldwideLocations(context, searchQuery)
        }

        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = IslamicEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Global Location & City Search", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("city_search_input"),
                        placeholder = { Text("Search any city worldwide...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions: GPS button & Custom Coords button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                showCityDialog = false
                                if (GlobalLocationEngine.hasLocationPermission(context)) {
                                    requestGpsLocation()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = IslamicEmerald)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use GPS", color = IslamicEmerald, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = {
                                showCityDialog = false
                                showCustomCoordsDialog = true
                            }
                        ) {
                            Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = IslamicGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Coords", color = IslamicGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Results list
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(searchResults) { loc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val previousLoc = appPreferences.getCachedLocation()
                                        val crossing = GlobalLocationEngine.detectTravelAcrossTimezone(previousLoc, loc)
                                        if (crossing != null && crossing.oldTimezone != crossing.newTimezone) {
                                            travelCrossingEvent = crossing
                                        }
                                        appPreferences.saveLocation(loc)
                                        onCityChanged(CityLocation(loc.name, loc.country, loc.latitude, loc.longitude, loc.timeZoneId))
                                        showCityDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = loc.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "${loc.country} • ${loc.timeZoneId}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (loc.name == currentCity.name) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = IslamicEmerald)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text(strings.cancel, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Custom Coordinates Entry Dialog (Allows entering ANY location on Earth)
    if (showCustomCoordsDialog) {
        var latInput by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentCity.latitude)) }
        var lngInput by remember { mutableStateOf(String.format(Locale.US, "%.4f", currentCity.longitude)) }
        var nameInput by remember { mutableStateOf(currentCity.name) }

        AlertDialog(
            onDismissRequest = { showCustomCoordsDialog = false },
            title = { Text("Enter Custom Coordinates", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Enter any coordinates on Earth to compute prayer times with geographical timezone intelligence.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Location Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text("Latitude (-90 to +90)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lngInput,
                        onValueChange = { lngInput = it },
                        label = { Text("Longitude (-180 to +180)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = latInput.toDoubleOrNull()
                        val lng = lngInput.toDoubleOrNull()
                        if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                            val resolved = GlobalLocationEngine.buildLocationFromCoordinates(
                                context = context,
                                latitude = lat,
                                longitude = lng,
                                isGps = false,
                                nameOverride = nameInput.ifBlank { "Custom Location" }
                            )
                            val previousLoc = appPreferences.getCachedLocation()
                            val crossing = GlobalLocationEngine.detectTravelAcrossTimezone(previousLoc, resolved)
                            if (crossing != null && crossing.oldTimezone != crossing.newTimezone) {
                                travelCrossingEvent = crossing
                            }
                            appPreferences.saveLocation(resolved)
                            onCityChanged(CityLocation(resolved.name, resolved.country, resolved.latitude, resolved.longitude, resolved.timeZoneId))
                            showCustomCoordsDialog = false
                            Toast.makeText(context, "Location set: ${resolved.name} (${resolved.timeZoneId})", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("Apply Coordinates")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCoordsDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // GPS Disabled Dialog
    if (showGpsDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDisabledDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = IslamicGold, modifier = Modifier.size(36.dp)) },
            title = { Text("Location Services Disabled", fontWeight = FontWeight.Bold) },
            text = {
                Text("Device GPS is currently turned off. To detect your exact location automatically, please enable Location Services in device settings, or continue using the offline cached location (${currentCity.name}).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsDisabledDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDisabledDialog = false }) {
                    Text("Use Offline Cache")
                }
            }
        )
    }

    // Calculation Method Dialog
    if (showCalcMethodDialog) {
        AlertDialog(
            onDismissRequest = { showCalcMethodDialog = false },
            title = { Text(strings.calculationMethod, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    calculationMethod = method
                                    appPreferences.setCalculationMethod(method)
                                    showCalcMethodDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = calculationMethod == method,
                                onClick = {
                                    calculationMethod = method
                                    appPreferences.setCalculationMethod(method)
                                    showCalcMethodDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = method.title, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Asr Juristic Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    JuristicMethod.entries.forEach { jm ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    juristicMethod = jm
                                    appPreferences.setJuristicMethod(jm)
                                    showCalcMethodDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = juristicMethod == jm,
                                onClick = {
                                    juristicMethod = jm
                                    appPreferences.setJuristicMethod(jm)
                                    showCalcMethodDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = jm.title, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalcMethodDialog = false }) {
                    Text("Done", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
private fun PrayerRowCard(
    name: String,
    item: PrayerTimeItem,
    icon: ImageVector,
    isNext: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isNext) Modifier.border(1.5.dp, IslamicGold, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) PrayerCardActive else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isNext) IslamicGold.copy(alpha = 0.2f) else IslamicEmerald.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isNext) IslamicGold else IslamicEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isNext) IslamicGold else MaterialTheme.colorScheme.onSurface
                    )
                    if (isNext) {
                        Text(
                            text = "Next Prayer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGold
                        )
                    }
                }
            }

            Text(
                text = item.timeFormatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isNext) IslamicGold else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.FAJR -> Icons.Default.WbTwilight
        PrayerType.SUNRISE -> Icons.Default.Brightness5
        PrayerType.DHUHR -> Icons.Default.Brightness7
        PrayerType.ASR -> Icons.Default.Brightness6
        PrayerType.MAGHRIB -> Icons.Default.Brightness4
        PrayerType.ISHA -> Icons.Default.WbTwilight
    }
}
