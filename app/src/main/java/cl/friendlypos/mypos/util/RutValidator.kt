package cl.friendlypos.mypos.util

/**
 * Validación de RUT chileno (módulo 11).
 * Puerto del algoritmo usado en el backend
 * (`modules/sales/assets/js/customer-rut-autocomplete.js`).
 */
object RutValidator {

    /** Limpia puntos y guión, deja cuerpo + dígito verificador en mayúscula. */
    fun normalize(rut: String): String =
        rut.replace(".", "").replace("-", "").uppercase()

    fun isValid(rut: String): Boolean {
        val clean = normalize(rut)
        if (clean.length < 2) return false

        val body = clean.dropLast(1)
        val dv = clean.last()

        if (!body.all { it.isDigit() }) return false

        var sum = 0
        var multiplier = 2
        for (i in body.indices.reversed()) {
            sum += (body[i] - '0') * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }

        val expected = 11 - (sum % 11)
        val dvCalc = when (expected) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + expected
        }
        return dv == dvCalc
    }
}
