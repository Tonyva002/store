package com.example.stores.Models

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stores.Interfaces.StoreDao

@Database(entities = arrayOf(StoreEntity::class), version = 2)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
}