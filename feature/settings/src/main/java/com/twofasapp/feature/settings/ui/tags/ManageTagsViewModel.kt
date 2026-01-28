package com.twofasapp.feature.settings.ui.tags

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ManageTagsViewModel(
    private val vaultsRepository: VaultsRepository,
    private val itemsRepository: ItemsRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(ManageTagsUiState())

    init {
        launchScoped {
            uiState.update { state -> state.copy(vaultId = vaultsRepository.getVault().id) }
        }

        launchScoped {
            tagsRepository.observeTags(vaultsRepository.getVault().id).collect { tags ->
                uiState.update { state -> state.copy(tags = tags) }
            }
        }

        launchScoped {
            tagsRepository.observeSuggestedTagColor(vaultsRepository.getVault().id)
                .collect { suggestedTagColor ->
                    uiState.update { state -> state.copy(suggestedTagColor = suggestedTagColor) }
                }
        }
    }

    fun addTag(tag: Tag) {
        launchScoped {
            tagsRepository.saveTags(tag)
        }
    }

    fun editTag(tag: Tag) {
        launchScoped {
            tagsRepository.saveTags(tag)
        }
    }

    fun deleteTag(tag: Tag) {
        launchScoped {
            tagsRepository.deleteTags(tag)
        }

        launchScoped {
            itemsRepository.deleteTag(tag.id)
        }
    }
}