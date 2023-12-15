package com.jorge.pr401

/**
 * Clase que representa una nota con un [id] y un valor [value].
 * El identificador puede ser cualquier cadena (NIA, DNI, etc.) para
 * diferenciar las notas en la lista.
 * El valor de la nota está limitado entre 0 y 10, donde 0 solo se utiliza con las
 * notas eliminadas o no inicializadas.
 */
class Mark {
    var id: String = ""  // solo el getter es public, hacemos el setter private
        private set
    var value = 0  // solo el getter es public, hacemos el setter private
        private set(value) {  // no puede ser menor que 0 ni mayor que 10 (0 servirá para eliminarla)
            field = when {
                value > 10 -> 10
                value < 0 -> 0
                else -> value
            }
        }

    /**
     * Inicializa la nota con un [id] y un valor [value].
     */
    fun initialize(id: String, value: Int) {
        this.id = id
        this.value = value
    }

    /**
     * Restablece la nota a su estado inicial, eliminando el id
     * y estableciendo el valor en 0.
     */
    fun reset() {
        this.id = ""
        this.value = 0
    }

    /**
     * Comprueba si la nota está inicializada, es decir,
     * si tiene un identificador y un valor distinto de 0.
     * @return `true` si la nota está inicializada, `false` si no lo está.
     */
    fun isInitialized(): Boolean {
        return !(this.id == "" && this.value == 0)
    }
}
