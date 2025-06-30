/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.parser

import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.service.autofill.FillRequest
import com.twofasapp.feature.autofill.service.PassAutofillService.Companion.AutofillTag
import timber.log.Timber

internal class NodeParser {
    private var packageName: String? = null
    private var webDomain: String? = null
    private val inputs: MutableList<AutofillInput> = mutableListOf()

    fun parse(fillRequest: FillRequest): NodeStructure {
        return fillRequest
            .fillContexts
            .lastOrNull()
            ?.structure
            ?.let {
                parseStructure(it)
            }
            ?: NodeStructure.Empty
    }

    private fun parseStructure(structure: AssistStructure): NodeStructure {
        val windowNodes = (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) }

        windowNodes.forEach { windowNode ->
            parseNode(
                viewNode = windowNode.rootViewNode,
                siblingsKeywords = setOf(),
                parentsKeywords = setOf(),
            )
        }

        return NodeStructure(
            packageName = structure.getPackageName(),
            webDomain = webDomain,
            inputs = inputs.filterBestMatches(),
        )
    }

    private fun parseNode(
        viewNode: ViewNode?,
        siblingsKeywords: Set<String>,
        parentsKeywords: Set<String>,
    ) {
        if (viewNode == null) return

        if (viewNode.webDomain != null && webDomain == null) {
            webDomain = viewNode.webDomain
        }

        if (viewNode.idPackage != null && packageName == null && viewNode.idPackage != "android") {
            packageName = viewNode.idPackage
        }

        AutofillNode.from(
            viewNode = viewNode,
            siblingsKeywords = siblingsKeywords,
            parentsKeywords = parentsKeywords,
        )?.let { autofillNode ->
            Timber.tag(AutofillTag).d(autofillNode.log())

            inputs.addAll(
                findAutofillInputs(autofillNode),
            )
        }

        val nodeKeywords = viewNode.keywords
        val children = viewNode.childrenNodes

        children.forEach { childNode ->
            parseNode(
                viewNode = childNode,
                siblingsKeywords = children.map { it.keywords }.flatten().toSet(),
                parentsKeywords = parentsKeywords + nodeKeywords,
            )
        }
    }

    private fun findAutofillInputs(node: AutofillNode): List<AutofillInput> {
        return buildList {
            val usernameMatchRank = node.matchesAutofillInput(AutofillInputConfig.Username)
            val passwordMatchRank = node.matchesAutofillInput(AutofillInputConfig.Password)

            if (usernameMatchRank != null) {
                add(
                    AutofillInput.Username(
                        id = node.id,
                        matchConfidence = usernameMatchRank,
                        node = node,
                    ),
                )
            }

            if (passwordMatchRank != null) {
                add(
                    AutofillInput.Password(
                        id = node.id,
                        matchConfidence = passwordMatchRank,
                        node = node,
                    ),
                )
            }
        }
    }

    /**
     * Determines the match confidence level between an AutofillNode and a given AutofillInputConfig.
     *
     * Matching criteria (from best to worst):
     *
     * - Exact Match:
     *   (Rank 1) Native Android autofill hints overlap.
     *   (Rank 2) Input type matches variation type, excluding those equal to denied keywords.
     *
     * - Strong Match:
     *   (Rank 3) Keywords match, excluding those containing denied keywords.
     *   (Rank 4) Regex matches keywords, excluding those containing denied keywords.
     *
     * - Weak Match:
     *   (Rank 5) HTML attributes match.
     *   (Rank 6) Regex matches siblings' keywords.
     *   (Rank 7) Regex matches parents' keywords.
     */
    private fun AutofillNode.matchesAutofillInput(config: AutofillInputConfig) = when {
        autofillHints.any { it in config.autofillHints } -> MatchConfidence.Exact(1)
        inputType.matchesInputType(config.inputTypeFlags) && keywords.none { key -> config.deniedKeywords.any { key == it } } -> MatchConfidence.Exact(2)
        keywords.any { it in config.keywords } && keywords.none { key -> config.deniedKeywords.any { key.contains(it) } } -> MatchConfidence.Strong(3)
        keywords.any { config.regex.containsMatchIn(it) } && keywords.none { key -> config.deniedKeywords.any { key.contains(it) } } -> MatchConfidence.Strong(4)
        htmlAttributes.any { it in config.htmlAttributes } -> MatchConfidence.Weak(5)
        siblingsKeywords.any { config.regex.containsMatchIn(it) } -> MatchConfidence.Weak(6)
        parentsKeywords.any { config.regex.containsMatchIn(it) } -> MatchConfidence.Weak(7)
        config == AutofillInputConfig.Password -> null
        else -> null
    }

    /**
     * Filters the list of AutofillInput to return the best matching inputs based on their match confidence rank.
     */
    private fun List<AutofillInput>.filterBestMatches(): List<AutofillInput> {
        val passwords = filterIsInstance<AutofillInput.Password>()
            // If finding fields in native apps, use only strong matches
            .filter {
                if (webDomain == null) {
                    it.matchConfidence.rankValue <= 4
                } else {
                    true
                }
            }

        val usernames = filterIsInstance<AutofillInput.Username>()
            // If finding fields in native apps, use only strong matches
            .filter {
                if (webDomain == null) {
                    it.matchConfidence.rankValue <= 4
                } else {
                    true
                }
            }

        val bestPasswordRank = passwords.minOfOrNull { it.matchConfidence.rankValue }
        val bestUsernameRank = usernames.minOfOrNull { it.matchConfidence.rankValue }

        val bestPasswordMatches = passwords.filter { it.matchConfidence.rankValue == bestPasswordRank }

        // Exclude username matches which has the same input id as a password match (rare cases)
        val usernamesExcludingPasswordMatches = usernames
            .filter { username -> bestPasswordMatches.none { it.id == username.id } }

        // Find usernames which has the same siblings or parents as a password match (high confident it's a best match)
        val usernamesClosestToPasswordMatches = usernamesExcludingPasswordMatches
            .filter { username ->
                bestPasswordMatches.any { password ->
                    password.node?.siblingsKeywords == username.node?.siblingsKeywords || password.node?.parentsKeywords == username.node?.parentsKeywords
                }
            }.toSet()

        // Find usernames with best rank
        val usernamesWithBestRank = usernamesExcludingPasswordMatches
            .filter { username -> username.matchConfidence.rankValue == bestUsernameRank }
            .toSet()

        val bestUsernameMatches =
            // The best match would be the one with the highest rank and closest to the password
            usernamesWithBestRank.intersect(usernamesClosestToPasswordMatches)
                .ifEmpty {
                    // If that's empty, the best match would be the one closest to the password
                    usernamesClosestToPasswordMatches.ifEmpty {
                        // If that's empty, the best match would be the one with highest rank
                        usernamesWithBestRank
                    }
                }.toList()

        return bestUsernameMatches + bestPasswordMatches
    }

    private fun AssistStructure.getPackageName(): String? {
        val packageName = getWindowNodeAt(0).packageName

        return if (browsersPackageNames.contains(packageName)) {
            null
        } else {
            packageName
        }
    }

    private val AssistStructure.WindowNode.packageName: String?
        get() = title.toString().split("/").firstOrNull()

    private val ViewNode.childrenNodes: List<ViewNode>
        get() = (0 until childCount).map { getChildAt(it) }
}