package ovh.gabrielhuav.flasklogin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun CrudScreen() {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var uiState by remember { mutableStateOf<UiState>(UiState.Loading) }
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var titleInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadTasks() {
        scope.launch {
            uiState = UiState.Loading
            try {
                val response = RetrofitClient.api.getTasks(SessionManager.bearerToken())
                if (response.isSuccessful) {
                    tasks = response.body() ?: emptyList()
                    uiState = UiState.Success
                } else {
                    uiState = UiState.Error("No se pudieron cargar las tareas")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) { loadTasks() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTask = null
                titleInput = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                else -> {
                    if (tasks.isEmpty()) {
                        Text("No tienes tareas todavía", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            items(tasks) { task ->
                                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(task.title, style = MaterialTheme.typography.bodyLarge)
                                            Text(if (task.done) "Completada" else "Pendiente")
                                        }
                                        IconButton(onClick = {
                                            editingTask = task
                                            titleInput = task.title
                                            showDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                try {
                                                    RetrofitClient.api.deleteTask(
                                                        SessionManager.bearerToken(), task.id
                                                    )
                                                    loadTasks()
                                                } catch (e: Exception) {
                                                    uiState = UiState.Error("No se pudo borrar")
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Borrar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingTask == null) "Nueva tarea" else "Editar tarea") },
            text = {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Título") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            if (editingTask == null) {
                                RetrofitClient.api.createTask(
                                    SessionManager.bearerToken(), TaskRequest(title = titleInput)
                                )
                            } else {
                                RetrofitClient.api.updateTask(
                                    SessionManager.bearerToken(), editingTask!!.id,
                                    TaskRequest(title = titleInput)
                                )
                            }
                            showDialog = false
                            loadTasks()
                        } catch (e: Exception) {
                            uiState = UiState.Error("No se pudo guardar")
                            showDialog = false
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}