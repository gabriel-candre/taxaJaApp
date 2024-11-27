package com.ti4all.agendaapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

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

    fun Event.getFullAddres(): String {
        return "$street, $number - $neighborhood, $city/$state"
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                Text (text = "Nome: ${event.title}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Horário: ${event.time}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Data: ${event.date}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "CEP: ${event.cep}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Endereço: ${event.getFullAddres()}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Descrição: ${event.description}")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment =  Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Contract arrow"
                    )

                    Text(
                        text = "Fechar descrição",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }

            } else {
                Text (text = "Nome: ${event.title}")
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment =  Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mostrar descrição",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand arrow"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(viewModel: EventViewModel) {
    val eventList by viewModel.eventList.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        viewModel.listarTodos()  }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center) })
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedEvent = null
                    showDialog = true },
                modifier = Modifier.width(176.dp).padding(vertical = 16.dp)
            ) {
                Text("Novo evento")
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
        if (showDialog && selectedEvent != null) {
            EventFormDialog(
                event = selectedEvent!!,
                isEditMode = true,
                onDismissRequest = { showDialog = false },
                onAddClick = { newEvent ->
                    viewModel.inserir(newEvent)
                    showDialog = false
                },

                onEditClick = { updatedEvent ->
                    viewModel.atualizar(updatedEvent)
                    showDialog = false
                },
                onDeleteClick = { id ->
                    viewModel.deletar(id)
                    showDialog = false
                }
            )
        } else if (showDialog) {
            EventFormDialog(
                event = Event(title = "", time = "", date = "", cep = "", street = "", number = "", neighborhood = "", city = "", state = "", description = "", imageUrl = ""), // Passa um novo objeto vazio para adicionar
                isEditMode = selectedEvent != null,
                onDismissRequest = {
                    showDialog = false
                    selectedEvent = null },

                onAddClick = { newEvent ->
                    viewModel.inserir(newEvent)
                    showDialog = false
                },
                onEditClick = {  },
                onDeleteClick = {  }
            )
        }
    }
}
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun EventFormDialog(
        event: Event,
        isEditMode: Boolean,
        onDismissRequest: () -> Unit,
        onAddClick: (Event) -> Unit,
        onEditClick: (Event) -> Unit,
        onDeleteClick: (Int) -> Unit
    ) {
        val context = LocalContext.current
        val imageUri = remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri.value = uri
        }

        val storagePermissionState = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)


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

        fun fetchCepData(cep: String, context: Context) {
            if (cep.length != 8 || !cep.all { it.isDigit() }) {
                Toast.makeText(context, "CEP inválido. Deve conter 8 dígitos numéricos.", Toast.LENGTH_SHORT).show()
                return
            }

            runCatching {
                RetrofitClient.instance.getCep(cep).enqueue(object : Callback<CepResponse> {
                    override fun onResponse(call: Call<CepResponse>, response: Response<CepResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                street = it.logradouro
                                neighborhood = it.bairro
                                city = it.localidade
                                state = it.uf
                            }
                        } else {
                            Toast.makeText(context, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CepResponse>, t: Throwable) {
                        Toast.makeText(context, "Erro ao buscar CEP: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }.onFailure {
                Toast.makeText(context, "Erro ao processar o CEP: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

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
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
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
                            onValueChange = {
                                if (it.length <= 8) {
                                    cep = it
                                    if(cep.length == 8) {
                                        fetchCepData(cep, context)
                                    }

                                } },
                            label = { Text("CEP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text("Street") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it },
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
                        },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Inserir imagem", color = Color.White)
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

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!isEditMode) {
                                Button(
                                    onClick = {
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
                                        )
                                        onDismissRequest()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, Color(0xFF388E3C)) //Verde
                                ) {
                                    Text(" Salvar ", color = Color(0xFF388E3C))
                                }
                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) {
                                    Text("Cancelar")
                                }
                            }


                            if (isEditMode) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
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
                                                )
                                                onDismissRequest()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            border = BorderStroke(1.dp, Color(0xFF388E3C)) //Verde
                                        ) {
                                            Text("Salvar", color = Color(0xFF388E3C))
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                onDeleteClick(event.id)
                                                onDismissRequest()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            border = BorderStroke(1.dp, Color(0xFFD32F2F)) //Vermelho
                                        ) {
                                            Text("Excluir", color = Color(0xFFD32F2F))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = onDismissRequest,
                                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("Cancelar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
