package com.ti4all.agendaapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ti4all.agendaapp.data.Event
import com.ti4all.agendaapp.data.EventViewModel
import com.ti4all.agendaapp.ui.theme.AgendaAppTheme
import coil.compose.rememberImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgendaAppTheme {
                EventScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EventList(event: Event, onClick: (Event) -> Unit, onEditClick: (Event) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded }, // Adicionando a ação de clique
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = event.imageUrl),
                contentDescription = "Event image",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .height(200.dp)
                    .clickable { onEditClick(event) }
            )

            if (expanded) {
                Text (text = "Title: ${event.title}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Time: ${event.time}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date: ${event.date}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "CEP: ${event.cep}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Street: ${event.street}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Number: ${event.number}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Neighborhood: ${event.neighborhood}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "City: ${event.city}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "State: ${event.state}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Description: ${event.description}")
            } else {
                Text (text = "Title: ${event.title}")
                Spacer(modifier = Modifier.height(8.dp))
            }


            Button(onClick = { expanded = !expanded }) {
                Text(text = "Expand description")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(viewModel: EventViewModel) {
    val eventList by viewModel.eventList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) } // Evento selecionado

    LaunchedEffect(Unit) {
        viewModel.listarTodos()  }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center) })
            },
        floatingActionButton = {FloatingActionButton(
                                onClick = { showDialog = true }
            ) {Icon(Icons.Filled.Add, contentDescription = "Adicionar evento")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(eventList) { event ->
                EventList(
                    event = event,
                    onClick = { selectedEvent = event; showDialog = true },
                    onEditClick = { selectedEvent = it; showDialog = true }
                )
            }
        }
        /*
        if (showDialog) {
            AgendaFormDialog(
                onDismissRequest = { showDialog = false }
            ) { nome, telefone ->
                viewModel.inserir(Agenda(nome = nome, telefone = telefone))
                showDialog = false
            }
        }

         */
        if (showDialog && selectedEvent != null) {
            EventFormDialog(
                event = selectedEvent!!, // Passa o evento selecionado
                isEditMode = true, // Indica que estamos editando
                onDismissRequest = { showDialog = false },
                onAddClick = { newEvent ->
                    viewModel.inserir(newEvent)
                    showDialog = false
                },
                // Evocando viewModel.atualizar
                onEditClick = { updatedEvent ->
                    viewModel.atualizar(updatedEvent) // Atualiza o evento
                    showDialog = false
                },
                onDeleteClick = { id ->
                    viewModel.deletar(id) // Chama a função de deletar
                    showDialog = false
                }
            )
        } else if (showDialog) {
            EventFormDialog(
                event = Event(title = "", time = "", date = "", cep = "", street = "", number = 0, neighborhood = "", city = "", state = "", description = "", imageUrl = ""), // Passa um novo objeto vazio para adicionar
                isEditMode = false, // Indica que estamos incluindo
                onDismissRequest = { showDialog = false },
                onAddClick = { newEvent ->
                    viewModel.inserir(newEvent) // Adiciona novo contato
                    showDialog = false
                },
                onEditClick = { /* Não faz nada, pois é um novo contato */ },
                onDeleteClick = { /* Não faz nada, pois não há id para novo contato */ }
            )
        }
    }
}
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun EventFormDialog(
        event: Event, // Novo parâmetro para receber o contato
        isEditMode: Boolean, // Sinaliza modo de operação da função
        onDismissRequest: () -> Unit,
        onAddClick: (Event) -> Unit, // Altera para receber um objeto Agenda
        onEditClick: (Event) -> Unit,
        onDeleteClick: (Int) -> Unit // Função para excluir o contato
    ) {
        val context = LocalContext.current
        val imageUri = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
        }

        val storagePermissionState = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
            ) {
                LazyColumn (
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {

                        var title by remember { mutableStateOf(event.title) }
                        var date by remember { mutableStateOf(event.date) }
                        var time by remember { mutableStateOf(event.time) }
                        var cep by remember { mutableStateOf(event.cep) }
                        var street by remember { mutableStateOf(event.street) }
                        var number by remember { mutableStateOf(event.number) }
                        var neighborhood by remember { mutableStateOf(event.neighborhood) }
                        var city by remember { mutableStateOf(event.city) }
                        var state by remember { mutableStateOf(event.state) }
                        var description by remember { mutableStateOf(event.description) }
                        var image by remember { mutableStateOf(event.imageUrl) }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = cep,
                            onValueChange = { cep = it },
                            label = { Text("CEP") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text("Street") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = number.toString(),
                            onValueChange = { number = it.toInt() },
                            label = { Text("Number") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            label = { Text("Neighborhood") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            if (storagePermissionState.status.isGranted) {
                                launcher.launch("image/*")
                            } else {
                                storagePermissionState.launchPermissionRequest()
                            }
                        }) {
                            Text("Select event banner image")
                        }

                        imageUri.value?.let {
                            image = it.toString()
                            Image(
                                painter = rememberImagePainter(data = it),
                                contentDescription = "Event selected banner image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }

                        // Usando Row para colocar os botões lado a lado
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    // Ajustando chamada da onAddClick
                                    onAddClick(
                                        event.copy(
                                            title = title,
                                            date = date,
                                            time = time,
                                            cep = cep,
                                            street = street,
                                            number = number,
                                            neighborhood = neighborhood,
                                            city = city,
                                            state = state,
                                            description = description,
                                            imageUrl = image
                                        )
                                    ) // Atualiza o evento
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(0.5f), // Para ocupar espaço igual
                                enabled = !isEditMode // Desabilitar se estiver em modo de edição
                            ) {
                                Text(" + ")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    onEditClick(
                                        event.copy(
                                            title = title,
                                            date = date,
                                            time = time,
                                            cep = cep,
                                            street = street,
                                            number = number,
                                            neighborhood = neighborhood,
                                            city = city,
                                            state = state,
                                            description = description,
                                            imageUrl = image
                                        )
                                    ) // Para editar contato
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    onDeleteClick(event.id) // Chama a função de deletar
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(0.5f) // Para ocupar espaço igual
                            ) {
                                Text(" - ")
                            }
                        }
                    }
                }
            }
        }
    }
