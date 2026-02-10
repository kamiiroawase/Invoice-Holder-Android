package com.github.kamiiroawase.android.invoiceholder.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity.HeaderType
import com.github.kamiiroawase.android.invoiceholder.ui.activity.TaitouEditorActivity

class TaitouAdapter(
    private var headers: List<HeaderEntity>,
    private val onDeleteClick: (HeaderEntity, Int, Int) -> Unit
) : RecyclerView.Adapter<TaitouAdapter.MyViewHolder>() {
    var version = 0L

    companion object {
        const val DO_NOT_UPDATE = 0
        const val SHOULD_UPDATE = 1
        const val LOADING_UPDATE = 2
        const val LATER_UPDATE = 3
    }

    override fun getItemCount(): Int = headers.size

    fun updateAt(position: Int) {
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, headers.size - position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(newHeaders: List<HeaderEntity>, shouldUpdate: Int) {
        when (shouldUpdate) {
            LOADING_UPDATE -> {
                val oldSize = headers.size
                val newSize = newHeaders.size

                headers = newHeaders

                notifyItemRangeInserted(oldSize, newSize - oldSize)
            }

            SHOULD_UPDATE -> {
                headers = newHeaders
                notifyDataSetChanged()
            }

            LATER_UPDATE -> {
                headers = newHeaders
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
                .inflate(R.layout.item_header, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val header = headers[position]

        @SuppressLint("SetTextI18n")
        if (header.type == HeaderType.Personal) {
            holder.content.text =
                "抬头名称：" + header.name
        } else if (header.type == HeaderType.Enterprise) {
            holder.content.text =
                "抬头名称：" + header.name +
                    "\n公司税号：" + header.shuihaoNum +
                    "\n电话号码：" + header.phoneNum +
                    "\n开户银行：" + header.kaihuyinhang +
                    "\n银行账号：" + header.yinhangzhanghao +
                    "\n公司地址：" + header.gongsidizhi
        }

        holder.buttonDelete.setOnClickListener {
            val context = holder.root.context

            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.shanchutaitou))
                .setMessage(context.getString(R.string.quedingyaoshanchutaitouma))
                .setPositiveButton(context.getString(R.string.queding)) { dialog, which ->
                    onDeleteClick.invoke(header, position, position)
                }
                .setNegativeButton(context.getString(R.string.quxiao)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.buttonUpdate.setOnClickListener {
            val context = holder.root.context

            context.startActivity(
                Intent(context, TaitouEditorActivity::class.java).apply {
                    putExtra("headerPosition", position)
                    putExtra("viewPosition", position)
                    putExtra("header", header)
                }
            )
        }

        holder.root.setOnClickListener {
            getClipboard(it).setPrimaryClip(
                ClipData.newPlainText(
                    "fontItem",
                    if (header.type == HeaderType.Enterprise) {
                        holder.content.text
                    } else {
                        header.name
                    }
                )
            )

            showToast(it, R.string.fuzhichenggong)
        }
    }

    private fun getClipboard(view: View): ClipboardManager {
        return view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private fun showToast(view: View, resId: Int) {
        Toast.makeText(view.context, resId, Toast.LENGTH_SHORT).show()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView
        val content = itemView.findViewById<TextView>(R.id.content)!!
        val buttonUpdate = itemView.findViewById<TextView>(R.id.buttonUpdate)!!
        val buttonDelete = itemView.findViewById<TextView>(R.id.buttonDelete)!!
    }
}
