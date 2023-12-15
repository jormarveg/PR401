package com.jorge.pr401

import java.lang.NumberFormatException
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Verifica si el texto [text] es un número entero válido y positivo.
 * Llama a la función [onValid] si es válido, [onError] si no lo es.
 */
fun checkIfValidQuantity(
    text: String, onValid: (Int) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val intValue = text.toInt()
        if (intValue > 0) onValid(intValue)
        else onError("Debe ser un número mayor a 0.")
    } catch (e: NumberFormatException) {
        onError("Solo puedes introducir números enteros.")
    }
}

/**
 * Verifica si el valor [value] representa una nota válida (entre 1 y 10).
 * Llama a la función [onValid] si es válido, [onError] si no lo es.
 */
fun checkIfValidMark(
    value: String, onValid: (Int) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val intValue = value.toInt()
        if (intValue in 1..10) onValid(intValue)
        else onError("La nota debe estar entre 1 y 10.")
    } catch (e: NumberFormatException) {
        onError("Solo puedes introducir notas válidas.")
    }
}

/**
 * Cuenta las notas inicializadas en el array de [marks].
 */
fun countInitializedMarks(marks: Array<Mark>): Int {
    var count = 0
    for (m in marks)
        if (m.value > 0) count++
    return count
}

/**
 * Introduce una nota [mark] en el array [marks], en el primer lugar
 * disponible, es decir, donde haya una nota no inicializada.
 * Devuelve `true` si ha introducido la nota, `false` si no.
 */
fun addMark(marks: Array<Mark>, id: String, mark: Int): Boolean {
    for (m in marks) {
        if (!m.isInitialized()) {
            m.initialize(id, mark)
            return true
        }
    }
    return false // no hay espacio
}

/**
 * Elimina la nota en la posición [position] del array [marks].
 */
fun removeMark(marks: Array<Mark>, position: Int) {
    marks[position].reset()
}

/**
 * Comprueba si el formato del [id] es válido.
 */
fun validId(id: String): Boolean {
    val regex = "[a-zA-Z0-9]+".toRegex()
    return regex.matches(id)
}

/**
 * Verifica si el [id] ya existe en el array de [marks].
 */
fun idExists(marks: Array<Mark>, id: String): Boolean {
    for (m in marks)
        if (m.isInitialized() && m.id == id)
            return true
    return false
}

/**
 * Elimina todas las notas inicializadas en el array [marks].
 */
fun removeAll(marks: Array<Mark>) {
    for (i in marks.indices)
        if (marks[i].isInitialized()) removeMark(marks, i)
}

/**
 * Obtiene la nota más alta y su posición en el array [marks].
 */
fun getHigherMark(marks: Array<Mark>): Pair<Int, Int> {
    var highestMark = marks[0]
    var pos = 0
    for (i in marks.indices) {
        if (marks[i].value > highestMark.value) {
            highestMark = marks[i]
            pos = i
        }
    }
    return Pair(highestMark.value, pos)
}

/**
 * Calcula la media truncada de las notas en el array [marks],
 * excluyendo el 15% de los valores extremos. Para la media truncada se
 * suelen usar valores del 10% al 20%, esto ayuda a reducir la
 * sensibilidad de la media a valores atípicos.
 */
fun trimmedMean(marks: Array<Mark>): Double {
    // 7.5% de los más altos y 7.5% de los más bajos (15% del total)
    val percentageToExclude = 0.075f
    val sortedMarks = getSortedMarks(marks)  // ordenamos de - a +
    // cantidad de notas a excluir en ambos extremos
    val countToExclude = (sortedMarks.size * percentageToExclude).roundToInt()
    // media truncada excluyendo una cantidad
    var sum = 0.0
    for (i in countToExclude..sortedMarks.lastIndex - countToExclude)
        sum += sortedMarks[i].value
    val mean = sum / (sortedMarks.size - countToExclude * 2)
    return mean.round(2)
}

/**
 * Obtiene un array de notas ordenadas de menor a mayor a partir del array [marks].
 */
fun getSortedMarks(marks: Array<Mark>): Array<Mark> {
    val sorted = copyInitializedMarks(marks)  // excluimos las que son 0
    for (i in sorted.indices) {
        for (j in 0..sorted.lastIndex - 1 - i) {
            if (sorted[j].value > sorted[j + 1].value) {
                val aux = sorted[j]
                sorted[j] = sorted[j + 1]
                sorted[j + 1] = aux
            }
        }
    }
    return sorted
}

/**
 * Copia las notas inicializadas del array [marks] a un nuevo array.
 */
fun copyInitializedMarks(marks: Array<Mark>): Array<Mark> {
    // nuevo array con el tamaño de las notas inicializadas
    val copy = arrayOfNulls<Mark>(countInitializedMarks(marks))
    var copyIndex = 0
    for (mark in marks) {
        if (mark.isInitialized()) {
            copy[copyIndex] = mark
            copyIndex++
        }
    }
    return copy.requireNoNulls()
}


/**
 * Redondea el número decimal a [decimals] decimales.
 */
fun Double.round(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    val rounded = (this * factor).roundToInt()
    return rounded / factor
}