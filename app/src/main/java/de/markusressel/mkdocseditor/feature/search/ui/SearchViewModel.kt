package de.markusressel.mkdocseditor.feature.search.ui

import androidx.core.text.trimmedLength
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.markusressel.mkdocseditor.feature.browser.domain.usecase.SearchUseCase
import de.markusressel.mkdocseditor.feature.search.domain.SearchResultItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val currentSearchResults: Flow<List<SearchResultItem>> = combine(
        uiState.map { it.currentSearchFilter }.distinctUntilChanged(),
        uiState.map { it.isSearchExpanded }.distinctUntilChanged(),
    ) { currentSearchFilter, isSearching ->
        when {
            isSearching && currentSearchFilter.trimmedLength() > 0 -> searchUseCase(
                currentSearchFilter
            )

            else -> emptyList()
        }
    }


    fun onUiEvent(event: UiEvent) {
        viewModelScope.launch {
            when (event) {
                is UiEvent.SearchInputChanged -> {
                    setSearch(event.searchFilter)
                }

                is UiEvent.SearchExpandedChanged -> onSearchExpandedChanged(event.isSearchExpanded)
                is UiEvent.SearchRequested -> onSearchRequested(event.searchFilter)
                is UiEvent.SearchResultClicked -> onSearchResultClicked(event.searchResultItem)
            }
        }
    }

    private fun onSearchResultClicked(searchResultItem: SearchResultItem) {
        when (searchResultItem) {
            is SearchResultItem.Section -> {

            }

            is SearchResultItem.Document -> {

            }

            is SearchResultItem.Resource -> {

            }
        }
    }

    private fun onSearchExpandedChanged(searchExpanded: Boolean) {
        if (searchExpanded.not()) {
            clearSearch()
        }
    }


    /**
     * Set the search string
     *
     * @return true if the value has changed, false otherwise
     */
    private fun setSearch(text: String): Boolean {
        return if (uiState.value.currentSearchFilter != text) {
            _uiState.update { old ->
                old.copy(
                    currentSearchFilter = text,
                )
            }
            true
        } else false
    }

    private fun clearSearch() {
        setSearch("")
        if (uiState.value.isSearchExpanded) {
            _uiState.update { old ->
                old.copy(
                    isSearchExpanded = false,
                )
            }
        }
    }

    private fun onSearchRequested(query: String) {
        setSearch(query)
        _uiState.update { old ->
            old.copy(isSearchExpanded = true)
        }
    }

    data class UiState(
        val currentSearchFilter: String = "",
        val isSearchExpanded: Boolean = false,
        val currentSearchResults: List<SearchResultItem> = emptyList(),
    )

    sealed class UiEvent {
        data class SearchInputChanged(val searchFilter: String) : UiEvent()
        data class SearchRequested(val searchFilter: String) : UiEvent()
        data class SearchExpandedChanged(val isSearchExpanded: Boolean) : UiEvent()

        data class SearchResultClicked(val searchResultItem: SearchResultItem) : UiEvent()
    }

}