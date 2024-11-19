package com.ti4all.agendaapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ti4all.agendaapp.EventApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(application: EventApplication) : ViewModel() {
    private val eventDao = application.database.eventDao()
    private val _eventList = MutableStateFlow<List<Event>>(emptyList())
    val eventList : StateFlow<List<Event>> = _eventList

    init {
        listarTodos()
    }

    // Construtor padrão necessário para ViewModelProvider
    @Suppress("unused")
    constructor() : this(EventApplication.instance)

    fun listarTodos() {
        viewModelScope.launch {
            _eventList.value = eventDao.listarTodos()
        }
    }

    fun inserir(event: Event) {
        viewModelScope.launch {
            eventDao.inserir(event)
            listarTodos()
        }
    }

    fun atualizar(event: Event) {
        viewModelScope.launch {
            eventDao.atualizar(event) // Atualiza o evento no banco de dados
            listarTodos() // Recarrega a lista
        }
    }

    fun deletar(id: Int) {
        viewModelScope.launch {
            eventDao.deletear(id)
            listarTodos()
        }
    }
}