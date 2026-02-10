package com.github.kamiiroawase.android.invoiceholder.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "headers")
data class HeaderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var type: HeaderType = HeaderType.Personal,
    var name: String = "",
    var shuihaoNum: String = "",
    var phoneNum: String = "",
    var kaihuyinhang: String = "",
    var yinhangzhanghao: String = "",
    var gongsidizhi: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
) : Parcelable {
    enum class HeaderType(val value: Int) {
        Personal(1),
        Enterprise(2)
    }
}
