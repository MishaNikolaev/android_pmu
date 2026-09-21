package com.nmichail.android_pmu.data.repository

import com.nmichail.android_pmu.data.api.CbrApi
import com.nmichail.android_pmu.data.dto.MetallResponse
import com.nmichail.android_pmu.domain.model.GoldRate
import com.nmichail.android_pmu.domain.repository.GoldRateRepository
import org.simpleframework.xml.core.Persister
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoldRateRepositoryImpl(
    private val api: CbrApi
) : GoldRateRepository {

    companion object {
        private const val GOLD_CODE = "1"
    }

    override suspend fun loadRate(): GoldRate {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val to = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }

        val body = api.getMetal(
            dateFrom = format.format(from.time),
            dateTo = format.format(to.time)
        )

        val response = body.byteStream().use { stream ->
            Persister().read(MetallResponse::class.java, stream, false)
        }

        val value = response.records
            ?.asReversed()
            ?.firstOrNull { it.code == GOLD_CODE }
            ?.buy
            ?.let { parseRub(it) }
            ?: error("Не найден курс золота в ответе ЦБ")

        return GoldRate(
            valueRubPerGram = value,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    private fun parseRub(raw: String): Double? {
        return raw.trim()
            .replace(" ", "")
            .replace(',', '.')
            .toDoubleOrNull()
    }
}