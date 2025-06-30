/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.testing

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
open class RobolectricTest {

    @get:Rule
    val compose = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun before() {
        ShadowLog.stream = System.out
    }

    @After
    fun after() {
        printRoot()
    }

    private fun printRoot() {
        compose.onRoot().printToLog("ComposeTest")
    }
}