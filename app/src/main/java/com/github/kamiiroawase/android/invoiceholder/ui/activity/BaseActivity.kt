package com.github.kamiiroawase.android.invoiceholder.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.widget.Toast

open class BaseActivity : AppCompatActivity() {
    protected fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            getParcelableExtra(key, T::class.java)

        else ->
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
    }
}
