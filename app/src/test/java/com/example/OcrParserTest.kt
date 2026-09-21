package com.example

import com.example.data.local.entity.SubjectEntity
import com.example.domain.model.DayOfWeekAr
import com.example.domain.ocr.OcrScheduleParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OcrParserTest {

    private val testSubjects = listOf(
        SubjectEntity(id = 1L, childId = 1L, name = "اللغة العربية", icon = "📖"),
        SubjectEntity(id = 2L, childId = 1L, name = "الرياضيات", icon = "📐"),
        SubjectEntity(id = 3L, childId = 1L, name = "اللغة الفرنسية (Français)", icon = "🇫🇷"),
        SubjectEntity(id = 4L, childId = 1L, name = "التربية الإسلامية", icon = "🕌"),
        SubjectEntity(id = 5L, childId = 1L, name = "التربية العلمية والتكنولوجية", icon = "🧪")
    )

    @Test
    fun testNormalizeDay_arabicAndFrench() {
        assertEquals(DayOfWeekAr.SUNDAY, OcrScheduleParser.normalizeDay("الأحد"))
        assertEquals(DayOfWeekAr.SUNDAY, OcrScheduleParser.normalizeDay("الاحد"))
        assertEquals(DayOfWeekAr.SUNDAY, OcrScheduleParser.normalizeDay("Dimanche"))
        assertEquals(DayOfWeekAr.SUNDAY, OcrScheduleParser.normalizeDay("sunday"))

        assertEquals(DayOfWeekAr.MONDAY, OcrScheduleParser.normalizeDay("الإثنين"))
        assertEquals(DayOfWeekAr.MONDAY, OcrScheduleParser.normalizeDay("الاثنين"))
        assertEquals(DayOfWeekAr.MONDAY, OcrScheduleParser.normalizeDay("Lundi"))
        assertEquals(DayOfWeekAr.MONDAY, OcrScheduleParser.normalizeDay("monday"))

        assertEquals(DayOfWeekAr.TUESDAY, OcrScheduleParser.normalizeDay("الثلاثاء"))
        assertEquals(DayOfWeekAr.TUESDAY, OcrScheduleParser.normalizeDay("Mardi"))

        assertEquals(DayOfWeekAr.WEDNESDAY, OcrScheduleParser.normalizeDay("الأربعاء"))
        assertEquals(DayOfWeekAr.WEDNESDAY, OcrScheduleParser.normalizeDay("Mercredi"))

        assertEquals(DayOfWeekAr.THURSDAY, OcrScheduleParser.normalizeDay("الخميس"))
        assertEquals(DayOfWeekAr.THURSDAY, OcrScheduleParser.normalizeDay("Jeudi"))

        assertEquals(DayOfWeekAr.FRIDAY, OcrScheduleParser.normalizeDay("الجمعة"))
        assertEquals(DayOfWeekAr.FRIDAY, OcrScheduleParser.normalizeDay("Vendredi"))
    }

    @Test
    fun testNormalizeTime_variousFormats() {
        assertEquals("08:00", OcrScheduleParser.normalizeTime("8:00", "08:00"))
        assertEquals("08:30", OcrScheduleParser.normalizeTime("8h30", "08:00"))
        assertEquals("09:00", OcrScheduleParser.normalizeTime("9", "09:00"))
        assertEquals("10:00", OcrScheduleParser.normalizeTime("10h", "10:00"))
        assertEquals("13:15", OcrScheduleParser.normalizeTime("13.15", "13:00"))
        assertEquals("14:45", OcrScheduleParser.normalizeTime("14:45", "14:00"))
    }

    @Test
    fun testMatchSubject_existingAndKeywords() {
        // Direct matching
        val match1 = OcrScheduleParser.matchSubject("اللغة العربية", testSubjects)
        assertEquals(1L, match1.first?.id)
        assertEquals("اللغة العربية", match1.second)

        // French keyword matching to Arabic subject
        val match2 = OcrScheduleParser.matchSubject("Français", testSubjects)
        assertEquals(3L, match2.first?.id)
        assertEquals("اللغة الفرنسية (Français)", match2.second)

        // Math keyword matching
        val match3 = OcrScheduleParser.matchSubject("Maths", testSubjects)
        assertEquals(2L, match3.first?.id)
        assertEquals("الرياضيات", match3.second)

        // Islamic education keyword
        val match4 = OcrScheduleParser.matchSubject("تربية إسلامية", testSubjects)
        assertEquals(4L, match4.first?.id)
        assertEquals("التربية الإسلامية", match4.second)

        // New subject not in existing list
        val match5 = OcrScheduleParser.matchSubject("التربية البدنية والرياضية", testSubjects)
        assertEquals(null, match5.first)
        assertEquals("التربية البدنية والرياضية", match5.second)
    }

    @Test
    fun testParseGeminiResponse_validJson() {
        val sampleJson = """
        [
          {
            "dayOfWeek": "SUNDAY",
            "startTime": "08:00",
            "endTime": "09:00",
            "subjectName": "اللغة العربية",
            "teacherName": "أ. أحمد",
            "classroom": "قاعة 1"
          },
          {
            "dayOfWeek": "MONDAY",
            "startTime": "09:00",
            "endTime": "10:00",
            "subjectName": "Maths",
            "teacherName": "أ. سمير",
            "classroom": "قاعة 3"
          },
          {
            "dayOfWeek": "TUESDAY",
            "startTime": "10:00",
            "endTime": "11:00",
            "subjectName": "التربية البدنية",
            "teacherName": "أ. كريم",
            "classroom": "الملعب"
          }
        ]
        """.trimIndent()

        val items = OcrScheduleParser.parseGeminiResponse(sampleJson, testSubjects)
        assertEquals(3, items.size)

        val first = items[0]
        assertEquals(DayOfWeekAr.SUNDAY, first.dayOfWeek)
        assertEquals("08:00", first.startTime)
        assertEquals("09:00", first.endTime)
        assertEquals(1L, first.matchedSubjectId)
        assertEquals("اللغة العربية", first.matchedSubjectName)
        assertEquals("أ. أحمد", first.teacherName)
        assertEquals("قاعة 1", first.classroom)
        assertTrue(first.isSelected)

        val second = items[1]
        assertEquals(DayOfWeekAr.MONDAY, second.dayOfWeek)
        assertEquals("09:00", second.startTime)
        assertEquals(2L, second.matchedSubjectId)
        assertEquals("الرياضيات", second.matchedSubjectName)

        val third = items[2]
        assertEquals(DayOfWeekAr.TUESDAY, third.dayOfWeek)
        assertEquals("10:00", third.startTime)
        assertEquals(null, third.matchedSubjectId) // New subject to be created
        assertEquals("🏃", third.icon)
    }

    @Test
    fun testParseGeminiResponse_withMarkdownFences() {
        val fencedJson = """
        ```json
        [
          {
            "dayOfWeek": "Lundi",
            "startTime": "08h00",
            "endTime": "09h00",
            "subjectName": "Français"
          }
        ]
        ```
        """.trimIndent()

        val items = OcrScheduleParser.parseGeminiResponse(fencedJson, testSubjects)
        assertEquals(1, items.size)
        assertEquals(DayOfWeekAr.MONDAY, items[0].dayOfWeek)
        assertEquals("08:00", items[0].startTime)
        assertEquals("09:00", items[0].endTime)
        assertEquals(3L, items[0].matchedSubjectId)
    }
}
