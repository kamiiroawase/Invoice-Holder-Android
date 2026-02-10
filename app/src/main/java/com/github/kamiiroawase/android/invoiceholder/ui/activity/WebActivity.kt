package com.github.kamiiroawase.android.invoiceholder.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.kamiiroawase.android.invoiceholder.databinding.ActivityWebBinding

class WebActivity : BaseActivity() {
    private lateinit var binding: ActivityWebBinding

    companion object {
        const val EXTRA_URL = "url"
        private const val DEFAULT_URL = "https://www.irs.gov/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupRefreshListener()
        loadUrl()
    }

    override fun onDestroy() {
        binding.webView.stopLoading()

        super.onDestroy()
    }

    private fun setupWebView() {
        @SuppressLint("SetJavaScriptEnabled")
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupRefreshListener() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }
    }

    private fun loadUrl() {
        val url = intent.getStringExtra(EXTRA_URL)
        binding.webView.loadUrl(url ?: DEFAULT_URL)
    }
}
