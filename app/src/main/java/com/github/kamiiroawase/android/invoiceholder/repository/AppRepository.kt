package com.github.kamiiroawase.android.invoiceholder.repository

import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object AppRepository {
    private val _updateInvoiceChannel0 = Channel<Triple<InvoiceEntity, Int, Int>>(
        capacity = Channel.UNLIMITED
    )
    private val _updateInvoiceChannel1 = Channel<Triple<InvoiceEntity, Int, Int>>(
        capacity = Channel.UNLIMITED
    )

    private val _deleteInvoiceChannel0 = Channel<Triple<InvoiceEntity, Int, Int>>(
        capacity = Channel.UNLIMITED
    )

    private val _insertHeaderChannel0 = Channel<HeaderEntity>(
        capacity = Channel.UNLIMITED
    )

    private val _updateHeaderChannel0 = Channel<Triple<HeaderEntity, Int, Int>>(
        capacity = Channel.UNLIMITED
    )

    val updateInvoiceChannel0 = _updateInvoiceChannel0.receiveAsFlow()
    val updateInvoiceChannel1 = _updateInvoiceChannel1.receiveAsFlow()

    val deleteInvoiceChannel0 = _deleteInvoiceChannel0.receiveAsFlow()

    val insertHeaderChannel0 = _insertHeaderChannel0.receiveAsFlow()

    val updateHeaderChannel0 = _updateHeaderChannel0.receiveAsFlow()

    fun pushDeleteInvoiceEvent(
        invoice: InvoiceEntity,
        invoicePosition: Int,
        viewPosition: Int
    ) {
        _deleteInvoiceChannel0.trySend(Triple(invoice, invoicePosition, viewPosition))
    }

    fun pushUpdateInvoiceEvent(
        invoice: InvoiceEntity,
        invoicePosition: Int,
        viewPosition: Int
    ) {
        _updateInvoiceChannel0.trySend(Triple(invoice, invoicePosition, viewPosition))
        _updateInvoiceChannel1.trySend(Triple(invoice, invoicePosition, viewPosition))
    }

    fun pushInsertHeaderEvent(header: HeaderEntity) {
        _insertHeaderChannel0.trySend(header)
    }

    fun pushUpdateHeaderEvent(
        header: HeaderEntity,
        headerPosition: Int,
        viewPosition: Int
    ) {
        _updateHeaderChannel0.trySend(Triple(header, headerPosition, viewPosition))
    }
}
