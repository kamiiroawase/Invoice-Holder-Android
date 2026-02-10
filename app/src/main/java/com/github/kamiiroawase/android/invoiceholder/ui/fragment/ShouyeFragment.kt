package com.github.kamiiroawase.android.invoiceholder.ui.fragment

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.databinding.FragmentShouyeBinding
import com.github.kamiiroawase.android.invoiceholder.ui.adapter.PiaojiaAdapter
import com.github.kamiiroawase.android.invoiceholder.util.BitmapRenderUtil
import com.github.kamiiroawase.android.invoiceholder.util.InvoiceUtil
import com.github.kamiiroawase.android.invoiceholder.viewmodel.MainActivityViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.getValue

class ShouyeFragment : BaseFragment() {
    private var _binding: FragmentShouyeBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    private var piaojiaAdapter: PiaojiaAdapter? = null

    private var cameraImageUri: Uri? = null

    private var cameraImageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShouyeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.recyclerView.adapter = null

        piaojiaAdapter = null

        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        piaojiaAdapter = PiaojiaAdapter(10, emptyList())

        initView()

        setupClickListener()

        observeViewModel()
    }

    private fun initView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )

            adapter = piaojiaAdapter
        }
    }

    private fun setupClickListener() {
        binding.buttonTupianshibie.setOnClickListener {
            pickFileLauncher.launch(
                arrayOf("image/*")
            )
        }

        binding.buttonWenjianshibie.setOnClickListener {
            pickFileLauncher.launch(
                arrayOf("application/pdf")
            )
        }

        binding.buttonPaizhaoshibie.setOnClickListener {
            recognizeFromCamera()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.invoicesEventFlow.collect { invoicesEvent ->
                    val piaojiaAdapter = piaojiaAdapter ?: return@collect

                    val (version, shouldUpdate, invoices) = invoicesEvent

                    if (piaojiaAdapter.version != version) {
                        piaojiaAdapter.version = version
                        piaojiaAdapter.submitData(invoices, shouldUpdate)
                    }

                    if (shouldUpdate == PiaojiaAdapter.SHOULD_UPDATE) {
                        binding.recyclerView.scrollToPosition(0)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.invoicesUpdateChannel0.collect { event ->
                    val piaojiaAdapter = piaojiaAdapter ?: return@collect
                    val (position, invoices) = event
                    val shouldUpdate = PiaojiaAdapter.LATER_UPDATE
                    piaojiaAdapter.submitData(invoices, shouldUpdate)
                    piaojiaAdapter.updateAt(position)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.invoicesRemoveChannel0.collect { event ->
                    val piaojiaAdapter = piaojiaAdapter ?: return@collect
                    val (position, invoices) = event
                    val shouldUpdate = PiaojiaAdapter.LATER_UPDATE
                    piaojiaAdapter.submitData(invoices, shouldUpdate)
                    piaojiaAdapter.removeAt(position)
                }
            }
        }
    }

    private fun startRecognize(uri: Uri, isCamera: Boolean = false) {
        val job = lifecycleScope.launch {
            delay(500)

            binding.invoiceRecognizing.visibility = View.VISIBLE
        }

        recognizeFromUri(uri, isCamera, {
            showToast(R.string.fapiaoshibieshibai)
        }) {
            lifecycleScope.launch {
                job.cancelAndJoin()
                binding.invoiceRecognizing.visibility = View.GONE
            }
        }
    }

    private fun recognizeFromUri(
        uri: Uri,
        isCamera: Boolean,
        onFail: () -> Unit,
        onSuccess: () -> Unit
    ) {
        val type = requireContext()
            .contentResolver
            .getType(uri)

        if (type == null) {
            onFail.invoke()
            return
        }

        when {
            type.startsWith("image/") -> {
                val bitmap = BitmapRenderUtil.decodeImageToBitmap(requireContext(), uri)

                if (bitmap != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        recognizeFromBitmap(bitmap).join()

                        val initInvoicesJob = sharedViewModel.initInvoices()

                        if (isCamera) {
                            cameraImageFile?.delete()
                            cameraImageFile = null
                            cameraImageUri = null
                        }

                        initInvoicesJob.join()

                        onSuccess.invoke()
                    }
                } else {
                    onFail.invoke()
                }
            }

            type == "application/pdf" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val jobs = mutableListOf<Job>()

                    BitmapRenderUtil.renderPdfPages(requireContext(), uri) { bitmap ->
                        if (bitmap != null) {
                            jobs.add(recognizeFromBitmap(bitmap))
                        }
                    }

                    if (jobs.isNotEmpty()) {
                        jobs.forEach { job ->
                            job.join()
                        }

                        sharedViewModel.initInvoices().join()

                        onSuccess.invoke()
                    } else {
                        onFail.invoke()
                    }
                }
            }

            else -> onFail.invoke()
        }
    }

    private fun recognizeFromBitmap(bitmap: Bitmap): Job {
        var done = false
        var job: Job? = null

        InvoiceUtil.recognize(bitmap) { invoice ->
            if (invoice != null) {
                val invoiceDir = File(requireContext().filesDir, "xzy_invoice_images")

                if (!invoiceDir.exists()) {
                    invoiceDir.mkdirs()
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                    .format(Date())

                val filename = "XZY_INVOICE_${UUID.randomUUID()}_${timeStamp}.jpg"

                val file = File(invoiceDir, filename)

                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            out
                        )
                    }
                } catch (_: Exception) {
                    return@recognize
                }

                job = sharedViewModel.insertInvoiceWithJob(invoice, filename)
            }

            done = true
        }

        return lifecycleScope.launch(Dispatchers.IO) {
            while (!done) {
                delay(100)
            }

            job?.join()
        }
    }

    private fun recognizeFromCamera() {
        cameraImageUri = null

        cameraImageFile = File(
            requireContext().cacheDir,
            "XZY_InvoiceHolder_${System.currentTimeMillis()}.jpg"
        )

        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            cameraImageFile!!
        )

        if (cameraImageUri != null) {
            takePictureLauncher.launch(cameraImageUri)
        }
    }

    private val pickFileLauncher =
        registerForActivityResult(OpenDocument()) { uri ->
            if (uri != null) {
                startRecognize(uri)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                startRecognize(cameraImageUri!!, true)
            }
        }
}
