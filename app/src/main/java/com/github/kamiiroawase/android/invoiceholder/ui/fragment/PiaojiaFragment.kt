package com.github.kamiiroawase.android.invoiceholder.ui.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kamiiroawase.android.invoiceholder.databinding.FragmentPiaojiaBinding
import com.github.kamiiroawase.android.invoiceholder.ui.adapter.PiaojiaAdapter
import com.github.kamiiroawase.android.invoiceholder.viewmodel.MainActivityViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch
import kotlin.getValue

class PiaojiaFragment : BaseFragment() {
    private var _binding: FragmentPiaojiaBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    private var piaojiaAdapter: PiaojiaAdapter? = null

    private val pageSize = 12
    private var currentPage = 0
    private val loadingLock = AtomicBoolean(false)
    private var hasMoreData = true

    override fun onDestroyView() {
        super.onDestroyView()

        binding.recyclerView.adapter = null

        piaojiaAdapter = null

        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPiaojiaBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        piaojiaAdapter = PiaojiaAdapter(null, emptyList())

        initView()

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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy <= 0) return

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    val preloadThreshold = 0 // 提前多少个 Item 加载更多

                    if (hasMoreData) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - preloadThreshold) {
                            if (loadingLock.compareAndSet(false, true)) {
                                loadNextPage()
                            }
                        }
                    }
                }
            })
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

                        if (loadingLock.compareAndSet(false, true)) {
                            currentPage = 0
                            hasMoreData = true
                            loadingLock.set(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.invoicesUpdateChannel1.collect { event ->
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
                sharedViewModel.invoicesRemoveChannel1.collect { event ->
                    val piaojiaAdapter = piaojiaAdapter ?: return@collect
                    val (position, invoices) = event
                    val shouldUpdate = PiaojiaAdapter.LATER_UPDATE
                    piaojiaAdapter.submitData(invoices, shouldUpdate)
                    piaojiaAdapter.removeAt(position)
                }
            }
        }
    }

    private fun loadNextPage() {
        currentPage++

        sharedViewModel.loadInvoices(pageSize, pageSize * currentPage) { hasMore ->
            loadingLock.set(false)

            this.hasMoreData = hasMore

//            if (!hasMoreData && recyclerViewAdapter.itemCount > 0) {
//                // 已加载全部数据
//            }
        }
    }
}
