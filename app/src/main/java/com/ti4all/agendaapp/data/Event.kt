package com.ti4all.agendaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date : String,
    val time : String,
    val cep : String,
    val street : String,
    val number : Int,
    val neighborhood : String,
    val city : String,
    val state : String,
    val description: String,
    val imageUrl: String
)
