package com.github.kamiiroawase.android.invoiceholder.ui.activity

import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.os.Bundle
import com.github.kamiiroawase.android.invoiceholder.R
import com.github.kamiiroawase.android.invoiceholder.databinding.ActivityMainBinding
import com.github.kamiiroawase.android.invoiceholder.repository.AppRepository
import com.github.kamiiroawase.android.invoiceholder.ui.fragment.PiaojiaFragment
import com.github.kamiiroawase.android.invoiceholder.ui.fragment.ShouyeFragment
import com.github.kamiiroawase.android.invoiceholder.ui.fragment.TaitouFragment
import com.github.kamiiroawase.android.invoiceholder.ui.fragment.WodeFragment
import com.github.kamiiroawase.android.invoiceholder.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navFragments: Map<Int, Fragment>

    private val viewModel: MainActivityViewModel by viewModels()

    private fun getFragmentByNavId(id: Int): Fragment = navFragments[id]
        ?: error("Fragment not found for id=$id")

    private var currentNavItemId = R.id.navigationShouye

    companion object {
        private var lastBackPressTimestamp = 0L
        private const val EXIT_CONFIRM_INTERVAL = 2000L
        private const val STATE_CURRENT_NAV_ITEM_ID = "CURRENT_ITEM_ID"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_CURRENT_NAV_ITEM_ID, currentNavItemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restoreSavedState(savedInstanceState)
        initNavFragments(savedInstanceState != null)
        setupBottomNavListener()
        initBackPressHandler()
        observeViewModel()
        initData()
    }

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            currentNavItemId =
                savedInstanceState.getInt(STATE_CURRENT_NAV_ITEM_ID, R.id.navigationShouye)
        }
    }

    private fun initNavFragments(isRestoring: Boolean) {
        navFragments = mapOf(
            R.id.navigationShouye to getNavFragment(R.id.navigationShouye) { ShouyeFragment() },
            R.id.navigationPiaojia to getNavFragment(R.id.navigationPiaojia) { PiaojiaFragment() },
            R.id.navigationTaitou to getNavFragment(R.id.navigationTaitou) { TaitouFragment() },
            R.id.navigationWode to getNavFragment(R.id.navigationWode) { WodeFragment() }
        )

        supportFragmentManager.beginTransaction().apply {
            navFragments.forEach { _, fragment ->
                hide(fragment)
            }

            show(getFragmentByNavId(currentNavItemId))

            commitNow()
        }

        if (isRestoring) {
            binding.bottomNav.selectedItemId = currentNavItemId
        }
    }

    private fun setupBottomNavListener() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            switchNavFragment(item.itemId)
            true
        }
    }

    private fun initBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTimestamp >= EXIT_CONFIRM_INTERVAL) {
                        lastBackPressTimestamp = currentTime
                        showToast(R.string.zaianyicituichuyingyong)
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppRepository.deleteInvoiceChannel0.collect { event ->
                    val (invoice, invoicePosition, viewPosition) = event
                    viewModel.deleteInvoiceWithView(invoice, invoicePosition, viewPosition)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppRepository.updateInvoiceChannel0.collect { event ->
                    val (invoice, invoicePosition, viewPosition) = event
                    viewModel.updateInvoiceWithView(invoice, invoicePosition, viewPosition)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppRepository.insertHeaderChannel0.collect { header ->
                    viewModel.insertHeaderWithJob(header).join()
                    viewModel.initHeaders()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppRepository.updateHeaderChannel0.collect { event ->
                    val (header, headerPosition, viewPosition) = event
                    viewModel.updateHeaderWithView(header, headerPosition, viewPosition)
                }
            }
        }
    }

    private fun initData() {
        viewModel.initInvoices()
        viewModel.initHeaders()
    }

    private fun switchNavFragment(itemId: Int) {
        if (currentNavItemId != itemId) {
            supportFragmentManager.beginTransaction().apply {
                hide(getFragmentByNavId(currentNavItemId))
                show(getFragmentByNavId(itemId))
                commitNow()
            }

            currentNavItemId = itemId
        }
    }

    private inline fun <reified T : Fragment> getNavFragment(
        id: Int,
        noinline factory: () -> T
    ): T {
        val existingFragment = supportFragmentManager
            .findFragmentByTag(id.toString())

        return if (existingFragment == null) {
            val newFragment = factory()

            supportFragmentManager.beginTransaction().apply {
                add(R.id.navHostFragment, newFragment, id.toString())
                commitNow()
            }

            newFragment
        } else {
            existingFragment as T
        }
    }
}
