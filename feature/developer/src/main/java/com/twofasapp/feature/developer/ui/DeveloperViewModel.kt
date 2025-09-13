/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.crypto.WordList
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.purchases.PurchasesOverrideRepository
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

internal class DeveloperViewModel(
    appBuild: AppBuild,
    private val vaultsRepository: VaultsRepository,
    private val itemsRepository: ItemsRepository,
    private val purchasesRepository: PurchasesRepository,
    private val purchasesOverrideRepository: PurchasesOverrideRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val securityRepository: SecurityRepository,
    private val tagsRepository: TagsRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(
        DeveloperUiState(
            appBuild = appBuild,
        ),
    )

    init {
        launchScoped {
            itemsRepository.observeItems(vaultsRepository.getVault().id).collect { logins ->
                uiState.update { it.copy(loginItemsCount = logins.size) }
            }
        }

        launchScoped {
            uiState.update {
                it.copy(
                    seed = securityRepository.getSeed(),
                )
            }
        }

        launchScoped {
            purchasesRepository.observeSubscriptionPlan().collect { plan ->
                uiState.update { it.copy(subscriptionPlan = plan) }
            }
        }

        launchScoped {
            purchasesOverrideRepository.observeOverrideSubscriptionPlan().collect { plan ->
                uiState.update { it.copy(overrideSubscriptionPlan = plan) }
            }
        }
    }

    fun generateTestItems(securityType: SecurityType) {
        launchScoped {
            val vault = vaultsRepository.getVault()
            vaultCryptoScope.withVaultCipher(vault) {
                repeat(1) {
                    launchScoped(Dispatchers.IO) {
                        itemsRepository.importItems(
                            buildList {
                                val id = Random.nextInt(9999)
                                val tier = securityType
                                val addNote = Random.nextBoolean()

                                add(
                                    Item.create(
                                        securityType = tier,
                                        contentType = "login",
                                        content = ItemContent.Login.Empty.copy(
                                            name = when (tier) {
                                                SecurityType.Tier1 -> "Name $id (T1)"
                                                SecurityType.Tier2 -> "Name $id (T2)"
                                                SecurityType.Tier3 -> "Name $id (T3)"
                                            },
                                            username = buildString {
                                                append("user")
                                                append(
                                                    when (tier) {
                                                        SecurityType.Tier1 -> 1
                                                        SecurityType.Tier2 -> 2
                                                        SecurityType.Tier3 -> 3
                                                    },
                                                )
                                                append("@mail$id.com")
                                            },
                                            password = SecretField.ClearText("pass$id"),
                                            uris = listOf(ItemUri("https://uri$id.com")),
                                            iconType = IconType.Label,
                                            customImageUrl = null,
                                            labelText = id.toString().take(2),
                                            labelColor = null,
                                            notes = if (addNote) "Lorem ipsum dolor sit amet $id" else null,
                                        ),
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    fun generateRandomTestItems(count: Int, onComplete: () -> Unit) {
        launchScoped {
            itemsRepository.importItems(
                buildList {
                    repeat(count) {
                        val id = Random.nextInt(9999999)
                        val tier = SecurityType.entries.random()
                        val addNote = Random.nextBoolean()

                        add(
                            Item.create(
                                securityType = tier,
                                contentType = "login",
                                content = ItemContent.Login.Empty.copy(
                                    name = when (tier) {
                                        SecurityType.Tier1 -> "Name $id (T1)"
                                        SecurityType.Tier2 -> "Name $id (T2)"
                                        SecurityType.Tier3 -> "Name $id (T3)"
                                    },
                                    username = buildString {
                                        append("user")
                                        append(
                                            when (tier) {
                                                SecurityType.Tier1 -> 1
                                                SecurityType.Tier2 -> 2
                                                SecurityType.Tier3 -> 3
                                            },
                                        )
                                        append("@mail$id.com")
                                    },
                                    password = SecretField.ClearText("pass$id"),
                                    uris = listOf(ItemUri("https://uri$id.com")),
                                    iconType = IconType.Label,
                                    customImageUrl = null,
                                    labelText = id.toString().take(2),
                                    labelColor = null,
                                    notes = if (addNote) "Lorem ipsum dolor sit amet $id" else null,
                                ),
                            ),
                        )
                    }
                },
            )
        }.invokeOnCompletion { onComplete() }
    }

    fun generateTopDomainItems(onComplete: () -> Unit) {
        launchScoped {
            itemsRepository.importItems(
                buildList {
                    DevTopDomains.list.forEach { domain ->
                        val tier = SecurityType.Tier3
                        val addNote = Random.nextBoolean()
                        val rand = Random.nextInt(9999999)

                        add(
                            Item.create(
                                securityType = tier,
                                contentType = "login",
                                content = ItemContent.Login.Empty.copy(
                                    name = domain,
                                    username = buildString {
                                        append("user")
                                        append(
                                            when (tier) {
                                                SecurityType.Tier1 -> 1
                                                SecurityType.Tier2 -> 2
                                                SecurityType.Tier3 -> 3
                                            },
                                        )
                                        append("@$domain")
                                    },
                                    password = SecretField.ClearText("pass$rand"),
                                    uris = listOf(ItemUri("https://$domain")),
                                    iconType = IconType.Icon,
                                    iconUriIndex = 0,
                                    customImageUrl = null,
                                    labelText = null,
                                    labelColor = null,
                                    notes = if (addNote) "Lorem ipsum dolor sit amet $rand" else null,
                                ),
                            ),
                        )
                    }
                },
            )
        }.invokeOnCompletion { onComplete() }
    }

    fun setSubscriptionOverride(plan: String?) {
        launchScoped {
            purchasesOverrideRepository.setOverrideSubscriptionPlan(plan)
        }
    }

    fun insertRandomTag() {
        launchScoped {
            tagsRepository.saveTags(
                Tag(
                    id = "",
                    vaultId = vaultsRepository.getVault().id,
                    name = WordList.words.random().replaceFirstChar { it.uppercase() },
                    position = Random.nextInt(1000),
                    color = null,
                    updatedAt = 0,
                    assignedItemsCount = 0,
                ),
            )
        }
    }

    fun deleteAll() {
        launchScoped {
            itemsRepository.permanentlyDeleteAll()
        }
    }
}