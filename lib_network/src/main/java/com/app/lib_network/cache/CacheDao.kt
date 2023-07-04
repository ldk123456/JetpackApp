package com.app.lib_network.cache

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(cache: Cache): Long

    @Query("select * from cache where `key`=:key")
    fun getCache(key: String): Cache?

    @Delete
    fun delete(cache: Cache): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(cache: Cache): Int
}