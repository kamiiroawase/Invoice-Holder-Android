package com.github.kamiiroawase.android.invoiceholder.ui.fragment

import androidx.fragment.app.Fragment
import android.widget.Toast

open class BaseFragment : Fragment() {
    protected fun showToast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }
}
