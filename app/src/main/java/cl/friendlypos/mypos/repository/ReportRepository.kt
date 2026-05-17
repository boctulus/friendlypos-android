package cl.friendlypos.mypos.repository

import android.util.Log
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
        val tag = "ReportRepository"
        val fechaDesde = fromDate?.format(isoFormatter)
        val fechaHasta = toDate?.let { "${it.format(isoFormatter)}T23:59:59" }
        Log.d(tag, "getSales() → fechaDesde=$fechaDesde  fechaHasta=$fechaHasta")

        val response = try {
            Log.d(tag, "calling ApiClient.service.getSales()...")
            val r = ApiClient.service.getSales(
                fechaDesde = fechaDesde,
                fechaHasta = fechaHasta
            )
            Log.d(tag, "API call returned → success=${r.success}  data.size=${r.data?.size}  total=${r.total}")
            r
        } catch (e: Exception) {
            Log.e(tag, "API call THREW: ${e.javaClass.name} — ${e.message}", e)
            throw e
        }

        if (!response.success) {
            Log.w(tag, "response.success=false → returning emptyList")
            return emptyList()
        }

        return try {
            val list = response.data?.map { it.toSaleReport() } ?: emptyList()
            Log.d(tag, "toSaleReport() mapped ${list.size} items OK")
            list
        } catch (e: Exception) {
            Log.e(tag, "toSaleReport() FAILED: ${e.javaClass.simpleName} — ${e.message}", e)
            emptyList()
        }
    }
}
