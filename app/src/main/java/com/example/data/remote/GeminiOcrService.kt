package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.entity.SubjectEntity
import com.example.domain.ocr.OcrScheduleParser
import com.example.domain.ocr.ParsedScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class OcrResult {
    data class Success(val items: List<ParsedScheduleItem>) : OcrResult()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : OcrResult()
}

class GeminiOcrService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val PRIMARY_MODEL = "gemini-2.5-flash"
        private const val FALLBACK_MODEL = "gemini-1.5-flash"

        /**
         * Resolves the preconfigured Gemini API key if available in BuildConfig.
         */
        fun getBuildConfigApiKey(): String? {
            return try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                val value = field.get(null) as? String
                if (!value.isNullOrBlank() && value != "MY_GEMINI_API_KEY" && value != "default") {
                    value
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun analyzeTimetableImage(
        bitmap: Bitmap,
        apiKey: String,
        existingSubjects: List<SubjectEntity>
    ): OcrResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext OcrResult.Error(
                message = "يرجى إدخال مفتاح Gemini API للمتابعة.",
                isApiKeyMissing = true
            )
        }

        val base64Image = try {
            encodeBitmapToBase64(bitmap)
        } catch (e: Exception) {
            return@withContext OcrResult.Error("فشل في معالجة وضغط الصورة: ${e.localizedMessage}")
        }

        val prompt = buildExtractionPrompt(existingSubjects)
        val requestJson = buildRequestBody(prompt, base64Image)

        // Try primary model first, fallback if unavailable
        val result = executeGeminiRequest(PRIMARY_MODEL, apiKey, requestJson, existingSubjects)
        if (result is OcrResult.Error && result.message.contains("model", ignoreCase = true)) {
            executeGeminiRequest(FALLBACK_MODEL, apiKey, requestJson, existingSubjects)
        } else {
            result
        }
    }

    private fun executeGeminiRequest(
        model: String,
        apiKey: String,
        jsonBody: String,
        existingSubjects: List<SubjectEntity>
    ): OcrResult {
        val url = "$BASE_URL/$model:generateContent?key=$apiKey"
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val isAuthError = response.code == 400 || response.code == 403 || response.code == 401
                val errorMsg = if (isAuthError) {
                    "مفتاح API غير صالح أو غير مفعل. يرجى التحقق من المفتاح في إعدادات التطبيق."
                } else {
                    "خطأ من الخادم (${response.code}): ${parseErrorMessage(responseString)}"
                }
                return OcrResult.Error(errorMsg, isApiKeyMissing = isAuthError)
            }

            val extractedText = extractCandidateText(responseString)
            if (extractedText.isBlank()) {
                return OcrResult.Error("لم يتمكن الذكاء الاصطناعي من قراءة أي جدول في الصورة. تأكد من وضوح الصورة وإضاءتها.")
            }

            val parsedItems = OcrScheduleParser.parseGeminiResponse(extractedText, existingSubjects)
            if (parsedItems.isEmpty()) {
                OcrResult.Error("لم يتم العثور على حصص دراسية واضحة في الصورة. يرجى إعادة التصوير بزاوية مستقيمة.")
            } else {
                OcrResult.Success(parsedItems)
            }
        } catch (e: Exception) {
            OcrResult.Error("تعذر الاتصال بخدمة الذكاء الاصطناعي: ${e.localizedMessage ?: "تحقق من اتصالك بالإنترنت"}")
        }
    }

    private fun extractCandidateText(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            parts.getJSONObject(0).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseErrorMessage(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val error = root.optJSONObject("error")
            error?.optString("message", "خطأ غير معروف") ?: "خطأ غير معروف"
        } catch (e: Exception) {
            "فشل في استلام الرد"
        }
    }

    private fun buildExtractionPrompt(existingSubjects: List<SubjectEntity>): String {
        val subjectNames = existingSubjects.joinToString(", ") { it.name }
        return """
        You are an expert OCR and school timetable parsing AI specialized in Arabic, French, and bilingual Algerian / Arab school schedules (جدول أوقات / استعمال الزمن / جدول الحصص).
        Analyze this image of a school timetable carefully.
        The student currently has these subjects: [$subjectNames].

        Extract all scheduled classes/sessions from the timetable table grid.
        For each session, provide:
        - "dayOfWeek": One of "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY".
          (Note: الأحد/Dimanche -> "SUNDAY", الإثنين/Lundi -> "MONDAY", الثلاثاء/Mardi -> "TUESDAY", الأربعاء/Mercredi -> "WEDNESDAY", الخميس/Jeudi -> "THURSDAY", الجمعة/Vendredi -> "FRIDAY").
        - "startTime": Start time in 24h format "HH:mm" (e.g. "08:00", "09:00", "10:00", "13:00", "14:00").
        - "endTime": End time in 24h format "HH:mm" (e.g. "09:00", "10:00", "11:00", "14:00", "15:00").
        - "subjectName": The name of the subject (preferably in Arabic or matching the student's subjects, e.g. "اللغة العربية", "الرياضيات", "اللغة الفرنسية", "التربية العلمية والتكنولوجية", "التربية الإسلامية", "اللغة الإنجليزية", "التربية المدنية", "التاريخ والجغرافيا", "التربية التشكيلية والرسم", "التربية البدنية والرياضية", etc.).
        - "teacherName": Teacher name if mentioned, otherwise empty string.
        - "classroom": Room or class number if mentioned, otherwise empty string.
        - "lessonNote": Any special remark or note if written in the cell, otherwise empty string.

        Return ONLY a valid JSON array of objects with these exact keys.
        """.trimIndent()
    }

    private fun buildRequestBody(promptText: String, base64Image: String): String {
        val root = JSONObject()

        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        // Text prompt part
        val textPart = JSONObject().put("text", promptText)
        partsArray.put(textPart)

        // Image part
        val inlineData = JSONObject()
            .put("mimeType", "image/jpeg")
            .put("data", base64Image)
        val imagePart = JSONObject().put("inlineData", inlineData)
        partsArray.put(imagePart)

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        // Generation config forcing JSON output
        val generationConfig = JSONObject()
            .put("temperature", 0.1)
            .put("responseMimeType", "application/json")
        root.put("generationConfig", generationConfig)

        return root.toString()
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val scaled = scaleDownBitmap(bitmap, maxDimension = 1600)
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun scaleDownBitmap(realImage: Bitmap, maxDimension: Int): Bitmap {
        val width = realImage.width
        val height = realImage.height
        if (width <= maxDimension && height <= maxDimension) {
            return realImage
        }
        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (ratio > 1) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(realImage, targetWidth, targetHeight, true)
    }
}
