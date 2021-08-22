package com.tkroman.kerl.receiver

import com.tkroman.kerl.executor.RpcExecutor
import io.appulse.encon.mailbox.Mailbox
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class RpcReceiver(
    private val rpcExecutor: RpcExecutor,
) : Closeable {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun pollForExecution(source: Mailbox) {
        val received = source.receive(1, TimeUnit.MILLISECONDS)
        if (received != null) {
            rpcExecutor
                .execute(received.body)
                .handle { r, t ->
                    // ok to do this w/o handleAsync because source.send is just
                    // an enqueue() under the hood. It can block but this is network-layer blocking
                    // (meaning if outgoing queue is full, we don't want to receive new requests yet)
                    // so this is fine. This can be changed in the future if we decide this is non-optimal.
                    when {
                        t != null -> logger.error("Failed to execute $received", t)
                        r == null -> logger.error("Can't reply to $received - failed to parse")
                        else -> try {
                            source.send(r.first, r.second)
                        } catch (e: Exception) {
                            logger.error("Failed to send", e)
                        }
                    }
                }
        }
    }

    private val poller = Executors.newScheduledThreadPool(
        4,
        object : ThreadFactory {
            private val tf = Executors.defaultThreadFactory()
            override fun newThread(r: Runnable): Thread {
                return tf.newThread(r).also { it.isDaemon = true }
            }
        }
    )

    private var scheduled: ScheduledFuture<*>? = null

    fun start(source: Mailbox) {
        synchronized(this) {
            if (scheduled == null) {
                scheduled = poller.scheduleWithFixedDelay(
                    { pollForExecution(source) },
                    0L,
                    1L,
                    TimeUnit.MILLISECONDS
                )
            } else {
                throw IllegalStateException(
                    "Couldn't start RpcReceiver. Make sure it was stopped correctly before reuse"
                )
            }
        }
    }

    override fun close() {
        synchronized(this) {
            scheduled?.also {
                it.cancel(true)
            }
            scheduled = null
        }
    }
}

