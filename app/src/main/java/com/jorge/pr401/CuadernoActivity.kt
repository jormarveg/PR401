package com.jorge.pr401

import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.jorge.pr401.ui.theme.PR401Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

const val TITLE = "Cuaderno de profesor"

/**
 * Activity principal de la aplicación.
 */
class CuadernoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 'dynamicColor = false' fuerza a respetar el tema
            PR401Theme(darkTheme = false, dynamicColor = false) {
                MainScreen(TITLE)
            }
        }
    }
}

/**
 * Muestra una snackbar con el mensaje [msg].
 */
fun showSnackbar(
    snackbarHost: SnackbarHostState,
    scope: CoroutineScope,
    msg: String
) {
    // lanza tarea asíncrona
    scope.launch {
        // si ya hay una mostrándose, la descarta
        snackbarHost.currentSnackbarData?.dismiss()
        snackbarHost.showSnackbar(msg)
    }
}

/**
 * Pantalla principal de la aplicación.
 *
 * @param title Título de la pantalla.
 * @param modifier Modificador opcional para aplicar a la pantalla.
 */
@Composable
fun MainScreen(title: String, modifier: Modifier = Modifier) {
    // State necesario para gestionar la snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    // CoroutineScope para lanzar tareas asíncronas, en este caso, mostrar snackbar
    val scope = rememberCoroutineScope()
    fun snackbar(msg: String) = showSnackbar(snackbarHostState, scope, msg)
    var studentsQuantity by remember { mutableIntStateOf(0) }
    val marksArray by remember(studentsQuantity) {
        mutableStateOf(
            if (studentsQuantity > 0)
                Array(studentsQuantity) { Mark() }  // array de notas
            else null
        )
    }
    var showInputDialog by remember { mutableStateOf(false) }
    // variable para forzar una recomposición al editar el array
    // porque Compose no observa los cambios en los elementos de arrays
    var triggerUpdate by remember { mutableStateOf(false) }
    fun triggerUpdate() {
        triggerUpdate = !triggerUpdate
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // indicamos al Scaffold que se va a mostrar una snackbar
        // Scaffold se encarga de colocarla donde corresponda (abajo)
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // lo mismo con la barra superior
        topBar = { TopBar(title = title) },
        floatingActionButton = {
            if (studentsQuantity > 0)
                FABAdd {
                    if (countInitializedMarks(marksArray!!) >= studentsQuantity)
                        snackbar("No puedes insertar más notas")
                    else showInputDialog = true
                }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues -> // Scaffold calcula y devuelve los paddings
        MainLayout(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            marksArray,
            onQuantitySet = { studentsQuantity = it },
            onTriggerUpdate = { triggerUpdate() },
            triggerUpdate,
            showSnackbar = ::snackbar
        )
        if (showInputDialog) {
            var errorText by remember { mutableStateOf("") }
            InputMarkDialog(
                onDismiss = {
                    showInputDialog = false
                },
                onConfirm = { id, markString ->
                    if (idExists(marksArray!!, id))
                        errorText = "Ya existe una nota para ese ID."
                    else if (!validId(id))
                        errorText = "El ID solo puede tener letras y números."
                    else {
                        checkIfValidMark(
                            markString,
                            onValid = { validMark ->
                                showInputDialog = false
                                // si se añade la nota, actualizamos triggerUpdate
                                // para forzar una recomposición
                                if (addMark(marksArray!!, id, validMark))
                                    triggerUpdate()
                            },
                            onError = {
                                errorText = it
                            }  // muestra snackbar con el mensaje de onError
                        )
                    }
                }, errorText = errorText
            )
        }
    }
}

/**
 * Diseño principal de la aplicación con una columna.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param marksArray Array de [Mark] que contiene las notas de los estudiantes.
 * @param onQuantitySet Función de callback ejecutada al establecer la cantidad de estudiantes.
 * @param onTriggerUpdate Función de callback para forzar una recomposición de la lista.
 * @param triggerUpdate Boolean para forzar una recomposición de la lista.
 * @param showSnackbar Función para mostrar la snackbar con un mensaje.
 */
@Composable
fun MainLayout(
    modifier: Modifier = Modifier,
    marksArray: Array<Mark>?,
    onQuantitySet: (Int) -> Unit,
    onTriggerUpdate: () -> Unit,
    triggerUpdate: Boolean,
    showSnackbar: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // si el array no está inicializado preguntamos por la cantidad de estudiantes
        if (marksArray == null) {
            var studentsText by remember { mutableStateOf("") }
            Text(text = "¿Cuántos alumnos hay?")
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(  // textfield numérico para la cantidad de estudiantes
                    modifier = Modifier.width(96.dp),
                    value = studentsText, onValueChange = { studentsText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    checkIfValidQuantity(studentsText,
                        onValid = onQuantitySet,
                        onError = {
                            showSnackbar(it)
                        })
                }) {
                    Text(text = "Aceptar")
                }
            }
            // si el array está inicializado mostramos botones y lista
        } else {
            val marksQuantity = countInitializedMarks(marksArray)
            Text(
                "$marksQuantity notas de ${marksArray.size}",
                fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            if (marksQuantity > 0) {
                var showHigherMarkDialog by remember { mutableStateOf(false) }
                var higherMark by remember { mutableStateOf(Pair(0, 0)) }
                var showMeanDialog by remember { mutableStateOf(false) }
                var showRemoveAllDialog by remember { mutableStateOf(false) }
                var indexToDelete by remember { mutableIntStateOf(-1) }
                ActionButtons(
                    modifier = Modifier.fillMaxWidth(),
                    onHigherClick = {
                        higherMark = getHigherMark(marksArray)
                        showHigherMarkDialog = true
                    },
                    onMeanClick = { showMeanDialog = true },
                    onRemoveAllClick = { showRemoveAllDialog = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    itemsIndexed(marksArray) { index, mark ->
                        // la función key() fuerza una recomposición cuando triggerUpdate cambia
                        key(triggerUpdate) {
                            if (mark.value > 0)
                                MarkItem(mark = mark, modifier = Modifier.fillMaxWidth(),
                                    onRemoveClicked = { indexToDelete = index })
                        }
                    }
                }
                when {  // mostramos los diálogos cuando sea preciso
                    (showHigherMarkDialog) -> InfoDialog(
                        title = "Nota más alta",
                        text = "Nota: ${higherMark.first}, posición en el array: ${higherMark.second}"
                    ) {
                        showHigherMarkDialog = false
                    }
                    (showMeanDialog) -> InfoDialog(
                        title = "Media eliminando el 15% de valores extremos",
                        text = "${trimmedMean(marksArray)}"
                    ) {
                        showMeanDialog = false
                    }
                    showRemoveAllDialog -> {
                        ConfirmDialog(
                            title = "Eliminar todas las notas",
                            text = "¿Seguro que quieres eliminar todas las notas?",
                            onConfirm = {
                                removeAll(marksArray)
                                onTriggerUpdate()
                                showRemoveAllDialog = false
                            }, onDismiss = { showRemoveAllDialog = false })
                    }
                    (indexToDelete != -1) -> {
                        val id = "#${marksArray[indexToDelete].id}"
                        ConfirmDialog(
                            title = "Eliminar nota",
                            text = "¿Seguro que quieres eliminar la nota con id $id?",
                            onConfirm = {
                                removeMark(marksArray, indexToDelete)
                                onTriggerUpdate()
                                Log.d("Nota eliminada", "Índice $indexToDelete")
                                showSnackbar("Nota $id eliminada")
                                indexToDelete = -1
                            }, onDismiss = { indexToDelete = -1 })
                    }
                }
            }
        }
    }
}

/**
 * Composable que muestra botones para las operaciones relacionadas con las notas.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param onHigherClick Función de callback ejecutada al hacer clic en "Nota más alta".
 * @param onMeanClick Función de callback ejecutada al hacer clic en "Media".
 * @param onRemoveAllClick Función de callback ejecutada al hacer clic en "Borrar todas".
 */
@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    onHigherClick: () -> Unit,
    onMeanClick: () -> Unit,
    onRemoveAllClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            FilledTonalButton(onClick = onHigherClick) {
                Text(text = "Nota más alta")
            }
            FilledTonalButton(onClick = onMeanClick) {
                Text(text = "Media")
            }
        }
        FilledTonalButton(onClick = onRemoveAllClick) {
            Text(text = "Borrar todas")
        }
    }
}

