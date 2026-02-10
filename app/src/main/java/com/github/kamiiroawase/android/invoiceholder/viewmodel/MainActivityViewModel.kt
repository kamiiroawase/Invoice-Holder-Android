package com.github.kamiiroawase.android.invoiceholder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kamiiroawase.android.invoiceholder.base.App
import com.github.kamiiroawase.android.invoiceholder.database.AppDatabase
import com.github.kamiiroawase.android.invoiceholder.database.dao.HeaderDao
import com.github.kamiiroawase.android.invoiceholder.database.dao.InvoiceDao
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import com.github.kamiiroawase.android.invoiceholder.ui.adapter.PiaojiaAdapter
import com.github.kamiiroawase.android.invoiceholder.ui.adapter.TaitouAdapter
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val _invoicesEventFlow = MutableStateFlow<Triple<Long, Int, List<InvoiceEntity>>>(
        Triple(0L, PiaojiaAdapter.DO_NOT_UPDATE, emptyList())
    )

    private val _invoicesUpdateChannel0 = Channel<Pair<Int, List<InvoiceEntity>>>(
        capacity = Channel.UNLIMITED
    )
    private val _invoicesUpdateChannel1 = Channel<Pair<Int, List<InvoiceEntity>>>(
        capacity = Channel.UNLIMITED
    )

    private val _invoicesRemoveChannel0 = Channel<Pair<Int, List<InvoiceEntity>>>(
        capacity = Channel.UNLIMITED
    )
    private val _invoicesRemoveChannel1 = Channel<Pair<Int, List<InvoiceEntity>>>(
        capacity = Channel.UNLIMITED
    )

    private val _headersEventFlow = MutableStateFlow<Triple<Long, Int, List<HeaderEntity>>>(
        Triple(0L, TaitouAdapter.DO_NOT_UPDATE, emptyList())
    )

    private val _headersUpdateChannel0 = Channel<Pair<Int, List<HeaderEntity>>>(
        capacity = Channel.UNLIMITED
    )

    private val _headersRemoveChannel0 = Channel<Pair<Int, List<HeaderEntity>>>(
        capacity = Channel.UNLIMITED
    )

    val invoicesEventFlow = _invoicesEventFlow.asSharedFlow()

    val invoicesUpdateChannel0 = _invoicesUpdateChannel0.receiveAsFlow()
    val invoicesUpdateChannel1 = _invoicesUpdateChannel1.receiveAsFlow()

    val invoicesRemoveChannel0 = _invoicesRemoveChannel0.receiveAsFlow()
    val invoicesRemoveChannel1 = _invoicesRemoveChannel1.receiveAsFlow()

    val headersEventFlow = _headersEventFlow.asSharedFlow()

    val headersUpdateChannel0 = _headersUpdateChannel0.receiveAsFlow()

    val headersRemoveChannel0 = _headersRemoveChannel0.receiveAsFlow()

    fun initInvoices(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            _invoicesEventFlow.value = Triple(
                System.currentTimeMillis(),
                PiaojiaAdapter.SHOULD_UPDATE,
                invoiceDao()
                    .getListOrderByIdDesc(12, 0, Int.MAX_VALUE)
            )
        }
    }

    fun loadInvoices(limit: Int, offset: Int, callback: (hasMore: Boolean) -> Unit) {
        var lastId = 0

        if (offset > 0) {
            if (_invoicesEventFlow.value.third.isNotEmpty()) {
                lastId = _invoicesEventFlow.value.third.last().id
            } else {
                callback.invoke(false)
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val moreInvoices = invoiceDao().getListOrderByIdDesc(limit, 0, lastId)

            if (moreInvoices.isNotEmpty()) {
                val invoices = _invoicesEventFlow.value.third.toMutableList()

                invoices.addAll(moreInvoices)

                _invoicesEventFlow.value = Triple(
                    System.currentTimeMillis(),
                    PiaojiaAdapter.LOADING_UPDATE,
                    invoices
                )

                callback.invoke(true)
            } else {
                callback.invoke(false)
            }
        }
    }

    fun insertInvoiceWithJob(invoice: InvoiceEntity, filename: String): Job {
        val timestamp = System.currentTimeMillis()

        invoice.createdAt = timestamp
        invoice.updatedAt = timestamp

        invoice.filename = filename

        return viewModelScope.launch(Dispatchers.IO) {
            invoiceDao().insert(invoice)
        }
    }

    fun updateInvoiceWithView(invoice: InvoiceEntity, invoicePosition: Int, viewPosition: Int) {
        invoice.updatedAt = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            invoiceDao().update(invoice)

            val invoices = _invoicesEventFlow.value.third.toMutableList()

            invoices[invoicePosition] = invoice

            _invoicesEventFlow.value = Triple(
                _invoicesEventFlow.value.first,
                PiaojiaAdapter.DO_NOT_UPDATE,
                invoices
            )

            _invoicesUpdateChannel0.trySend(Pair(viewPosition, invoices))
            _invoicesUpdateChannel1.trySend(Pair(viewPosition, invoices))
        }
    }

    fun deleteInvoiceWithView(invoice: InvoiceEntity, invoicePosition: Int, viewPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceDao().delete(invoice)

            val invoiceDir = File(App.INSTANCE.filesDir, "xzy_invoice_images")
            val file = File(invoiceDir, invoice.filename)

            if (file.exists()) {
                file.delete()
            }

            val invoices = _invoicesEventFlow.value.third.toMutableList()

            invoices.removeAt(invoicePosition)

            _invoicesEventFlow.value = Triple(
                _invoicesEventFlow.value.first,
                PiaojiaAdapter.DO_NOT_UPDATE,
                invoices
            )

            _invoicesRemoveChannel0.trySend(Pair(viewPosition, invoices))
            _invoicesRemoveChannel1.trySend(Pair(viewPosition, invoices))
        }
    }

    fun initHeaders() {
        viewModelScope.launch(Dispatchers.IO) {
            _headersEventFlow.value = Triple(
                System.currentTimeMillis(),
                TaitouAdapter.SHOULD_UPDATE,
                headerDao()
                    .getListOrderByIdDesc(12, 0, Int.MAX_VALUE)
            )
        }
    }

    fun loadHeaders(limit: Int, offset: Int, callback: (hasMore: Boolean) -> Unit) {
        var lastId = 0

        if (offset > 0) {
            if (_headersEventFlow.value.third.isNotEmpty()) {
                lastId = _headersEventFlow.value.third.last().id
            } else {
                callback.invoke(false)
                return
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val moreHeaders = headerDao().getListOrderByIdDesc(limit, 0, lastId)

            if (moreHeaders.isNotEmpty()) {
                val headers = _headersEventFlow.value.third.toMutableList()

                headers.addAll(moreHeaders)

                _headersEventFlow.value = Triple(
                    System.currentTimeMillis(),
                    TaitouAdapter.LOADING_UPDATE,
                    headers
                )

                callback.invoke(true)
            } else {
                callback.invoke(false)
            }
        }
    }

    fun insertHeaderWithJob(header: HeaderEntity): Job {
        val timestamp = System.currentTimeMillis()

        header.createdAt = timestamp
        header.updatedAt = timestamp

        return viewModelScope.launch(Dispatchers.IO) {
            headerDao().insert(header)
        }
    }

    fun updateHeaderWithView(header: HeaderEntity, headerPosition: Int, viewPosition: Int) {
        header.updatedAt = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            headerDao().update(header)

            val headers = _headersEventFlow.value.third.toMutableList()

            headers[headerPosition] = header

            _headersEventFlow.value = Triple(
                _headersEventFlow.value.first,
                TaitouAdapter.DO_NOT_UPDATE,
                headers
            )

            _headersUpdateChannel0.trySend(Pair(viewPosition, headers))
        }
    }

    fun deleteHeaderWithView(header: HeaderEntity, headerPosition: Int, viewPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            headerDao().delete(header)

            val headers = _headersEventFlow.value.third.toMutableList()

            headers.removeAt(headerPosition)

            _headersEventFlow.value = Triple(
                _headersEventFlow.value.first,
                TaitouAdapter.DO_NOT_UPDATE,
                headers
            )

            _headersRemoveChannel0.trySend(Pair(viewPosition, headers))
        }
    }

    private fun headerDao(): HeaderDao {
        return AppDatabase.getInstance().headerDao()
    }

    private fun invoiceDao(): InvoiceDao {
        return AppDatabase.getInstance().invoiceDao()
    }
}
