package io.github.alxiw.punkpaging.ui

import android.app.SearchManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.alxiw.punkpaging.App
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.data.model.Beer
import io.github.alxiw.punkpaging.databinding.ActivityMainBinding
import io.github.alxiw.punkpaging.ui.beers.BeersAdapter
import io.github.alxiw.punkpaging.ui.beers.BeersLoadingAdapter
import io.github.alxiw.punkpaging.ui.beers.BeersViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var searchManager: SearchManager

    private val adapter = BeersAdapter(::onItemClicked)

    private var searchJob: Job? = null

    private val viewModel: BeersViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[BeersViewModel::class.java]
    }

    private lateinit var binding: ActivityMainBinding

    private var forceLoadFromCache: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        (application as App).appComponent.inject(this)

        restoreSavedInstanceState(savedInstanceState)
        initAdapter()
        val query =  if (viewModel.currentQuery.isNotEmpty()) {
            viewModel.currentQuery
        } else {
            DEFAULT_QUERY
        }
        fetchBeers(query)
        initSearch()
        restoreBeer()
    }

    private fun initSearch() {
        binding.beersSearch.let { view ->
            view.findViewById<TextView>(R.id.search_src_text)
                .setPadding(50, 0, 0, 0)
            view.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    view.clearFocus()
                    fetchBeers(query)
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (forceLoadFromCache) {
                        forceLoadFromCache = false
                    } else {
                        if (query.isEmpty()) {
                            view.clearFocus()
                            fetchBeers(query)
                        }
                    }
                    return true
                }
            })
        }
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                    // Only emit when REFRESH LoadState for RemoteMediator changes.
                    .distinctUntilChangedBy { it.refresh }
                    // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                    .filter {
                        it.refresh is LoadState.NotLoading && it.mediator?.refresh !is LoadState.NotLoading
                    }
                    .collect { binding.beersRecycler.scrollToPosition(0) }
        }
    }

    private fun restoreBeer() {
        viewModel.clickedBeer?.let {
            onItemClicked(it)
        }
    }

    private fun fetchBeers(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.searchBeers(query).collectLatest {
                adapter.submitData(it)
            }
        }

    }

    private fun restoreSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }
        forceLoadFromCache = savedInstanceState.getBoolean(BEERS_CACHE_TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        forceLoadFromCache = true
        outState.putBoolean(BEERS_CACHE_TAG, forceLoadFromCache)
    }

    override fun onBackPressed() {
        if (viewModel.currentQuery.isNotEmpty()) {
            binding.beersSearch.setQuery(DEFAULT_QUERY, true)
            fetchBeers(DEFAULT_QUERY)
        } else {
            super.onBackPressed()
        }
    }

    private fun initAdapter() {
        binding.beersRecycler.adapter = adapter
        binding.beersRecycler.adapter = adapter.withLoadStateFooter(
            //header = BeersLoadingAdapter { adapter.retry() },
            footer = BeersLoadingAdapter { adapter.retry() }
        )
        adapter.addLoadStateListener { loadState ->
            // Only show the list if refresh succeeds.
            binding.beersRecycler.isVisible = loadState.source.refresh is LoadState.NotLoading
            // Show loading spinner during initial load or refresh.
            binding.beersProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
            // Show the retry state if initial load or refresh fails.
            binding.beersEmptyList.isVisible = loadState.source.refresh is LoadState.Error

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                             ?: loadState.source.prepend as? LoadState.Error
                             ?: loadState.append as? LoadState.Error
                             ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                        this,
                        "\uD83D\uDE28 Wooops ${it.error}",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun onItemClicked(beer: Beer) {
        viewModel.clickedBeer = beer
        MaterialAlertDialogBuilder(this)
            .setTitle(beer.name)
            .setMessage(beer.description)
            .setOnDismissListener {
                viewModel.clickedBeer = null
            }
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, which ->
                // do nothing
            }
            .show()
    }

    companion object {

        private const val BEERS_CACHE_TAG = "forced_load_from_cache"
        private const val DEFAULT_QUERY = ""
    }
}
