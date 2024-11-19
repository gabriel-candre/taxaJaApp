package com.ti4all.agendaapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ti4all.agendaapp.dao.EventDao
import com.ti4all.agendaapp.data.Event

@Database(entities = [Event::class], version = 2)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao() : EventDao

}
