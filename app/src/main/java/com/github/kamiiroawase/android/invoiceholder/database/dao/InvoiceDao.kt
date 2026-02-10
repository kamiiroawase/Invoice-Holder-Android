package com.github.kamiiroawase.android.invoiceholder.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity

@Dao
interface InvoiceDao {
    @Insert
    fun insert(invoice: InvoiceEntity)

    @Query("SELECT * FROM invoices WHERE id < :lastId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getListOrderByIdDesc(limit: Int, offset: Int, lastId: Int): List<InvoiceEntity>

    @Delete
    fun delete(invoice: InvoiceEntity)

    @Update
    fun update(invoice: InvoiceEntity)
}
