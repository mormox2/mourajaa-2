package com.example.domain.model

enum class MasteryLevel(val stars: Int, val percentage: Int, val labelAr: String, val recommendedMinutes: Int) {
    NEEDS_REVIEW(1, 20, "يحتاج مراجعة", 30),
    MEDIUM(2, 40, "متوسط", 25),
    GOOD(3, 60, "جيد", 20),
    VERY_GOOD(4, 80, "جيد جداً", 15),
    EXCELLENT(5, 100, "ممتاز 🌟", 10);

    companion object {
        fun fromLevel(level: Int): MasteryLevel {
            return entries.find { it.stars == level } ?: GOOD
        }
    }
}

enum class DayOfWeekAr(val key: String, val titleAr: String, val shortAr: String, val dayNumber: String) {
    SUNDAY("SUNDAY", "الأحد", "أحد", "01"),
    MONDAY("MONDAY", "الإثنين", "إثنين", "02"),
    TUESDAY("TUESDAY", "الثلاثاء", "ثلاثاء", "03"),
    WEDNESDAY("WEDNESDAY", "الأربعاء", "أربعاء", "04"),
    THURSDAY("THURSDAY", "الخميس", "خميس", "05"),
    FRIDAY("FRIDAY", "الجمعة", "جمعة", "06");

    companion object {
        fun fromKey(key: String): DayOfWeekAr {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: MONDAY
        }

        fun getNextSchoolDay(currentKey: String): DayOfWeekAr {
            return when (currentKey.uppercase()) {
                "SUNDAY" -> MONDAY
                "MONDAY" -> TUESDAY
                "TUESDAY" -> WEDNESDAY
                "WEDNESDAY" -> THURSDAY
                "THURSDAY" -> FRIDAY
                "FRIDAY" -> SUNDAY
                else -> MONDAY
            }
        }
    }
}

data class SubjectWithReview(
    val id: Long,
    val childId: Long,
    val name: String,
    val icon: String,
    val mastery: MasteryLevel,
    val reviewMinutes: Int,
    val lessonNote: String = "",
    val timeSlot: String = "",
    val scheduledReviewTime: String? = null,
    val scheduledReminderEnabled: Boolean = false
)

data class NightReviewPlan(
    val targetDay: DayOfWeekAr,
    val items: List<SubjectWithReview>,
    val totalMinutes: Int
)

data class GradeInfo(
    val title: String,
    val categoryAr: String,
    val stage: String,
    val icon: String
)

object SchoolGradeCatalog {
    val ALL_GRADES: List<GradeInfo> = listOf(
        // التعليم التحضيري وما قبل المدرسي
        GradeInfo("التحضيري / روضة الأطفال (Maternelle)", "ما قبل المدرسي", "تحضيري", "🌱"),
        
        // التعليم الابتدائي (Primaire)
        GradeInfo("السنة الأولى ابتدائي (1AP)", "التعليم الابتدائي", "ابتدائي", "🎒"),
        GradeInfo("السنة الثانية ابتدائي (2AP)", "التعليم الابتدائي", "ابتدائي", "✏️"),
        GradeInfo("السنة الثالثة ابتدائي (3AP)", "التعليم الابتدائي", "ابتدائي", "📚"),
        GradeInfo("السنة الرابعة ابتدائي (4AP)", "التعليم الابتدائي", "ابتدائي", "📐"),
        GradeInfo("السنة الخامسة ابتدائي (5AP)", "التعليم الابتدائي", "ابتدائي", "🎓"),
        GradeInfo("السنة السادسة ابتدائي (6AP)", "التعليم الابتدائي", "ابتدائي", "🌟"),

        // التعليم المتوسط / الإعدادي (Collège / Moyen)
        GradeInfo("السنة الأولى متوسط (1AM / 6ème)", "التعليم المتوسط", "متوسط", "🔬"),
        GradeInfo("السنة الثانية متوسط (2AM / 5ème)", "التعليم المتوسط", "متوسط", "🧪"),
        GradeInfo("السنة الثالثة متوسط (3AM / 4ème)", "التعليم المتوسط", "متوسط", "📐"),
        GradeInfo("السنة الرابعة متوسط - شهادة BEM (3ème)", "التعليم المتوسط", "متوسط", "🏆"),

        // التعليم الثانوي (Lycée)
        GradeInfo("السنة الأولى ثانوي (جذع مشترك علوم / آداب)", "التعليم الثانوي", "ثانوي", "🏛️"),
        GradeInfo("السنة الثانية ثانوي (علمي / أدبي / رياضي)", "التعليم الثانوي", "ثانوي", "⚡"),
        GradeInfo("السنة الثالثة ثانوي - شهادة البكالوريا (BAC)", "التعليم الثانوي", "ثانوي", "🎯")
    )
}

enum class BadgeTier(val labelAr: String, val colorHex: Long) {
    BRONZE("برونزي", 0xFFCD7F32),
    SILVER("فضي", 0xFF9E9E9E),
    GOLD("ذهبي", 0xFFFFD700),
    DIAMOND("ألماسي", 0xFF00BCD4)
}

enum class BadgeCategory(val labelAr: String) {
    ALL("الكل"),
    STUDY_TASKS("المهام والدروس"),
    STREAK("المواظبة"),
    FOCUS("التركيز"),
    MASTERY("العلامات الكاملة")
}

