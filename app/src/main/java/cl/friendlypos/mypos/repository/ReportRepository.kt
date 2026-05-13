package cl.friendlypos.mypos.repository

import cl.friendlypos.mypos.api.ApiClient
import cl.friendlypos.mypos.model.SaleReport
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportRepository {

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getSales(
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null
    ): List<SaleReport> {
        val response = ApiClient.service.getSales(
            fechaDesde = fromDate?.format(isoFormatter),
            fechaHasta = toDate?.format(isoFormatter)
        )
        if (!response.success) return emptyList()
        return response.data?.map { it.toSaleReport() } ?: emptyList()
    }
}