/**
 * Composable que representa un elemento de lista para mostrar una nota.
 *
 * @param onRemoveClicked Función de callback ejecutada al hacer clic en el ícono de eliminar.
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param mark Objeto [Mark] que contiene información de la nota a mostrar.
 */
@Composable
fun MarkItem(
    onRemoveClicked: () -> Unit,
    modifier: Modifier = Modifier,
    mark: Mark,
) {
    ListItem(modifier = modifier, headlineContent = {
        Row {
            Text("#${mark.id}", modifier = Modifier.weight(0.33f))
            Text(
                "${mark.value}", modifier = Modifier.weight(0.66f),
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar",
                modifier = Modifier.clickable { onRemoveClicked() }
            )
        }
    })
    Divider()
}

/**
 * Composable que dibuja un FAB para añadir nuevas notas.
 *
 * @param modifier Modificador opcional para aplicar al diseño.
 * @param onClick Función de callback ejecutada al hacer clic en el botón.
 */
@Composable
fun FABAdd(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(modifier = modifier, onClick = onClick) {
        Icon(Icons.Default.Add, "Añadir nueva nota")
    }
}

/**
 * TopBar es la barra superior de la aplicación.
 * Implementa CenterAlignedTopAppBar que es una barra superior con
 * título centrado.
 *
 * @param title Título de la barra superior.
 * @param modifier Modificador para aplicar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}