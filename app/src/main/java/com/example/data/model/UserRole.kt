package com.example.data.model

enum class UserRole(
    val titleAr: String,
    val badge: String,
    val description: String,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canApprove: Boolean
) {
    SUPERVISOR(
        titleAr = "د.ص أنس يحيى باقاضي (المشرف)",
        badge = "مشرف بصلاحيات كاملة",
        description = "كامل الصلاحيات: التعديل، الحفظ، الحذف، والاعتماد الرسمي للبيانات والوصفات",
        canEdit = true,
        canDelete = true,
        canApprove = true
    ),
    COLLEAGUE(
        titleAr = "حساب الزملاء والصيادلة",
        badge = "اطلاع واستفادة فقط",
        description = "صلاحيات التحميل، القراءة، والاطلاع فقط - لا يحفظ شيء بدون اعتماد المشرف الصيدلاني",
        canEdit = false,
        canDelete = false,
        canApprove = false
    )
}

data class YemenRemittanceNetwork(
    val name: String,
    val details: String,
    val id: String
)

data class SubscriptionDetails(
    val trialDaysTotal: Int = 30,
    val trialDaysRemaining: Int = 28,
    val plan4MonthsPrice: String = "25 ريال سعودي",
    val plan4MonthsDuration: String = "أربعة أشهر (4 أشهر)",
    val planAnnualPrice: String = "50 ريال سعودي",
    val planAnnualDuration: String = "اشتراك سنوي كامل (سنة كاملة)",
    val feePlanText: String = "التطبيق مجاني لمدة شهر، ثم 25 سعودي لمدة أربعة أشهر أو 50 سعودي اشتراك سنوي",
    val supervisorPhone: String = "776028506",
    val supervisorEmail: String = "ndbdbndbrh@gmail.com",
    val networks: List<YemenRemittanceNetwork> = listOf(
        YemenRemittanceNetwork("شركة القطيبي للصرافة", "حوالة باسم: أنس يحيى باقاضي • ت/ 776028506", "qutaibi"),
        YemenRemittanceNetwork("بنك الشامل للصرافة", "حوالة باسم: أنس يحيى باقاضي • ت/ 776028506", "shamil"),
        YemenRemittanceNetwork("بنك الكريمي للتمويل الأصغر (الكريمي إكسبرس / المميز)", "حساب الكريمي أو حوالة لرقم: 776028506", "kuraimi"),
        YemenRemittanceNetwork("خدمة حاسب (بنك اليمن والكويت)", "تحويل عبر حاسب إلى رقم: 776028506", "haseb"),
        YemenRemittanceNetwork("خدمة حسب", "تحويل مباشر لرقم المشرف: 776028506", "hesab"),
        YemenRemittanceNetwork("محفظة جوالي (بنك اليمن والخليج)", "إيداع أو تحويل لمحفظة رقم: 776028506", "jawwali"),
        YemenRemittanceNetwork("محفظة كاش (Cash)", "تحويل فوري لمحفظة كاش رقم: 776028506", "cash")
    )
)
