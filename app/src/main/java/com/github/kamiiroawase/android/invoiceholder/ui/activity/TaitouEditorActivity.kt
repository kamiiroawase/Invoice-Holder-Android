package com.github.kamiiroawase.android.invoiceholder.ui.activity

import android.os.Bundle
import android.view.View
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.databinding.ActivityTaitouEditorBinding
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity.HeaderType
import com.github.kamiiroawase.android.invoiceholder.repository.AppRepository

class TaitouEditorActivity : BaseActivity() {
    private lateinit var binding: ActivityTaitouEditorBinding

    private lateinit var header: HeaderEntity

    private var viewPosition = 0

    private var headerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaitouEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        header = intent.parcelable<HeaderEntity>("header")!!
        viewPosition = intent.getIntExtra("viewPosition", -1)
        headerPosition = intent.getIntExtra("headerPosition", -1)

        initView()

        setupClickListener()

        setupCheckedChangeListener()
    }

    private fun initView() {
        if (header.id > 0) {
            binding.nameEditText.setText(header.name)
            binding.shuihaoNumEditText.setText(header.shuihaoNum)
            binding.phoneNumEditText.setText(header.phoneNum)
            binding.kaihuyinhangEditText.setText(header.kaihuyinhang)
            binding.yinhangzhanghaoEditText.setText(header.yinhangzhanghao)
            binding.gongsidizhiEditText.setText(header.gongsidizhi)
            binding.buttonUpdate.text = getString(R.string.genggai)
            binding.title.text = getString(R.string.bianjitaitou)
        } else {
            binding.buttonUpdate.text = getString(R.string.tianjia)
            binding.title.text = getString(R.string.tianjiataitou)
        }

        if (header.type == HeaderType.Personal) {
            binding.enterpriseEditTextGroup.visibility = View.GONE
        } else if (header.type == HeaderType.Enterprise) {
            binding.enterpriseEditTextGroup.visibility = View.VISIBLE
        }
    }

    private fun setupClickListener() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonUpdate.setOnClickListener {
            if (header.type == HeaderType.Personal) {
                header.name = binding.nameEditText.text.toString()
                if (header.id > 0) {
                    AppRepository.pushUpdateHeaderEvent(header, headerPosition, viewPosition)
                } else {
                    AppRepository.pushInsertHeaderEvent(header)
                }
            } else if (header.type == HeaderType.Enterprise) {
                header.name = binding.nameEditText.text.toString()
                header.shuihaoNum = binding.shuihaoNumEditText.text.toString()
                header.phoneNum = binding.phoneNumEditText.text.toString()
                header.kaihuyinhang = binding.kaihuyinhangEditText.text.toString()
                header.yinhangzhanghao = binding.yinhangzhanghaoEditText.text.toString()
                header.gongsidizhi = binding.gongsidizhiEditText.text.toString()
                if (header.id > 0) {
                    AppRepository.pushUpdateHeaderEvent(header, headerPosition, viewPosition)
                } else {
                    AppRepository.pushInsertHeaderEvent(header)
                }
            }

            finish()
        }
    }

    private fun setupCheckedChangeListener() {
        binding.taitouType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.taitouTypeQiye -> {
                    header.type = HeaderType.Enterprise
                    binding.enterpriseEditTextGroup.visibility = View.VISIBLE
                }

                R.id.taitouTypeGeren -> {
                    header.type = HeaderType.Personal
                    binding.enterpriseEditTextGroup.visibility = View.GONE
                }
            }
        }

        if (header.type == HeaderType.Personal) {
            binding.taitouType.check(R.id.taitouTypeGeren)
        } else if (header.type == HeaderType.Enterprise) {
            binding.taitouType.check(R.id.taitouTypeQiye)
        }
    }
}
