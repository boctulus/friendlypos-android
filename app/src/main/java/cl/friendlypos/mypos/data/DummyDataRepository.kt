package cl.friendlypos.mypos.data

import cl.friendlypos.mypos.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalDateTime

object DummyDataRepository {

    private val products = listOf(
        Product("1", "Coca Cola 350ml", "Bebida gaseosa", 1.50, 45, "Bebidas"),
        Product("2", "Pan Integral", "Pan de molde integral", 2.25, 12, "Panadería"),
        Product("3", "Leche Entera 1L", "Leche fresca entera", 3.80, 8, "Lácteos"),
        Product("4", "Arroz 1kg", "Arroz blanco premium", 4.50, 25, "Granos"),
        Product("5", "Pollo entero", "Pollo fresco por kg", 6.75, 5, "Carnes"),
        Product("6", "Manzanas Red", "Manzanas rojas por kg", 3.20, 18, "Frutas"),
        Product("7", "Detergente 500ml", "Detergente líquido", 5.90, 30, "Limpieza"),
        Product("8", "Yogurt Natural", "Yogurt natural 200g", 1.80, 22, "Lácteos"),
        Product("9", "Aceite Vegetal 1L", "Aceite de cocina", 4.20, 15, "Aceites"),
        Product("10", "Galletas Oreo", "Galletas de chocolate", 2.10, 40, "Snacks")
    )

    private val customers = listOf(
        Customer("1", "María González", "maria@email.com", "+1234567890", "Calle 123, Ciudad", 150.50, "2024-01-15"),
        Customer("2", "Juan Pérez", "juan@email.com", "+1234567891", "Av. Principal 456", 89.75, "2024-01-14"),
        Customer("3", "Ana Rodríguez", "ana@email.com", "+1234567892", "Plaza Central 789", 220.30, "2024-01-13"),
        Customer("4", "Carlos Martínez", "carlos@email.com", "+1234567893", "Barrio Norte 321", 95.80, "2024-01-12"),
        Customer("5", "Lucía Fernández", "lucia@email.com", "+1234567894", "Zona Sur 654", 310.25, "2024-01-11"),
        Customer("6", "Pedro Sánchez", "pedro@email.com", "+1234567895", "Centro 987", 78.90, "2024-01-10"),
        Customer("7", "Isabel Torres", "isabel@email.com", "+1234567896", "Residencial 147", 165.40, "2024-01-09"),
        Customer("8", "Miguel Ruiz", "miguel@email.com", "+1234567897", "Colonial 258", 125.60, "2024-01-08")
    )

    fun getProducts(): Flow<List<Product>> = flowOf(products)

    fun getCustomers(): Flow<List<Customer>> = flowOf(customers)

    fun getSalesReports(): Flow<List<SaleReport>> = flow {
        val salesReports = listOf(
            SaleReport(
                id = "PAY-001",
                customerName = "Juan Pérez",
                total = 150.50,
                paymentMethod = "Tarjeta de Crédito",
                date = LocalDate.now().minusDays(1),
                items = listOf(
                    // FIX: Pass the object, not just the .productName property
                    ProductSale("Coca Cola 350ml", 2, 1.50, 3.00),
                    ProductSale("Pan Integral", 1, 2.25, 2.25)
                )
            ),
            SaleReport(
                id = "PAY-002",
                customerName = "María García",
                total = 89.99,
                paymentMethod = "Efectivo",
                date = LocalDate.now().minusDays(2),
                items = listOf(
                    // FIX: Pass the object, not just the .productName property
                    ProductSale("Leche Entera 1L", 1, 3.80, 3.80),
                    ProductSale("Yogurt Natural", 2, 1.80, 3.60)
                )
            ),
            SaleReport(
                id = "PAY-003",
                customerName = "Ana Rodríguez",
                total = 220.30,
                paymentMethod = "Transferencia",
                date = LocalDate.now().minusDays(3),
                items = listOf(
                    // FIX: Pass the object, not just the .productName property
                    ProductSale("Arroz 1kg", 2, 4.50, 9.00),
                    ProductSale("Aceite Vegetal 1L", 1, 4.20, 4.20)
                )
            ),
            SaleReport(
                id = "PAY-004",
                customerName = "Carlos Martínez",
                total = 95.80,
                paymentMethod = "Efectivo",
                date = LocalDate.now().minusDays(4),
                items = listOf(
                    // FIX: Pass the object, not just the .productName property
                    ProductSale("Pollo entero", 1, 6.75, 6.75),
                    ProductSale("Manzanas Red", 2, 3.20, 6.40)
                )
            )
        )
        emit(salesReports)
    }

