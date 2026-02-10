package com.github.kamiiroawase.android.invoiceholder.ui.fragment

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kamiiroawase.android.invoiceholder.databinding.FragmentTaitouBinding
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity
import com.github.kamiiroawase.android.invoiceholder.entity.HeaderEntity.HeaderType
import com.github.kamiiroawase.android.invoiceholder.ui.activity.TaitouEditorActivity
import com.github.kamiiroawase.android.invoiceholder.ui.adapter.TaitouAdapter
import com.github.kamiiroawase.android.invoiceholder.viewmodel.MainActivityViewModel
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch
import kotlin.getValue

class TaitouFragment : BaseFragment() {
    private var _binding: FragmentTaitouBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainActivityViewModel by activityViewModels()

    private var taitouAdapter: TaitouAdapter? = null

    private val pageSize = 12
    private var currentPage = 0
    private val loadingLock = AtomicBoolean(false)
    private var hasMoreData = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaitouBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.recyclerView.adapter = null

        taitouAdapter = null

        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taitouAdapter = TaitouAdapter(emptyList()) { header, headerPosition, viewPosition ->
            sharedViewModel.deleteHeaderWithView(header, headerPosition, viewPosition)
        }

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

            adapter = taitouAdapter

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

    private fun setupClickListener() {
        binding.buttonAddHeader.setOnClickListener {
            startActivity(Intent(requireActivity(), TaitouEditorActivity::class.java).apply {
                putExtra(
                    "header",
                    HeaderEntity(
                        type = HeaderType.Enterprise,
                        name = "",
                        shuihaoNum = "",
                        phoneNum = "",
                        kaihuyinhang = "",
                        yinhangzhanghao = "",
                        gongsidizhi = "",
                    )
                )
            })
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.headersEventFlow.collect { headersEvent ->
                    val taitouAdapter = taitouAdapter ?: return@collect

                    val (version, shouldUpdate, headers) = headersEvent

                    if (taitouAdapter.version != version) {
                        taitouAdapter.version = version
                        taitouAdapter.submitData(headers, shouldUpdate)
                    }

                    if (shouldUpdate == TaitouAdapter.SHOULD_UPDATE) {
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
                sharedViewModel.headersUpdateChannel0.collect { event ->
                    val taitouAdapter = taitouAdapter ?: return@collect
                    val (position, headers) = event
                    val shouldUpdate = TaitouAdapter.LATER_UPDATE
                    taitouAdapter.submitData(headers, shouldUpdate)
                    taitouAdapter.updateAt(position)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.headersRemoveChannel0.collect { event ->
                    val taitouAdapter = taitouAdapter ?: return@collect
                    val (position, headers) = event
                    val shouldUpdate = TaitouAdapter.LATER_UPDATE
                    taitouAdapter.submitData(headers, shouldUpdate)
                    taitouAdapter.removeAt(position)
                }
            }
        }
    }

    private fun loadNextPage() {
        currentPage++

        sharedViewModel.loadHeaders(pageSize, pageSize * currentPage) { hasMore ->
            loadingLock.set(false)

            this.hasMoreData = hasMore

//            if (!hasMoreData && recyclerViewAdapter.itemCount > 0) {
//                // 已加载全部数据
//            }
        }
    }
}
