package io.github.alxiw.punkpaging.ui

import android.app.SearchManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.collection.LongSparseArray
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.github.alxiw.punkpaging.App
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.data.model.Beer
import io.github.alxiw.punkpaging.databinding.ActivityMainBinding
import io.github.alxiw.punkpaging.di.component.ActivityComponent
import io.github.alxiw.punkpaging.di.component.ConfigPersistentComponent
import io.github.alxiw.punkpaging.di.component.DaggerConfigPersistentComponent
import io.github.alxiw.punkpaging.di.module.ActivityModule
import io.github.alxiw.punkpaging.ui.beers.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var searchManager: SearchManager

    // Sparse arrays are cheaper memory-wise, see - https://stackoverflow.com/a/31413003/1271136
    private val componentsCache: LongSparseArray<ConfigPersistentComponent> = LongSparseArray()

    private var activityId: Long = 0

    private lateinit var binding: ActivityMainBinding

    private val viewModel: BeersViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[BeersViewModel::class.java]
    }

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag("MainActivity")
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // here we try to reuse components instances between configuration changes
        activityId = restoreOrIncrementActivityId(savedInstanceState)

        val component: ConfigPersistentComponent = reuseOrCreateConfigPersistentComponent()
        val activityComponent: ActivityComponent = component.activityComponent(ActivityModule(this))

        activityComponent.inject(this)

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept,
        )

        Timber.d("Activity created")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ACTIVITY_KEY_ID, activityId)
    }

    override fun onBackPressed() {
        if (binding.beersSearch.query.trim().isNotEmpty()) {
            binding.beersSearch.setQuery("", true)
            binding.updateBeersListFromInput(viewModel.accept)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }

    private fun ActivityMainBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Beer>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val beersAdapter = BeersAdapter(::onItemClicked)
        val header = BeersLoadStateAdapter { beersAdapter.retry() }
        beersRecycler.adapter = beersAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = BeersLoadStateAdapter { beersAdapter.retry() }
        )
        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions
        )
        bindList(
            header = header,
            beersAdapter = beersAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions
        )
    }

    private fun ActivityMainBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ) {
        beersSearch.let { view ->
            view.findViewById<TextView>(R.id.search_src_text).setPadding(50, 0, 0, 0)
            view.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val oldQuery = uiState.value.query
                    Timber.d("Query changing is trying to submit from <$oldQuery> to <$query>")
                    if (oldQuery != query) {
                        Timber.d("Query changing from <$oldQuery> to <$query> submitted")
                        view.clearFocus()

                        updateBeersListFromInput(onQueryChanged)
                    }
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    val oldQuery = uiState.value.query
                    Timber.d("Query in search input is changing to <$query>")
                    if (query.isEmpty() && oldQuery != query) {
                        Timber.d("Query changing from <$oldQuery> to <$query> applied")
                        view.clearFocus()
                        updateBeersListFromInput(onQueryChanged)
                    }
                    return true
                }
            })
            lifecycleScope.launch {
                uiState
                    .map { it.query }
                    .distinctUntilChanged()
                    .collect { view.setQuery(it, true) }
            }
        }
    }

    private fun ActivityMainBinding.updateBeersListFromInput(onQueryChanged: (UiAction.Search) -> Unit) {
        beersSearch.query.trim().let {
            onQueryChanged(UiAction.Search(query = it.toString()))
        }
    }

    private fun ActivityMainBinding.bindList(
        header: BeersLoadStateAdapter,
        beersAdapter: BeersAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Beer>>,
        onScrollChanged: (UiAction.Scroll) -> Unit
    ) {
        retryButton.setOnClickListener { beersAdapter.retry() }
        beersRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentQuery = uiState.value.query))
            }
        })
        val notLoading = beersAdapter.loadStateFlow
            .asRemotePresentationState()
            .map { it == RemotePresentationState.PRESENTED }

        val hasNotScrolledForCurrentSearch = uiState
            .map { it.hasNotScrolledForCurrentSearch }
            .distinctUntilChanged()

        val shouldScrollToTop = combine(notLoading, hasNotScrolledForCurrentSearch, Boolean::and)
            .distinctUntilChanged()

        lifecycleScope.launch {
            pagingData.collectLatest(beersAdapter::submitData)
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                if (shouldScroll) {
                    beersRecycler.scrollToPosition(0)
                }
            }
        }

        lifecycleScope.launch {
            beersAdapter.loadStateFlow.collect { loadState ->
                // Show a retry header if there was an error refreshing, and items were previously
                // cached OR default to the default prepend state
                header.loadState = loadState.mediator
                    ?.refresh
                    ?.takeIf { it is LoadState.Error && beersAdapter.itemCount > 0 }
                    ?: loadState.prepend

                val isListEmpty = loadState.refresh is LoadState.NotLoading && beersAdapter.itemCount == 0
                // show empty list
                beersEmptyList.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                beersRecycler.isVisible = loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
                // Show loading spinner during initial load or refresh.
                beersProgressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                // Show the retry state if initial load or refresh fails.
                retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error && beersAdapter.itemCount == 0
                // Snackbar on any error, regardless of whether it came from RemoteMediator or PagingSource
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                errorState?.let {
                    Snackbar.make(
                        binding.root,
                        "\uD83D\uDE28 Wooops ${it.error}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun onItemClicked(beer: Beer) {
        val fm: FragmentManager = supportFragmentManager
        val beerDialog = BeerDialogFragment.newInstance(beer.id, beer.name, beer.description)
        beerDialog.show(fm, "fragment_beer")

        Timber.d("Beer ${beer.id} clicked")
    }

    private fun restoreOrIncrementActivityId(savedInstanceState: Bundle?): Long {
        return savedInstanceState?.getLong(ACTIVITY_KEY_ID) ?:NEXT_ID_COUNTER.incrementAndGet()
    }

    private fun reuseOrCreateConfigPersistentComponent(): ConfigPersistentComponent {
        val configPersistentComponent: ConfigPersistentComponent
        if (componentsCache.get(activityId) == null) {
            Timber.d("Not found cached, creating new ConfigPersistentComponent id=$activityId")
            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                .appComponent(App[this].appComponent)
                .build()
            componentsCache.put(activityId, configPersistentComponent)
        } else {
            Timber.d("Reusing ConfigPersistentComponent id=$activityId")
            configPersistentComponent = componentsCache.get(activityId)!!
        }
        return configPersistentComponent
    }

    companion object {
        private val NEXT_ID_COUNTER = AtomicLong(0)

        private const val ACTIVITY_KEY_ID = "ACTIVITY_KEY_ID"
    }
}
