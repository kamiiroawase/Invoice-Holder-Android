package com.github.kamiiroawase.android.invoiceholder.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity

@Dao
interface HeaderDao {
    @Insert
    fun insert(header: HeaderEntity)

    @Query("SELECT * FROM headers WHERE id < :lastId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getListOrderByIdDesc(limit: Int, offset: Int, lastId: Int): List<HeaderEntity>

    @Delete
    fun delete(header: HeaderEntity)

    @Update
    fun update(header: HeaderEntity)
}
