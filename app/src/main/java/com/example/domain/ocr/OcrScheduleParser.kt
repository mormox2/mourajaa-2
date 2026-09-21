package com.example.domain.ocr

import com.example.data.local.entity.SubjectEntity
import com.example.domain.model.DayOfWeekAr
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ParsedScheduleItem(
    val tempId: String = UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeekAr,
    val startTime: String,
    val endTime: String,
    val rawSubjectName: String,
    val matchedSubjectId: Long?,
    val matchedSubjectName: String,
    val icon: String,
    val teacherName: String = "",
    val classroom: String = "",
    val lessonNote: String = "",
    val isSelected: Boolean = true
)

object OcrScheduleParser {

    /**
     * Parses the JSON array returned by Gemini Vision model into structured [ParsedScheduleItem]s.
     */
    fun parseGeminiResponse(jsonText: String, existingSubjects: List<SubjectEntity>): List<ParsedScheduleItem> {
        val cleanJson = extractJsonArray(jsonText)
        val items = mutableListOf<ParsedScheduleItem>()

        try {
            val jsonArray = JSONArray(cleanJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dayStr = obj.optString("dayOfWeek", "")
                val startTimeStr = obj.optString("startTime", "")
                val endTimeStr = obj.optString("endTime", "")
                val subjectNameStr = obj.optString("subjectName", "").trim()
                val teacherStr = obj.optString("teacherName", "").trim()
                val classroomStr = obj.optString("classroom", "").trim()
                val noteStr = obj.optString("lessonNote", "").trim()

                if (subjectNameStr.isBlank()) continue

                val day = normalizeDay(dayStr)
                val startTime = normalizeTime(startTimeStr, default = "08:00")
                val endTime = normalizeTime(endTimeStr, default = calculateDefaultEndTime(startTime))

                val (matchedSubject, finalSubjectName) = matchSubject(subjectNameStr, existingSubjects)
                val icon = matchedSubject?.icon ?: guessIconForSubject(finalSubjectName)

                items.add(
                    ParsedScheduleItem(
                        dayOfWeek = day,
                        startTime = startTime,
                        endTime = endTime,
                        rawSubjectName = subjectNameStr,
                        matchedSubjectId = matchedSubject?.id,
                        matchedSubjectName = finalSubjectName,
                        icon = icon,
                        teacherName = teacherStr,
                        classroom = classroomStr,
                        lessonNote = noteStr,
                        isSelected = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort items logically by day and start time
        return items.sortedWith(
            compareBy<ParsedScheduleItem> { daySortOrder(it.dayOfWeek) }
                .thenBy { it.startTime }
        )
    }

    private fun extractJsonArray(text: String): String {
        val trimmed = text.trim()
        val startIndex = trimmed.indexOf('[')
        val endIndex = trimmed.lastIndexOf(']')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            trimmed.substring(startIndex, endIndex + 1)
        } else {
            trimmed
        }
    }

    fun normalizeDay(raw: String): DayOfWeekAr {
        val lower = raw.trim().lowercase()
        return when {
            // Sunday (الأحد / Dimanche)
            lower.contains("أحد") || lower.contains("احد") || lower.contains("dimanche") || lower == "sunday" || lower == "sun" -> DayOfWeekAr.SUNDAY

            // Monday (الإثنين / Lundi)
            lower.contains("إثنين") || lower.contains("اثنين") || lower.contains("lundi") || lower == "monday" || lower == "mon" -> DayOfWeekAr.MONDAY

            // Tuesday (الثلاثاء / Mardi)
            lower.contains("ثلاثاء") || lower.contains("ثلاثا") || lower.contains("mardi") || lower == "tuesday" || lower == "tue" -> DayOfWeekAr.TUESDAY

            // Wednesday (الأربعاء / Mercredi)
            lower.contains("أربعاء") || lower.contains("اربعاء") || lower.contains("mercredi") || lower == "wednesday" || lower == "wed" -> DayOfWeekAr.WEDNESDAY

            // Thursday (الخميس / Jeudi)
            lower.contains("خميس") || lower.contains("jeudi") || lower == "thursday" || lower == "thu" -> DayOfWeekAr.THURSDAY

            // Friday (الجمعة / Vendredi)
            lower.contains("جمعة") || lower.contains("جمعه") || lower.contains("vendredi") || lower == "friday" || lower == "fri" -> DayOfWeekAr.FRIDAY

            else -> DayOfWeekAr.fromKey(raw)
        }
    }

    fun normalizeTime(raw: String, default: String): String {
        val cleaned = raw.trim().replace("h", ":", ignoreCase = true).replace("H", ":")
        val regex = Regex("""(\d{1,2})[:.](\d{2})""")
        val match = regex.find(cleaned)
        if (match != null) {
            val hours = match.groupValues[1].toIntOrNull() ?: 8
            val minutes = match.groupValues[2].toIntOrNull() ?: 0
            return String.format("%02d:%02d", hours.coerceIn(0, 23), minutes.coerceIn(0, 59))
        }

        // Handle single hour e.g. "8", "08", "9"
        val singleHourRegex = Regex("""^(\d{1,2})$""")
        val singleMatch = singleHourRegex.find(cleaned)
        if (singleMatch != null) {
            val h = singleMatch.groupValues[1].toIntOrNull() ?: 8
            return String.format("%02d:00", h.coerceIn(0, 23))
        }

        return default
    }

    private fun calculateDefaultEndTime(startTime: String): String {
        return try {
            val parts = startTime.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val endH = (h + 1).coerceAtMost(23)
            String.format("%02d:%02d", endH, m)
        } catch (e: Exception) {
            "09:00"
        }
    }

    /**
     * Matches raw extracted subject name with the child's existing subjects,
     * or canonicalizes it if not yet existing.
     */
    fun matchSubject(extracted: String, existingSubjects: List<SubjectEntity>): Pair<SubjectEntity?, String> {
        val clean = extracted.trim()
        val lower = clean.lowercase()

        // 1. Exact or contains match in existing subjects
        val directMatch = existingSubjects.find { sub ->
            sub.name.equals(clean, ignoreCase = true) ||
                    sub.name.contains(clean, ignoreCase = true) ||
                    clean.contains(sub.name, ignoreCase = true)
        }
        if (directMatch != null) return Pair(directMatch, directMatch.name)

        // 2. Keyword matching across common Arabic and French subject terms
        for (sub in existingSubjects) {
            val subLower = sub.name.lowercase()
            if (isSameSubjectCategory(lower, subLower)) {
                return Pair(sub, sub.name)
            }
        }

        // 3. Canonical name if not found in existing
        val canonicalName = getCanonicalSubjectName(clean)
        val canonicalMatch = existingSubjects.find {
            it.name.contains(canonicalName, ignoreCase = true) || canonicalName.contains(it.name, ignoreCase = true)
        }
        if (canonicalMatch != null) {
            return Pair(canonicalMatch, canonicalMatch.name)
        }

        return Pair(null, canonicalName)
    }

    private fun isSameSubjectCategory(a: String, b: String): Boolean {
        val arabicTerms = listOf("عربي", "عربية", "لغة عربية", "arabe", "langue arabe", "قراءة", "إملاء", "نحو")
        val mathTerms = listOf("رياضيات", "حساب", "math", "maths", "mathematiques", "géométrie", "هندسة")
        val frenchTerms = listOf("فرنسية", "فرنسي", "francais", "français", "french")
        val englishTerms = listOf("انجليزية", "إنجليزية", "anglais", "english")
        val islamicTerms = listOf("إسلامية", "اسلامية", "تربية إسلامية", "دين", "قرآن", "islamique", "education islamique")
        val scienceTerms = listOf("علوم", "علمية", "تربية علمية", "science", "sciences", "svt", "nature")
        val physicsTerms = listOf("فيزياء", "فيزيائية", "physique", "chimie", "كيمياء")
        val civicsTerms = listOf("مدنية", "تربية مدنية", "civique")
        val historyGeoTerms = listOf("تاريخ", "جغرافيا", "اجتماعيات", "histoire", "geographie", "géo")
        val artTerms = listOf("رسم", "تشكيلية", "أشغال", "موسيقى", "dessin", "art", "musique")
        val sportsTerms = listOf("رياضة", "تربية بدنية", "بدنية", "sport", "eps")

        val categories = listOf(
            arabicTerms, mathTerms, frenchTerms, englishTerms,
            islamicTerms, scienceTerms, physicsTerms, civicsTerms,
            historyGeoTerms, artTerms, sportsTerms
        )

        return categories.any { list ->
            list.any { a.contains(it) } && list.any { b.contains(it) }
        }
    }

    private fun getCanonicalSubjectName(raw: String): String {
        val lower = raw.trim().lowercase()
        return when {
            lower.contains("عرب") || lower.contains("arabe") -> "اللغة العربية"
            lower.contains("رياضة") || lower.contains("بدن") || lower.contains("sport") || lower.contains("eps") -> "التربية البدنية والرياضية"
            lower.contains("رياضيات") || lower.contains("math") || lower.contains("حساب") -> "الرياضيات"
            lower.contains("فرنس") || lower.contains("franc") -> "اللغة الفرنسية"
            lower.contains("انجل") || lower.contains("إنجل") || lower.contains("angl") || lower.contains("engl") -> "اللغة الإنجليزية"
            lower.contains("إسلام") || lower.contains("اسلام") || lower.contains("دين") || lower.contains("قرآن") -> "التربية الإسلامية"
            lower.contains("فيزي") || lower.contains("phys") || lower.contains("كيميا") -> "العلوم الفيزيائية"
            lower.contains("علم") || lower.contains("طبيع") || lower.contains("svt") || lower.contains("scien") -> "التربية العلمية والتكنولوجية"
            lower.contains("مدن") || lower.contains("civ") -> "التربية المدنية"
            lower.contains("تاريخ") || lower.contains("جغراف") || lower.contains("اجتماع") || lower.contains("hist") -> "التاريخ والجغرافيا"
            lower.contains("رسم") || lower.contains("تشكيل") || lower.contains("art") || lower.contains("dessin") -> "التربية التشكيلية والرسم"
            lower.contains("موسيق") || lower.contains("musiq") -> "التربية الموسيقية"
            lower.contains("إعلام") || lower.contains("حاسوب") || lower.contains("inform") -> "الإعلام الآلي"
            else -> raw.trim()
        }
    }

    fun guessIconForSubject(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("عرب") || lower.contains("arabe") || lower.contains("قراءة") -> "📖"
            lower.contains("رياضة") || lower.contains("بدن") || lower.contains("sport") -> "🏃"
            lower.contains("رياضيات") || lower.contains("math") || lower.contains("حساب") -> "📐"
            lower.contains("فرنس") || lower.contains("franc") -> "🇫🇷"
            lower.contains("انجل") || lower.contains("إنجل") || lower.contains("angl") -> "🇬🇧"
            lower.contains("إسلام") || lower.contains("اسلام") || lower.contains("دين") -> "🕌"
            lower.contains("فيزي") || lower.contains("phys") -> "⚡"
            lower.contains("علم") || lower.contains("scien") || lower.contains("طبيع") -> "🧪"
            lower.contains("مدن") || lower.contains("civ") -> "⚖️"
            lower.contains("تاريخ") || lower.contains("جغراف") || lower.contains("hist") -> "🗺️"
            lower.contains("رسم") || lower.contains("تشكيل") || lower.contains("art") -> "🎨"
            lower.contains("رياضة") || lower.contains("بدن") || lower.contains("sport") -> "🏃"
            lower.contains("موسيق") || lower.contains("musiq") -> "🎵"
            lower.contains("إعلام") || lower.contains("حاسوب") || lower.contains("inform") -> "💻"
            else -> "📚"
        }
    }

    private fun daySortOrder(day: DayOfWeekAr): Int {
        return when (day) {
            DayOfWeekAr.SUNDAY -> 1
            DayOfWeekAr.MONDAY -> 2
            DayOfWeekAr.TUESDAY -> 3
            DayOfWeekAr.WEDNESDAY -> 4
            DayOfWeekAr.THURSDAY -> 5
            DayOfWeekAr.FRIDAY -> 6
        }
    }
}
