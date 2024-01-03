package de.markusressel.mkdocseditor.feature.browser.domain.usecase

import de.markusressel.mkdocseditor.feature.browser.data.DataRepository
import de.markusressel.mkdocseditor.feature.browser.data.SectionData
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import javax.inject.Inject

internal class GetSectionContentUseCase @Inject constructor(
    private val dataRepository: DataRepository,
) {
    operator fun invoke(
        sectionId: String,
        refresh: Boolean = false
    ): Flow<StoreReadResponse<SectionData>> =
        dataRepository.sectionStore.stream<SectionData>(
            StoreReadRequest.cached(
                key = sectionId,
                refresh = refresh
            )
        )
}