    fun getReportSummary(fromDate: String, toDate: String): Flow<ReportSummary> = flow {
        val salesReportsList = getSalesReports().first()
        val totalSales = salesReportsList.sumOf { it.total }

        // Crear datos del gráfico
        val chartData = salesReportsList.groupBy { it.date }
            .map { (date, sales) ->
                ChartData(
                    date = date,
                    amount = sales.sumOf { it.total }
                )
            }
            .sortedBy { it.date }

        emit(
            ReportSummary(
                totalCustomers = customers.size,
                totalProducts = products.size,
                totalSales = totalSales,
                dateRange = "$fromDate - $toDate",
                chartData = chartData
            )
        )
    }

    fun getNotifications(): Flow<List<Notification>> = flow {
        val notifications = listOf(
            Notification(
                id = "NOT-001",
                title = "Sistema actualizado",
                message = "El sistema se ha actualizado a la versión 2.1.0 con nuevas funcionalidades y mejoras de seguridad.",
                type = NotificationType.SUCCESS,
                timestamp = LocalDateTime.now().minusHours(1),
                isRead = false
            ),
            Notification(
                id = "NOT-002",
                title = "Stock bajo",
                message = "El producto 'Coca Cola 350ml' tiene stock bajo (5 unidades restantes).",
                type = NotificationType.WARNING,
                timestamp = LocalDateTime.now().minusHours(3),
                isRead = false
            ),
            Notification(
                id = "NOT-003",
                title = "Error en conexión",
                message = "No se pudo conectar con el servidor de pagos. Verifique su conexión a internet.",
                type = NotificationType.ERROR,
                timestamp = LocalDateTime.now().minusHours(5),
                isRead = true
            ),
            Notification(
                id = "NOT-004",
                title = "Nuevo cliente registrado",
                message = "Se ha registrado un nuevo cliente: Roberto Silva",
                type = NotificationType.INFO,
                timestamp = LocalDateTime.now().minusDays(1),
                isRead = true
            ),
            Notification(
                id = "NOT-005",
                title = "Backup completado",
                message = "El respaldo automático de datos se completó exitosamente.",
                type = NotificationType.SUCCESS,
                timestamp = LocalDateTime.now().minusDays(1).minusHours(6),
                isRead = false
            ),
            Notification(
                id = "NOT-006",
                title = "Mantenimiento programado",
                message = "El sistema entrará en mantenimiento el próximo domingo de 2:00 AM a 4:00 AM.",
                type = NotificationType.INFO,
                timestamp = LocalDateTime.now().minusDays(2),
                isRead = true
            ),
            Notification(
                id = "NOT-007",
                title = "Producto vencido",
                message = "El producto 'Leche Entera 1L' está próximo a vencer (vence en 2 días).",
                type = NotificationType.WARNING,
                timestamp = LocalDateTime.now().minusDays(3),
                isRead = false
            ),
            Notification(
                id = "NOT-008",
                title = "Configuración actualizada",
                message = "Las configuraciones de impresora han sido actualizadas correctamente.",
                type = NotificationType.SUCCESS,
                timestamp = LocalDateTime.now().minusDays(4),
                isRead = true
            )
        )
        emit(notifications)
    }

}