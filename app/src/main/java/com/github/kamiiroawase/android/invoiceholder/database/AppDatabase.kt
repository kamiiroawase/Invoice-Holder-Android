package com.github.kamiiroawase.android.invoiceholder.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.github.kamiiroawase.android.invoiceholder.base.App
import com.github.kamiiroawase.android.invoiceholder.database.dao.HeaderDao
import com.github.kamiiroawase.android.invoiceholder.database.dao.InvoiceDao
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity.HeaderType
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity.InvoiceType

@Database(
    entities = [
        InvoiceEntity::class,
        HeaderEntity::class
    ],
    version = 1
)
@TypeConverters(
    AppDatabase.HeaderTypeConverter::class,
    AppDatabase.InvoiceTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun headerDao(): HeaderDao

    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    App.INSTANCE,
                    AppDatabase::class.java,
                    "c5d413ab-021b-4042-8770-fce08344d81a"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }

    class HeaderTypeConverter {
        @TypeConverter
        fun fromHeaderType(type: HeaderType): Int {
            return type.value
        }

        @TypeConverter
        fun toHeaderType(value: Int): HeaderType {
            return HeaderType.entries.first { it.value == value }
        }
    }

    class InvoiceTypeConverter {
        @TypeConverter
        fun fromInvoiceType(type: InvoiceType): Int {
            return type.value
        }

        @TypeConverter
        fun toInvoiceType(value: Int): InvoiceType {
            return InvoiceType.entries.first { it.value == value }
        }
    }
}
