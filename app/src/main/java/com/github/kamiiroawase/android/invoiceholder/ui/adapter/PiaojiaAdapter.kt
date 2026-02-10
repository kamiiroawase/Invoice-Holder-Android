package com.github.kamiiroawase.android.invoiceholder.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.entity.InvoiceEntity
import com.github.kamiiroawase.android.invoiceholder.ui.activity.InvoiceActivity

class PiaojiaAdapter(
    private val maxItemCount: Int? = null,
    private var invoices: List<InvoiceEntity>
) : RecyclerView.Adapter<PiaojiaAdapter.MyViewHolder>() {
    var version = 0L

    companion object {
        const val DO_NOT_UPDATE = 0
        const val SHOULD_UPDATE = 1
        const val LOADING_UPDATE = 2
        const val LATER_UPDATE = 3
    }

    override fun getItemCount(): Int =
        minOf(maxItemCount ?: invoices.size, invoices.size)

    fun updateAt(position: Int) {
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, invoices.size - position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(newInvoices: List<InvoiceEntity>, shouldUpdate: Int) {
        when (shouldUpdate) {
            LOADING_UPDATE -> {
                val oldSize = invoices.size
                val newSize = newInvoices.size

                invoices = newInvoices

                notifyItemRangeInserted(oldSize, newSize - oldSize)
            }

            SHOULD_UPDATE -> {
                invoices = newInvoices
                notifyDataSetChanged()
            }

            LATER_UPDATE -> {
                invoices = newInvoices
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_invoice, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val invoice = invoices[position]

        @SuppressLint("SetTextI18n")
        holder.content.text =
            "买方名称：" + invoice.buyerName +
                "\n卖方名称：" + invoice.sellerName +
                "\n发票号码：" + invoice.fapiaoNum +
                "\n开票日期：" + invoice.kaipiaoriqi +
                "\n税价合计：" + invoice.shuijiaheji +
                "\n发票税额：" + invoice.shuie

        holder.root.setOnClickListener {
            val context = holder.root.context

            context.startActivity(
                Intent(context, InvoiceActivity::class.java).apply {
                    putExtra("invoicePosition", position)
                    putExtra("viewPosition", position)
                    putExtra("invoice", invoice)
                }
            )
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView
        val content = itemView.findViewById<TextView>(R.id.content)!!
    }
}
