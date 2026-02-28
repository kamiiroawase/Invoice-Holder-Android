package com.github.kamiiroawase.android.invoiceholder.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.databinding.FragmentWodeBinding
import com.github.kamiiroawase.android.invoiceholder.ui.activity.WebActivity
import com.github.kamiiroawase.android.invoiceholder.ui.activity.FeedbackActivity

class WodeFragment : BaseFragment() {
    private var _binding: FragmentWodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWodeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.buttonYonghuxieyi.setOnClickListener {
            startActivity(Intent(activity, WebActivity::class.java).apply {
                putExtra(WebActivity.EXTRA_NANE, getString(R.string.yonghuxieyi))
                putExtra(WebActivity.EXTRA_URL, "https://www.irs.gov/")
            })
        }

        binding.buttonYinsixieyi.setOnClickListener {
            startActivity(Intent(activity, WebActivity::class.java).apply {
                putExtra(WebActivity.EXTRA_NANE, getString(R.string.yinsixieyi))
                putExtra(WebActivity.EXTRA_URL, "https://www.irs.gov/")
            })
        }

        binding.buttonYijianfankui.setOnClickListener {
            startActivity(Intent(requireContext(), FeedbackActivity::class.java))
        }
    }

    private fun initView() {
        binding.buttonDangqianbanbenText.text =
            getString(R.string.dangqianbanben, getVersionName())
    }

    private fun getVersionName(): String {
        return runCatching {
            requireContext()
                .packageManager
                .getPackageInfo(requireContext().packageName, 0)
                .versionName
                ?: ""
        }.getOrDefault("")
    }
}
