/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.readTextFile
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvListener
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.CsvRow
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.TransferUtils

internal class DashlaneMobileImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
) : ImportSpec() {
    override val type = ImportType.DashlaneMobile
    override val name = "Dashlane (Mobile)"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_dashlane
    override val instructions = context.getString(R.string.transfer_instructions_dashlane_mobile)
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_csv),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        var unknownItems = 0
        tags.clear()

        val items = buildList {
            var csvType: CsvType = CsvType.Unknown

            CsvParser.parse(
                text = context.readTextFile(uri),
                listener = object : CsvListener {
                    override fun onHeaders(headersList: List<String>) {
                        val h = headersList.asSequence()
                            .map { it.trim().lowercase() }
                            .toSet()

                        fun hasAll(vararg keys: String) = keys.all(h::contains)
                        fun hasAny(vararg keys: String) = keys.any(h::contains)

                        csvType = when {
                            hasAll("username", "title", "password", "url") -> CsvType.Credentials
                            hasAll("title", "note") && !h.contains("password") -> CsvType.SecureNotes
                            hasAll("type", "account_holder") -> CsvType.Payments
                            hasAll("type", "number", "name") && h.contains("issue_date") -> CsvType.Ids
                            h.contains("type") && hasAny("first_name", "email", "phone_number", "address") -> CsvType.PersonalInfo
                            h.contains("ssid") -> CsvType.WiFi
                            else -> CsvType.Unknown
                        }
                    }

                    override fun onRow(row: CsvRow) {
                        val tagIds: List<String> = resolveTagIds(
                            raw = row.get("category"),
                            vaultId = vaultId,
                            separator = ',',
                        )

                        when (csvType) {
                            CsvType.Credentials -> {
                                val username = row.get("username") ?: row.get("username2") ?: row.get("username3")
                                val note = row.get("note")
                                val noteWithAdditionalInfo = TransferUtils.formatNote(
                                    note = note,
                                    fields = buildMap {
                                        row.get("username2")?.takeIf { it != username }?.let { put("Username", it) }
                                        row.get("username3")?.takeIf { it != username }?.let { put("Alternate username", it) }
                                    },
                                )

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.Login,
                                        content = ItemContent.Login.create(
                                            name = row.get("title"),
                                            username = username,
                                            password = row.get("password"),
                                            url = row.get("url"),
                                            notes = noteWithAdditionalInfo,
                                        ),
                                    ),
                                )
                            }

                            CsvType.SecureNotes -> {
                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = row.get("title"),
                                            text = row.get("note"),
                                        ),
                                    ),
                                )
                            }

                            CsvType.Payments -> {
                                // TODO: Implement when payment cards done
                                unknownItems++

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = row.get("name") ?: row.get("account_name"),
                                            text = TransferUtils.formatNote(
                                                note = row.get("note"),
                                                fields = buildMap {
                                                    row.get("type")?.let { put("Type", it) }
                                                    row.get("account_name")?.let { put("Account name", it) }
                                                    row.get("account_holder")?.let { put("Account holder", it) }
                                                    row.get("cc_number")?.let { put("Card number", it) }
                                                    row.get("code")?.let { put("Security code", it) }
                                                    row.get("expiration_month")?.let { put("Expiration month", it) }
                                                    row.get("expiration_year")?.let { put("Expiration year", it) }
                                                    row.get("routing_number")?.let { put("Routing number", it) }
                                                    row.get("account_number")?.let { put("Account number", it) }
                                                    row.get("country")?.let { put("Country", it) }
                                                    row.get("issuing_bank")?.let { put("Issuing bank", it) }
                                                },
                                            ),
                                        ),
                                    ),
                                )
                            }

                            CsvType.Ids -> {
                                unknownItems++

                                val rawType = row.get("type") ?: "id"
                                val typeName = rawType
                                    .replace("_", " ")
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() }

                                val idName = row.get("name")

                                val itemName = buildString {
                                    if (!idName.isNullOrBlank()) {
                                        append(idName)
                                        append(" ")
                                    }
                                    append("($typeName)")
                                }

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = itemName,
                                            text = TransferUtils.formatNote(
                                                note = row.get("note"),
                                                fields = row.map.minus(listOf("type", "name")),
                                            ),
                                        ),
                                    ),
                                )
                            }

                            CsvType.PersonalInfo -> {
                                unknownItems++

                                val rawType = row.get("type") ?: "personal"
                                val typeName = rawType
                                    .replace("_", " ")
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() }

                                val detectedName: String? = when (rawType) {
                                    "name" -> listOfNotNull(
                                        row.get("first_name"),
                                        row.get("middle_name"),
                                        row.get("last_name"),
                                    )
                                        .joinToString(" ")
                                        .takeIf { it.isNotBlank() }

                                    "email" -> row.get("email")
                                    "number" -> row.get("phone_number")
                                    "website" -> row.get("url")
                                    else -> null
                                }

                                val displayName = row.get("item_name") ?: detectedName

                                val itemName = buildString {
                                    if (!displayName.isNullOrBlank()) {
                                        append(displayName)
                                        append(" ")
                                    }
                                    append("($typeName)")
                                }

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = itemName,
                                            text = TransferUtils.formatNote(
                                                note = null,
                                                fields = row.map.minus(listOf("type", "item_name")),
                                            ),
                                        ),
                                    ),
                                )
                            }

                            CsvType.WiFi -> {
                                unknownItems++

                                val ssid = row.get("ssid")
                                val wifiName = row.get("name")
                                val displayName = wifiName ?: ssid

                                val itemName = buildString {
                                    if (!displayName.isNullOrBlank()) {
                                        append(displayName)
                                        append(" ")
                                    }
                                    append("(WiFi)")
                                }

                                val additionalInfo = row.map.minus(listOf("name", "note"))
                                val noteText = TransferUtils.formatNote(
                                    note = row.get("note"),
                                    fields = additionalInfo,
                                )

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = itemName,
                                            text = noteText,
                                        ),
                                    ),
                                )
                            }

                            CsvType.Unknown -> {
                                unknownItems++

                                add(
                                    Item.create(
                                        vaultId = vaultId,
                                        tagIds = tagIds,
                                        contentType = ItemContentType.SecureNote,
                                        content = ItemContent.SecureNote.create(
                                            name = row.get("name"),
                                            text = TransferUtils.formatNote(
                                                note = row.get("note"),
                                                fields = row.map.minus(listOf("name", "note", "type")),
                                            ),
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                },
            )
        }

        return ImportContent(
            items = items,
            tags = tags,
            unknownItems = unknownItems,
        )
    }

    enum class CsvType {
        Credentials, SecureNotes, Payments, Ids, PersonalInfo, WiFi, Unknown
    }
}