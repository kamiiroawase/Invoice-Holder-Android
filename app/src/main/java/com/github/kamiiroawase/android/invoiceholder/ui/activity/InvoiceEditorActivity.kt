package com.github.kamiiroawase.android.invoiceholder.ui.activity

import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.github.kamiiroawase.android.invoiceholder.databinding.ActivityInvoiceEditorBinding
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import com.github.kamiiroawase.android.invoiceholder.repository.AppRepository
import java.io.File

class InvoiceEditorActivity : BaseActivity() {
    private lateinit var binding: ActivityInvoiceEditorBinding

    private lateinit var invoice: InvoiceEntity

    private var viewPosition = 0

    private var invoicePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        invoice = intent.parcelable<InvoiceEntity>("invoice")!!
        viewPosition = intent.getIntExtra("viewPosition", -1)
        invoicePosition = intent.getIntExtra("invoicePosition", -1)

        initView()

        setupClickListener()

        initBackPressHandler()
    }

    private fun initView() {
        val invoiceDir = File(filesDir, "xzy_invoice_images")
        val file = File(invoiceDir, invoice.filename)

        Glide.with(this)
            .load(file)
            .override(314)
            .into(binding.picture0)
        Glide.with(this)
            .load(file)
            .into(binding.picture1)

        binding.fapiaodaimaEditText.setText(invoice.fapiaodaima)
        binding.fapiaoNumEditText.setText(invoice.fapiaoNum)
        binding.kaipiaoriqiEditText.setText(invoice.kaipiaoriqi)
        binding.buyerNameEditText.setText(invoice.buyerName)
        binding.sellerNameEditText.setText(invoice.sellerName)
        binding.buyerShuihaoNumEditText.setText(invoice.buyerShuihaoNum)
        binding.sellerShuihaoNumEditText.setText(invoice.sellerShuihaoNum)
        binding.shuieEditText.setText(invoice.shuie)
        binding.shuijiahejiEditText.setText(invoice.shuijiaheji)
    }

    private fun setupClickListener() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.pictureWrap0.setOnClickListener {
            binding.pictureWrap1.visibility = View.VISIBLE
        }

        binding.pictureWrap1ButtonClose.setOnClickListener {
            binding.pictureWrap1.visibility = View.GONE
        }

        binding.buttonUpdate.setOnClickListener {
            invoice.fapiaodaima = binding.fapiaodaimaEditText.text.toString()
            invoice.fapiaoNum = binding.fapiaoNumEditText.text.toString()
            invoice.kaipiaoriqi = binding.kaipiaoriqiEditText.text.toString()
            invoice.buyerName = binding.buyerNameEditText.text.toString()
            invoice.sellerName = binding.sellerNameEditText.text.toString()
            invoice.buyerShuihaoNum = binding.buyerShuihaoNumEditText.text.toString()
            invoice.sellerShuihaoNum = binding.sellerShuihaoNumEditText.text.toString()
            invoice.shuie = binding.shuieEditText.text.toString()
            invoice.shuijiaheji = binding.shuijiahejiEditText.text.toString()

            invoice.kaipiaoriqiDate = binding.kaipiaoriqiEditText.text.toString().let {
                Regex("(\\d+)")
                    .findAll(it)
                    .joinToString("") { it2 -> it2.value }
                    .toLongOrNull()
                    ?: 0L
            }

            AppRepository.pushUpdateInvoiceEvent(invoice, invoicePosition, viewPosition)

            finish()
        }
    }

    private fun initBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.pictureWrap1.isVisible) {
                        binding.pictureWrap1.visibility = View.GONE
                    } else {
                        finish()
                    }
                }
            }
        )
    }
}
