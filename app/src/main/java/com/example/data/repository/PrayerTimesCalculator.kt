package com.example.data.repository

import com.example.data.location.GeoTimezoneResolver
import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimesCalculator {

    data class CalculatedDayPrayers(
        val fajr: PrayerTimeItem,
        val sunrise: PrayerTimeItem,
        val dhuhr: PrayerTimeItem,
        val asr: PrayerTimeItem,
        val maghrib: PrayerTimeItem,
        val isha: PrayerTimeItem,
        val nextPrayer: PrayerTimeItem,
        val remainingMillis: Long
    ) {
        fun asList(): List<PrayerTimeItem> = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
    }

    /**
     * Calculates prayer times using an explicit IANA timezone ID, ensuring geographical accuracy and DST compliance.
     */
    fun calculate(
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)),
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        juristic: JuristicMethod = JuristicMethod.STANDARD
    ): CalculatedDayPrayers {
        val tz = TimeZone.getTimeZone(timeZoneId)
        return calculate(latitude, longitude, tz, calendar, method, juristic)
    }

    fun calculate(
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone = GeoTimezoneResolver.resolveTimeZone(latitude, longitude),
        calendar: Calendar = Calendar.getInstance(timeZone),
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        juristic: JuristicMethod = JuristicMethod.STANDARD
    ): CalculatedDayPrayers {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tzOffsetHours = timeZone.getOffset(calendar.timeInMillis) / 3600000.0
        val julianDay = getJulianDay(year, month, day) - (longitude / (15.0 * 24.0))

        val d = julianDay - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l))))) / 15.0
        var eqT = q / 15.0 - ra
        while (eqT > 12.0) eqT -= 24.0
        while (eqT < -12.0) eqT += 24.0
        val dhuhrMidday = 12.0 + tzOffsetHours - (longitude / 15.0) - eqT

        val declination = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        // Sunrise & Sunset angle is 0.833 degrees below horizon
        val sunriseDiff = sunAngleDiff(0.833, latitude, declination)
        val sunriseHour = dhuhrMidday - (sunriseDiff / 15.0)
        val maghribHour = dhuhrMidday + (sunriseDiff / 15.0)

        // Fajr
        val fajrDiff = sunAngleDiff(method.fajrAngle, latitude, declination)
        val fajrHour = dhuhrMidday - (fajrDiff / 15.0)

        // Asr
        val shadowFactor = juristic.shadowFactor
        val asrAltitudeRad = atan(1.0 / (shadowFactor + tan(degToRad(abs(latitude - declination)))))
        val asrDiff = asrHourAngle(asrAltitudeRad, latitude, declination)
        val asrHour = dhuhrMidday + (asrDiff / 15.0)

        // Isha
        val ishaHour = if (method.ishaMinutesAfterMaghrib != null) {
            maghribHour + (method.ishaMinutesAfterMaghrib / 60.0)
        } else {
            val ishaDiff = sunAngleDiff(method.ishaAngle, latitude, declination)
            dhuhrMidday + (ishaDiff / 15.0)
        }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0 + calendar.get(Calendar.SECOND) / 3600.0

        val (fajrH, fajrM) = decimalToTime(fajrHour)
        val (sunH, sunM) = decimalToTime(sunriseHour)
        val (dhuhrH, dhuhrM) = decimalToTime(dhuhrMidday)
        val (asrH, asrM) = decimalToTime(asrHour)
        val (maghribH, maghribM) = decimalToTime(maghribHour)
        val (ishaH, ishaM) = decimalToTime(ishaHour)

        // Determine next prayer and time remaining
        val prayerHours = listOf(
            PrayerType.FAJR to fajrHour,
            PrayerType.SUNRISE to sunriseHour,
            PrayerType.DHUHR to dhuhrMidday,
            PrayerType.ASR to asrHour,
            PrayerType.MAGHRIB to maghribHour,
            PrayerType.ISHA to ishaHour
        )

        var nextType = PrayerType.FAJR
        var nextHourDecimal = fajrHour + 24.0 // tomorrow fajr by default
        for ((pType, pHour) in prayerHours) {
            if (pHour > currentHour) {
                nextType = pType
                nextHourDecimal = pHour
                break
            }
        }

        val remainingHours = nextHourDecimal - currentHour
        val remainingMillis = (remainingHours * 3600 * 1000).toLong().coerceAtLeast(0L)

        val fajrItem = PrayerTimeItem(PrayerType.FAJR, formatTime(fajrH, fajrM), fajrH, fajrM, nextType == PrayerType.FAJR, currentHour > fajrHour)
        val sunriseItem = PrayerTimeItem(PrayerType.SUNRISE, formatTime(sunH, sunM), sunH, sunM, nextType == PrayerType.SUNRISE, currentHour > sunriseHour)
        val dhuhrItem = PrayerTimeItem(PrayerType.DHUHR, formatTime(dhuhrH, dhuhrM), dhuhrH, dhuhrM, nextType == PrayerType.DHUHR, currentHour > dhuhrMidday)
        val asrItem = PrayerTimeItem(PrayerType.ASR, formatTime(asrH, asrM), asrH, asrM, nextType == PrayerType.ASR, currentHour > asrHour)
        val maghribItem = PrayerTimeItem(PrayerType.MAGHRIB, formatTime(maghribH, maghribM), maghribH, maghribM, nextType == PrayerType.MAGHRIB, currentHour > maghribHour)
        val ishaItem = PrayerTimeItem(PrayerType.ISHA, formatTime(ishaH, ishaM), ishaH, ishaM, nextType == PrayerType.ISHA, currentHour > ishaHour)

        val nextItem = when (nextType) {
            PrayerType.FAJR -> fajrItem
            PrayerType.SUNRISE -> sunriseItem
            PrayerType.DHUHR -> dhuhrItem
            PrayerType.ASR -> asrItem
            PrayerType.MAGHRIB -> maghribItem
            PrayerType.ISHA -> ishaItem
        }

        return CalculatedDayPrayers(
            fajr = fajrItem,
            sunrise = sunriseItem,
            dhuhr = dhuhrItem,
            asr = asrItem,
            maghrib = maghribItem,
            isha = ishaItem,
            nextPrayer = nextItem,
            remainingMillis = remainingMillis
        )
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunAngleDiff(angle: Double, lat: Double, declination: Double): Double {
        val latRad = degToRad(lat)
        val decRad = degToRad(declination)
        val d = -sin(degToRad(angle)) - sin(latRad) * sin(decRad)
        val cosD = d / (cos(latRad) * cos(decRad))
        val clamped = cosD.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clamped))
    }

    private fun asrHourAngle(altitudeRad: Double, lat: Double, declination: Double): Double {
        val latRad = degToRad(lat)
        val decRad = degToRad(declination)
        val d = sin(altitudeRad) - sin(latRad) * sin(decRad)
        val cosD = d / (cos(latRad) * cos(decRad))
        val clamped = cosD.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clamped))
    }

    private fun decimalToTime(time: Double): Pair<Int, Int> {
        var t = time
        if (t.isNaN()) return Pair(0, 0)
        t = fixHour(t)
        val hour = floor(t).toInt()
        val min = floor((t - hour) * 60.0 + 0.5).toInt()
        if (min >= 60) {
            return Pair((hour + 1) % 24, 0)
        }
        return Pair(hour % 24, min)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h12 = when (hour % 12) {
            0 -> 12
            else -> hour % 12
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, amPm)
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun degToRad(d: Double): Double = d * Math.PI / 180.0
    private fun radToDeg(r: Double): Double = r * 180.0 / Math.PI

    /**
     * Algorithmic Hijri Date Calculation (Islamic Calendar)
     */
    data class HijriDate(val day: Int, val monthIndex: Int, val monthNameArabic: String, val monthNameEnglish: String, val year: Int)

    private val hijriMonthsArabic = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val hijriMonthsEnglish = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun getHijriDate(calendar: Calendar = Calendar.getInstance(), dayAdjustment: Int = 0): HijriDate {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, dayAdjustment)

        val jd = getJulianDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        ).toInt()

        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l1 = l - 10631 * n + 354
        val j = (((10985 - l1) / 5316).toInt()) * ((50 * l1 / 17719).toInt()) +
                ((l1 / 5670).toInt()) * ((43 * l1 / 15238).toInt())
        val l2 = l1 - (((30 - j) / 15).toInt()) * ((17719 * j / 50).toInt()) -
                ((j / 16).toInt()) * ((15238 * j / 43).toInt()) + 29
        val m = ((24 * l2 / 709).toInt())
        val d = l2 - ((709 * m / 24).toInt())
        val y = 30 * n + j - 30

        val monthIdx = (m - 1).coerceIn(0, 11)
        val validDay = d.coerceIn(1, 30)

        return HijriDate(
            day = validDay,
            monthIndex = monthIdx,
            monthNameArabic = hijriMonthsArabic[monthIdx],
            monthNameEnglish = hijriMonthsEnglish[monthIdx],
            year = y
        )
    }
}
