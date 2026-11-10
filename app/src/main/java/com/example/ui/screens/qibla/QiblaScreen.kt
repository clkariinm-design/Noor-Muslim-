package com.example.ui.screens.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import com.example.data.local.AppPreferences
import com.example.data.location.GlobalLocationEngine
import com.example.data.location.LocationError
import com.example.data.model.CityLocation
import com.example.data.model.LocalizedStrings
import com.example.data.repository.QiblaCalculator
import com.example.ui.components.IslamicOrnamentCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldBright
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    strings: LocalizedStrings,
    currentCity: CityLocation,
    appPreferences: AppPreferences? = null,
    onLocationChanged: ((CityLocation) -> Unit)? = null
) {
    val context = LocalContext.current

    val qiblaBearing = remember(currentCity) {
        QiblaCalculator.calculateBearing(currentCity.latitude, currentCity.longitude)
    }

    val distanceKm = remember(currentCity) {
        QiblaCalculator.calculateDistanceKm(currentCity.latitude, currentCity.longitude)
    }

    var sensorAvailable by remember { mutableStateOf(false) }
    var deviceHeading by remember { mutableFloatStateOf(0f) }
    var manualHeading by remember { mutableFloatStateOf(0f) }
    var isAcquiringGps by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                      (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
        if (granted) {
            isAcquiringGps = true
            GlobalLocationEngine.fetchGpsLocation(
                context = context,
                onSuccess = { newLoc ->
                    isAcquiringGps = false
                    appPreferences?.saveLocation(newLoc)
                    onLocationChanged?.invoke(CityLocation(newLoc.name, newLoc.country, newLoc.latitude, newLoc.longitude, newLoc.timeZoneId))
                    Toast.makeText(context, "${strings.currentLocation}: ${newLoc.name}", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    isAcquiringGps = false
                    Toast.makeText(context, "GPS location unavailable, using offline cache", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission not granted. Using offline cache.", Toast.LENGTH_SHORT).show()
        }
    }

    // Compass Sensor Listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var hasSensor = false
        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            var lastAccelerometer = FloatArray(3)
            var lastMagnetometer = FloatArray(3)
            var lastAccelSet = false
            var lastMagnetSet = false

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    val normalized = (azimuthInDegrees + 360f) % 360f
                    // Smooth low-pass filter
                    deviceHeading = (deviceHeading * 0.8f + normalized * 0.2f)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetSet = true
                }

                if (lastAccelSet && lastMagnetSet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        val normalized = (azimuth + 360f) % 360f
                        deviceHeading = (deviceHeading * 0.8f + normalized * 0.2f)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            hasSensor = sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null && magnetSensor != null) {
            val aRegistered = sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            val mRegistered = sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
            hasSensor = aRegistered && mRegistered
        }

        sensorAvailable = hasSensor

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val activeHeading = if (sensorAvailable) deviceHeading else manualHeading
    val needleAngle = (qiblaBearing - activeHeading + 360f) % 360f

    val isFacingKaaba = abs(needleAngle) <= 5f || abs(needleAngle - 360f) <= 5f

    val animatedNeedleAngle by animateFloatAsState(
        targetValue = needleAngle,
        animationSpec = spring(),
        label = "NeedleAngle"
    )

    val dialBorderColor by animateColorAsState(
        targetValue = if (isFacingKaaba) IslamicGoldBright else IslamicEmerald,
        label = "BorderColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location & Distance header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentCity.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${strings.kaabaAngle}: ${qiblaBearing.toInt()}°",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$distanceKm km",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Text(
                        text = strings.distanceToMakkah,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Facing Kaaba Announcement banner
        if (isFacingKaaba) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IslamicEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.facingKaabaSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // COMPASS DIAL & NEEDLE
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
                .border(4.dp, dialBorderColor, CircleShape)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Compass Rose Markings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Draw tick marks every 30 degrees
                for (angleDeg in 0 until 360 step 30) {
                    val rad = Math.toRadians((angleDeg - activeHeading).toDouble())
                    val isMajor = angleDeg % 90 == 0
                    val tickLen = if (isMajor) 18f else 10f
                    val startOffset = Offset(
                        center.x + (radius - tickLen) * sin(rad).toFloat(),
                        center.y - (radius - tickLen) * cos(rad).toFloat()
                    )
                    val endOffset = Offset(
                        center.x + radius * sin(rad).toFloat(),
                        center.y - radius * cos(rad).toFloat()
                    )
                    drawLine(
                        color = if (isMajor) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.4f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = if (isMajor) 3f else 1.5f
                    )
                }
            }

            // North indicator needle (Red/Gray pointer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-activeHeading),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = "N",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Rotating Qibla Needle pointing directly to Kaaba
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedNeedleAngle)
                    .testTag("qibla_needle"),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Qibla Direction",
                        tint = IslamicGoldBright,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Center Holy Kaaba Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, IslamicGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kaaba_compass),
                    contentDescription = "Kaaba",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Heading & Angle readout card
        IslamicOrnamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${strings.currentHeading}: ${activeHeading.toInt()}°",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.alignNeedle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Sensor Unavailable Notice or Manual Rotation Slider for Testing/Fallback
        if (!sensorAvailable) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.sensorUnavailable,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.sensorUnavailableDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulate Device Heading: ${manualHeading.toInt()}°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = manualHeading,
                        onValueChange = { manualHeading = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicEmerald,
                            activeTrackColor = IslamicEmerald
                        )
                    )
                }
            }
        }
    }
}
