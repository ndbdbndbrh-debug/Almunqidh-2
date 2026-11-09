package com.example.ui.util

import android.content.Context
import android.content.Intent
import com.example.data.model.PrescriptionScan

object ShareHelper {

    fun sharePrescription(context: Context, scan: PrescriptionScan) {
        val builder = StringBuilder()
        builder.appendLine("🌟 *تقرير فحص الوصفة الطبية - تطبيق المُنقذ الدوائي* 🌟")
        builder.appendLine("👨‍⚕️ *تحت إشراف:* د.ص أنس يحيى باقاضي")
        builder.appendLine("━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("📋 *عنوان الوصفة:* ${scan.title}")
        builder.appendLine("🏥 *الجهة / الطبيب:* ${scan.clinicOrDoctor}")
        builder.appendLine("📅 *التاريخ:* ${scan.date}")
        builder.appendLine("⚡ *دقة التحليل بالذكاء الاصطناعي:* ${scan.confidence}")
        builder.appendLine("━━━━━━━━━━━━━━━━━━━")
        builder.appendLine("")

        scan.items.forEachIndexed { index, item ->
            builder.appendLine("💊 *[دواء ${index + 1}]:* ${item.tradeName} (${item.dosageForm} - ${item.strength})")
            builder.appendLine("🔬 *المادة الفعالة:* ${item.genericName}")
            builder.appendLine("🏢 *الشركة والوكيل:* ${item.manufacturer} | الوكيل: ${item.localAgent}")
            builder.appendLine("⏱️ *الجرعة المكتوبة:* ${item.writtenDosage}")
            if (item.drugInteractions.isNotEmpty()) {
                builder.appendLine("⚠️ *التداخلات الدوائية:* ${item.drugInteractions.joinToString("، ")}")
            }
            if (item.medicalTips.isNotEmpty()) {
                builder.appendLine("💡 *نصائح المريض:* ${item.medicalTips.joinToString("، ")}")
            }
            builder.appendLine("✍️ *ملاحظة د.ص أنس باقاضي:* ${item.supervisorNotes}")
            builder.appendLine("-------------------")
        }

        builder.appendLine("")
        builder.appendLine("📝 *التقييم العام للمشرف الصيدلاني:*")
        builder.appendLine(scan.supervisorOverallComment)
        builder.appendLine("")
        builder.appendLine("✨ *تطبيق المُنقذ الدوائي* متاح مجاناً للجميع بدون أي قيود أو أكواد!")
        builder.appendLine("📲 حمّل التطبيق وشاركه لحماية المرضى ودعم الصيدليات.")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير الوصفة عبر:")
        context.startActivity(shareIntent)
    }

    fun shareAppInvitation(context: Context) {
        val text = """
            🌟 *تطبيق المنقذ الدوائي (Pharma Savior)* 🌟
            تحت إشراف: د.ص أنس يحيى باقاضي
            
            🧠 تطبيق ذكي لقراءة وفك رموز الوصفات الطبية اليدوية والمطبوعة بالذكاء الاصطناعي!
            
            المميزات:
            ✅ قراءة خطوط الأطباء الصعبة وفك رموز الروشتات بدقة فائقة
            ✅ تفصيل الأسماء التجارية والعلمية والشركات والوكلاء المعتمدين
            ✅ كشف فوري للتداخلات والتعارضات الدوائية الخطيرة
            ✅ مكتبة وأرشيف شامل لملفات ونشرات أدوية الشركات
            ✅ تدقيق وملاحظات واعتماد المشرف الصيدلاني د.ص أنس يحيى باقاضي
            
            التطبيق مجاني ومتاح للجميع بدون أي اشتراكات أو أكواد تفعيل!
            شارك التطبيق الآن مع أصدقائك وزملائك لحفظ سلامة المرضى 💊🩺
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تطبيق المنقذ الدوائي عبر:")
        context.startActivity(shareIntent)
    }
}
