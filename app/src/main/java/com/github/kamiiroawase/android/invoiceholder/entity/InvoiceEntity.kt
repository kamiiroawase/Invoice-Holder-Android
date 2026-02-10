package com.github.kamiiroawase.android.invoiceholder.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var type: InvoiceType = InvoiceType.Other,
    var filename: String = "",
    var fapiaodaima: String = "",
    var fapiaoNum: String = "",
    var kaipiaoriqi: String = "",
    var buyerName: String = "",
    var sellerName: String = "",
    var buyerShuihaoNum: String = "",
    var sellerShuihaoNum: String = "",
    var shuie: String = "",
    var shuijiaheji: String = "",
    var kaipiaoriqiDate: Long = 0L,
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
) : Parcelable {
    enum class InvoiceType(val value: Int) {
        Other(0),
        Canyinfei(1),
        Zhusufei(2),
        Jiaotongfei(3),
        Tongxunfei(1)
    }
}
