package com.app.lib_network.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.lib_common.app.AppGlobals

@Database(entities = [Cache::class], version = 1, exportSchema = false)
abstract class CacheDatabase : RoomDatabase() {
    companion object {
        private val database: CacheDatabase =
            Room.databaseBuilder(AppGlobals.context, CacheDatabase::class.java, "jetpack_app")
                .allowMainThreadQueries()
                .build()

        fun get() = database
    }

    abstract fun cacheDao(): CacheDao
}