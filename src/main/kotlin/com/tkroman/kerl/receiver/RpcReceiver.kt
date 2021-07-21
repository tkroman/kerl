package com.tkroman.kerl.receiver

import com.tkroman.kerl.executor.RpcExecutor
import io.appulse.encon.Node
import java.io.Closeable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class RpcReceiver(
    node: Node,
    private val rpcExecutor: RpcExecutor,
) : Closeable {
    private val rex = node
        .mailbox()
        .queue(ArrayBlockingQueue(4096))
        .name("rex")
        .build()

    private val pollForExecution: () -> Unit = {
        rex.receive(1, TimeUnit.MILLISECONDS)?.also {
            rpcExecutor.executeAsync(it.body, rex)
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

    private val scheduled = AtomicReference<ScheduledFuture<*>>(null)

    fun start() {
        scheduled.compareAndSet(
            null,
            poller.scheduleWithFixedDelay(
                pollForExecution,
                0L,
                1L,
                TimeUnit.MILLISECONDS
            )
        )
    }

    override fun close() {
        scheduled.getAndSet(null)?.cancel(true)
    }
}

