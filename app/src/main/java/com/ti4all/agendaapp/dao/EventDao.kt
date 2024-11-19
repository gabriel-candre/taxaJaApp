package com.ti4all.agendaapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ti4all.agendaapp.data.Event

@Dao
interface EventDao {

    @Insert
    suspend fun inserir(event: Event)

    @Update
    suspend fun atualizar(event: Event) // Método para atualizar contato

    @Query("SELECT * FROM event")
    suspend fun listarTodos() : List<Event>

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deletear(id: Int)
}