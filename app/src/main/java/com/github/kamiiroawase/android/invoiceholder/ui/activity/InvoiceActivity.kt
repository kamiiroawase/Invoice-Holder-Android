package com.github.kamiiroawase.android.invoiceholder.ui.activity

import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.databinding.ActivityInvoiceBinding
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import com.github.kamiiroawase.android.invoiceholder.repository.AppRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InvoiceActivity : BaseActivity() {
    private lateinit var binding: ActivityInvoiceBinding

    private lateinit var invoice: InvoiceEntity

    private lateinit var file: File

    private var viewPosition = 0

    private var invoicePosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        invoice = intent.parcelable<InvoiceEntity>("invoice")!!
        viewPosition = intent.getIntExtra("viewPosition", -1)
        invoicePosition = intent.getIntExtra("invoicePosition", -1)

        file = File(
            File(filesDir, "xzy_invoice_images"),
            invoice.filename
        )

        initView()
        setupClickListener()
        initBackPressHandler()
        observeViewModel()
    }

    private fun initView() {
        setContentText(invoice)

        Glide.with(this)
            .load(file)
            .override(314)
            .into(binding.picture0)
        Glide.with(this)
            .load(file)
            .into(binding.picture1)
    }

    private fun setupClickListener() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonExport.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val uri = exportImageToGallery(file)

                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        showToast(R.string.tupianyidaochuzhixiangce)
                    } else {
                        showToast(R.string.daochutupianshibai)
                    }
                }
            }
        }

        binding.pictureWrap0.setOnClickListener {
            binding.pictureWrap1.visibility = View.VISIBLE
        }

        binding.pictureWrap1ButtonClose.setOnClickListener {
            binding.pictureWrap1.visibility = View.GONE
        }

        binding.buttonDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.shanchufapiao))
                .setMessage(getString(R.string.quedingyaoshanchufapiaoma))
                .setPositiveButton(getString(R.string.queding)) { dialog, which ->
                    AppRepository.pushDeleteInvoiceEvent(invoice, invoicePosition, viewPosition)
                    finish()
                }
                .setNegativeButton(getString(R.string.quxiao)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.buttonEdit.setOnClickListener {
            startActivity(Intent(this, InvoiceEditorActivity::class.java).apply {
                putExtra("invoicePosition", invoicePosition)
                putExtra("viewPosition", viewPosition)
                putExtra("invoice", invoice)
            })
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

    private fun setContentText(invoice: InvoiceEntity) {
        @SuppressLint("SetTextI18n")
        binding.content.text =
            "发票代码：" + invoice.fapiaodaima +
                "\n发票号码：" + invoice.fapiaoNum +
                "\n买方名称：" + invoice.buyerName +
                "\n卖方名称：" + invoice.sellerName +
                "\n买方税号：" + invoice.buyerShuihaoNum +
                "\n卖方税号：" + invoice.sellerShuihaoNum +
                "\n发票税额：" + invoice.shuie +
                "\n税价合计：" + invoice.shuijiaheji +
                "\n开票日期：" + invoice.kaipiaoriqi +
                "\n创建时间：" +
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.CHINA
                ).format(invoice.createdAt) +
                "\n更新时间：" +
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.CHINA
                ).format(invoice.updatedAt)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppRepository.updateInvoiceChannel1.collect { event ->
                    val (newInvoice, _, _) = event
                    invoice = newInvoice
                    setContentText(invoice)
                }
            }
        }
    }

    private fun exportImageToGallery(file: File): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/XZY_InvoiceHolder"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return null

        try {
            contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            contentValues.clear()

            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)

            contentResolver.update(uri, contentValues, null, null)

            return uri
        } catch (_: Exception) {
            contentResolver.delete(uri, null, null)
        }

        return null
    }
}
