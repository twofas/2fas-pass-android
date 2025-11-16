package com.twofasapp.core.common.ktx

fun <T> List<T>.toggle(item: T): List<T> =
    if (any { it == item }) filterNot { it == item } else this + item

/**
 * Returns the single element of this list if all elements are equal.
 * Otherwise, returns null.
 */
fun <T> List<T>.uniform(): T? {
    val distinct = this.distinct()
    return if (distinct.size == 1) distinct[0] else null
}