package com.twofasapp.core.common.ktx

fun <T> List<T>.toggle(item: T): List<T> =
    if (any { it == item }) filterNot { it == item } else this + item