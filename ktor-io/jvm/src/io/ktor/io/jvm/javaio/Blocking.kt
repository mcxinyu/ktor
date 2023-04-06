/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.jvm.javaio

import io.ktor.io.*
import kotlinx.coroutines.*
import java.io.*
import java.lang.Integer.*

/**
 * Create blocking [java.io.InputStream] for this channel that does block every time the channel suspends at read
 * Similar to do reading in [runBlocking] however you can pass it to regular blocking API
 */
public fun ByteReadChannel.toInputStream(parent: Job? = null): InputStream = InputAdapter(parent, this)

/**
 * Create blocking [java.io.OutputStream] for this channel that does block every time the channel suspends at write
 * Similar to do reading in [runBlocking] however you can pass it to regular blocking API
 */
public fun ByteWriteChannel.toOutputStream(parent: Job? = null): OutputStream = OutputAdapter(parent, this)

private class InputAdapter(parent: Job?, private val channel: ByteReadChannel) : InputStream() {

    private val loop = object : BlockingAdapter(parent) {
        override suspend fun loop() {
            var readCount = 0
            while (!channel.isClosedForRead()) {
                val buffer = rendezvous(readCount) as ByteArray

                val size = min(channel.availableForRead, buffer.size)
                val data = channel.readByteArray(size)
                data.copyInto(buffer, 0, 0, data.size)
                readCount = size
            }

            finish(-1)
        }
    }

    private var single: ByteArray? = null

    override fun available(): Int {
        return channel.availableForRead
    }

    @Synchronized
    override fun read(): Int {
        val buffer = single ?: ByteArray(1).also { single = it }
        val rc = loop.submitAndAwait(buffer, 0, 1)
        if (rc == -1) return -1
        if (rc != 1) error("Expected a single byte or EOF. Got $rc bytes.")
        return buffer[0].toInt() and 0xff
    }

    @Synchronized
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return loop.submitAndAwait(b, off, len)
    }

    @Synchronized
    override fun close() {
        super.close()
        channel.cancel()
        loop.shutdown()
    }
}