data class BadgeDefinition(
    val code: String,
    val titleAr: String,
    val descriptionAr: String,
    val icon: String,
    val category: BadgeCategory,
    val tier: BadgeTier,
    val starsAward: Int,
    val targetCount: Int,
    val requirementDescriptionAr: String
)

data class BadgeItem(
    val definition: BadgeDefinition,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String? = null
)

object BadgeCatalog {
    val ALL_DEFINITIONS: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            code = "first_step",
            titleAr = "أول خطوة في النجاح",
            descriptionAr = "أتممت أول جلسة مراجعة لمادة دراسية بنجاح!",
            icon = "🌟",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.BRONZE,
            starsAward = 50,
            targetCount = 1,
            requirementDescriptionAr = "إكمال جلسة مراجعة واحدة لمادة مدرسية"
        ),
        BadgeDefinition(
            code = "daily_champion",
            titleAr = "بطل المذاكرة اليومية",
            descriptionAr = "أتممت مهام خطة المراجعة المسائية المجدولة كاملة!",
            icon = "🏆",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.GOLD,
            starsAward = 150,
            targetCount = 3,
            requirementDescriptionAr = "إكمال 3 جلسات مراجعة للمهام المجدولة"
        ),
        BadgeDefinition(
            code = "focus_master",
            titleAr = "شعلة التركيز",
            descriptionAr = "أنجزت جلسة مراجعة كاملة مع تفعيل وضع قفل الهاتف والتركيز التام!",
            icon = "⚡",
            category = BadgeCategory.FOCUS,
            tier = BadgeTier.SILVER,
            starsAward = 100,
            targetCount = 1,
            requirementDescriptionAr = "إكمال جلسة مراجعة كاملة أثناء تفعيل قفل الهاتف"
        ),
        BadgeDefinition(
            code = "arabic_hero",
            titleAr = "فارس لغة الضاد",
            descriptionAr = "أتممت مراجعة دروس اللغة العربية والقراءة وحفظ النصوص!",
            icon = "📖",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.SILVER,
            starsAward = 80,
            targetCount = 2,
            requirementDescriptionAr = "إكمال جلستي مراجعة لمادة اللغة العربية"
        ),
        BadgeDefinition(
            code = "french_star",
            titleAr = "نجم اللغة الفرنسية",
            descriptionAr = "أتممت مراجعة وحفظ مفردات وقواعد اللغة الفرنسية بنجاح!",
            icon = "🇫🇷",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.SILVER,
            starsAward = 80,
            targetCount = 2,
            requirementDescriptionAr = "إكمال جلستي مراجعة لمادة اللغة الفرنسية"
        ),
        BadgeDefinition(
            code = "math_wizard",
            titleAr = "عبقري الرياضيات",
            descriptionAr = "قمت بحل المسائل الحسابية وتمارين الرياضيات بتركيز عالٍ!",
            icon = "🔢",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.SILVER,
            starsAward = 80,
            targetCount = 2,
            requirementDescriptionAr = "إكمال جلستي مراجعة لمادة الرياضيات والنشاط العلمي"
        ),
        BadgeDefinition(
            code = "streak_3",
            titleAr = "سلسلة الإصرار (3 أيام)",
            descriptionAr = "حافظت على المذاكرة والمراجعة اليومية لمدة 3 أيام متتالية دون انقطاع!",
            icon = "🔥",
            category = BadgeCategory.STREAK,
            tier = BadgeTier.GOLD,
            starsAward = 200,
            targetCount = 3,
            requirementDescriptionAr = "المواظبة على جلسات المذاكرة لـ 3 أيام متتالية"
        ),
        BadgeDefinition(
            code = "knowledge_hour",
            titleAr = "ساعة من المعرفة",
            descriptionAr = "جمعت أكثر من 60 دقيقة من المذاكرة والمراجعة النشطة المركزة!",
            icon = "⏱️",
            category = BadgeCategory.FOCUS,
            tier = BadgeTier.GOLD,
            starsAward = 150,
            targetCount = 60,
            requirementDescriptionAr = "تجميع 60 دقيقة من المراجعة الفعلية"
        ),
        BadgeDefinition(
            code = "perfect_mastery",
            titleAr = "درع العلامة الكاملة",
            descriptionAr = "حققت تقييم ممتاز (100%) في إتقان المادة بعد انتهاء الجلسة!",
            icon = "💯",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.DIAMOND,
            starsAward = 250,
            targetCount = 1,
            requirementDescriptionAr = "الحصول على تقييم 5 نجوم (ممتاز 🌟) في المراجعة"
        ),
        BadgeDefinition(
            code = "night_scholar",
            titleAr = "مُراجع الليل الذكي",
            descriptionAr = "أنهيت خطة مراجعة الغد قبل موعد النوم بانتظام!",
            icon = "🦉",
            category = BadgeCategory.STUDY_TASKS,
            tier = BadgeTier.DIAMOND,
            starsAward = 300,
            targetCount = 5,
            requirementDescriptionAr = "إكمال 5 جلسات تحضير ليلية لحصص اليوم الموالي"
        )
    )
}

