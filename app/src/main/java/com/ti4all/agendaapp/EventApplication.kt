package com.ti4all.agendaapp

import android.app.Application
import androidx.room.Room

class EventApplication : Application() {

    lateinit var database : EventDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(this, EventDatabase::class.java
            , "event-db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    companion object {
        lateinit var instance: EventApplication
            private set
    }
}