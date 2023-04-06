/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

/**
 * Check if [Char] is in lower case
 */
public fun Char.isLowerCase(): Boolean = lowercaseChar() == this

/**
 * Convert [String] to [CharArray]
 */
public fun String.toCharArray(): CharArray = CharArray(length) { get(it) }
