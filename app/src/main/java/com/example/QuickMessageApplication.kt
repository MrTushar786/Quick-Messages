package com.example

import android.app.Application
import com.example.data.MessageRepository

class QuickMessageApplication : Application() {

    lateinit var repository: MessageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MessageRepository(this)
    }
}
