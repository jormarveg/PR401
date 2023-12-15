package com.jorge.pr401

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Composable que muestra un diálogo para introducir una nueva nota.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param onDismiss Función de callback ejecutada al cerrar el cuadro de diálogo.
 * @param onConfirm Función de callback ejecutada al confirmar la entrada de datos.
 * @param errorText Texto de error que se muestra si hay problemas con la entrada.
 */
@Composable
fun InputMarkDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    errorText: String
) {
    var id by remember { mutableStateOf("") }
    var mark by remember { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = "Añadir nota")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Introduce los datos.")
                Text(text = " - Se necesita un ID para diferenciar las notas. DNI, NIA, etc.")
                Text(text = " - La nota debe estar entre 1 y 10.")
                Text(text = errorText, color = Color.Red, fontWeight = FontWeight.Bold)
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(2f),
                        value = id, onValueChange = {
                            if (it.length <= 10) // 10 dígitos o menos
                                id = it
                        },
                        label = { Text(text = "ID") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = mark, onValueChange = {
                            if (it.length <= 2) // 2 dígitos o menos
                                mark = it
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = "Nota") },
                        singleLine = true
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                // desactivado si no se han rellenado ambos campos
                enabled = (id.isNotEmpty() && mark.isNotEmpty()),
                onClick = {
                    onConfirm(id, mark)
                }
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Composable que muestra un cuadro de diálogo de información.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param title Título del cuadro de diálogo.
 * @param text Texto de información a mostrar.
 * @param onConfirm Función de callback ejecutada al cerrar el cuadro de diálogo.
 */
@Composable
fun InfoDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Cerrar")
            }
        }
    )
}


/**
 * Composable que muestra un cuadro de diálogo de confirmación.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param title Título del cuadro de diálogo.
 * @param text Texto de confirmación a mostrar.
 * @param onConfirm Función de callback ejecutada al confirmar la acción.
 * @param onDismiss Función de callback ejecutada al cancelar la acción.
 */
@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("No")
            }
        },
    )
}
