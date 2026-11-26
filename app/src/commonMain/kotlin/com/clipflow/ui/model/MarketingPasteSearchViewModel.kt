package com.clipflow.ui.model

import com.clipflow.paste.PasteData
import com.clipflow.paste.PasteTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MarketingPasteSearchViewModel(
    marketingPasteData: MarketingPasteData,
) : PasteSearchViewModel() {
    override val convertTerm: (String) -> List<String> = { inputSearch ->
        inputSearch
            .trim()
            .lowercase()
            .split("\\s+".toRegex())
            .filterNot { it.isEmpty() }
            .distinct()
    }

    override val tagList: StateFlow<List<PasteTag>> =
        MutableStateFlow(listOf())

    override val searchResults: StateFlow<List<PasteData>> =
        MutableStateFlow(marketingPasteData.getPasteDataList())
}
