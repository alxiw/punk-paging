package io.github.alxiw.punkpaging.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.github.alxiw.punkpaging.App
import io.github.alxiw.punkpaging.R
import io.github.alxiw.punkpaging.data.model.Beer
import io.github.alxiw.punkpaging.databinding.FragmentCatalogueBinding
import io.github.alxiw.punkpaging.ui.beers.BeerDialogFragment
import io.github.alxiw.punkpaging.ui.beers.BeersAdapter
import io.github.alxiw.punkpaging.ui.beers.BeersLoadStateAdapter
import io.github.alxiw.punkpaging.ui.beers.BeersViewModel
import io.github.alxiw.punkpaging.ui.beers.RemotePresentationState
import io.github.alxiw.punkpaging.ui.beers.UiAction
import io.github.alxiw.punkpaging.ui.beers.UiState
import io.github.alxiw.punkpaging.ui.beers.asRemotePresentationState
import io.github.alxiw.simplesearchview.SimpleSearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class CatalogueFragment : Fragment(), MenuProvider {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: BeersViewModel by viewModels<BeersViewModel> { viewModelFactory }

    private lateinit var binding: FragmentCatalogueBinding

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_catalogue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCatalogueBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED)

        binding.bindSearch(
            uiState = viewModel.state,
            onQueryChanged = viewModel.accept
        )

        val beersAdapter = BeersAdapter(::onItemClicked, imageLoader)
        binding.beersRecycler.adapter = beersAdapter.withLoadStateFooter(
            footer = BeersLoadStateAdapter { beersAdapter.retry() }
        )

        binding.bindList(
            beersAdapter = beersAdapter,
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            onScrollChanged = viewModel.accept
        )

        (activity as AppCompatActivity).onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.beersSearch.onBackPressed()) {
                        onBackAction()

                        return
                    }

                    (activity as AppCompatActivity).finish()
                }
            }
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
        val item = menu.findItem(R.id.action_search)
        binding.beersSearch.setMenuItem(item)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favourites -> {
                Log.d("HELLO", "FAVOURITES CLICKED")
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null

        super.onDestroyView()
    }

    private fun FragmentCatalogueBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ) {
        beersSearch.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {

            var job: Job? = null

            override fun onQueryTextSubmit(query: String): Boolean {
                val oldQuery = uiState.value.query
                Log.d("HELLO", "On query text submit, old query is <$oldQuery>, new query is <$query>")

                if (oldQuery != query) {
                    Log.d("HELLO", "Query changing from <$oldQuery> to <$query> submitted")
                    binding.beersSearch.clearFocus()
                    onQueryChanged(UiAction.Search(query = query.trim()))
                }

                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val oldQuery = uiState.value.query
                Log.d("HELLO", "On query text change, old query is <$oldQuery>, new query is <$query>")

                if (query.isEmpty() && oldQuery != query) {
                    Log.d("HELLO", "Query text changing from <$oldQuery> to <$query> applied")
                    job?.cancel()
                    job = CoroutineScope(Dispatchers.Main).launch {
                        delay(1000)
                        binding.beersSearch.clearFocus()
                        onQueryChanged(UiAction.Search(query = query.trim())) // query is empty
                    }
                } else {
                    job?.cancel()
                }

                return true
            }

            override fun onQueryTextCleared(): Boolean {
                val oldQuery = uiState.value.query
                Log.d("HELLO", "On query text cleared, old query is <$oldQuery>")

                if (oldQuery.isNotEmpty()) {
                    Log.d("HELLO", "Query text changing from <$oldQuery> to <> applied")
                    binding.beersSearch.clearFocus()
                    onQueryChanged(UiAction.Search(query = ""))
                }

                return true
            }
        })

        beersSearch.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener {
            override fun onSearchViewClosed() = onBackAction()
            override fun onSearchViewShown() = Unit
            override fun onSearchViewShownAnimation() = Unit
            override fun onSearchViewClosedAnimation() = Unit
        })

        lifecycleScope.launch {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect { beersSearch.setQuery(it, true) }
        }
    }

    private fun onBackAction() {
        binding.beersSearch.setQuery("", true)
        viewModel.accept(UiAction.Search(query = ""))
        binding.beersRecycler.scrollToPosition(0)
    }

    private fun FragmentCatalogueBinding.bindList(
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
            beersAdapter.loadStateFlow.collect { loadState: CombinedLoadStates ->
                val isListEmpty = loadState.refresh is LoadState.NotLoading && beersAdapter.itemCount == 0
                // show empty list
                beersEmptyList.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                beersRecycler.isVisible = (loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading) && !isListEmpty
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
        val fm: FragmentManager = childFragmentManager
        val beerDialog = BeerDialogFragment.newInstance(
            beer.id,
            beer.name,
            beer.tagline,
            beer.description,
            beer.abv,
            beer.firstBrewed,
            beer.image
        )
        beerDialog.show(fm, "fragment_beer")

        Log.d("HELLO", "Beer ${beer.id} clicked")
    }
}
