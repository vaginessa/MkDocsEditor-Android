package de.markusressel.mkdocseditor.feature.backendconfig.common.domain

import de.markusressel.mkdocseditor.feature.backendconfig.common.data.BackendConfig
import de.markusressel.mkdocseditor.feature.backendconfig.common.data.BackendConfigRepository
import de.markusressel.mkdocseditor.feature.backendconfig.common.data.toBackendConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal class BackendManager @Inject constructor(
    private val backendConfigRepository: BackendConfigRepository
) {

    val currentBackend: StateFlow<BackendConfig?> = backendConfigRepository
        .selectedBackendConfigFlow()
        .map { it?.toBackendConfig() }
        .stateIn(GlobalScope, SharingStarted.Eagerly, null)

    fun setBackend(backendConfig: BackendConfig) {
        backendConfigRepository.selectBackendConfig(backendConfig)
    }

}