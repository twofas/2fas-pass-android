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
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.crypto.WordList
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.data.main.ConnectedBrowsersRepository
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
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
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
                                        contentType = ItemContentType.Login,
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
                                    ).copy(id = Uuid.generate()),
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
                                contentType = ItemContentType.Login,
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
                            ).copy(id = Uuid.generate()),
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
                                contentType = ItemContentType.Login,
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
                            ).copy(id = Uuid.generate()),
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

    fun insertRandomSecureNote() {
        launchScoped(Dispatchers.IO) {
            val vault = vaultsRepository.getVault()
            val securityType = SecurityType.entries.random()
            val nameSeed = WordList.words.random().replaceFirstChar { it.uppercase() }
            val body = (1..Random.nextInt(2, 5)).joinToString(separator = "\n\n") { loremSentences.random() }

            itemsRepository.importItems(
                listOf(
                    Item.create(
                        securityType = securityType,
                        contentType = ItemContentType.SecureNote,
                        vaultId = vault.id,
                        content = ItemContent.SecureNote(
                            name = "Secure Note: $nameSeed",
                            text = SecretField.ClearText(body),
                        ),
                    ).copy(id = Uuid.generate()),
                ),
            )
        }
    }

    fun deleteAll() {
        launchScoped {
            itemsRepository.permanentlyDeleteAll()
        }
    }

    fun deleteAllBrowsers() {
        launchScoped {
            connectedBrowsersRepository.permanentlyDeleteAll()
        }
    }

    private companion object {
        val loremSentences = listOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae.",
            "Integer fringilla risus at ipsum facilisis, vitae dictum lorem molestie.",
            "Praesent eget sapien non nisl tincidunt venenatis.",
            "Morbi ut augue a arcu pharetra consequat vel in metus.",
            "Curabitur sit amet erat eu odio maximus dignissim.",
            "Suspendisse potenti. Aenean a augue libero.",
            "Nulla facilisi. Pellentesque habitant morbi tristique senectus et netus et malesuada fames.",
            "Donec efficitur elit eu massa vulputate, vitae porta sapien aliquet.",
            "Sed vehicula magna a nunc viverra, in tincidunt ipsum volutpat.",
        )
    }
}