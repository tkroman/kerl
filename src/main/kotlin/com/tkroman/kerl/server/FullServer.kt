package com.tkroman.kerl.server

import com.tkroman.kerl.receiver.RpcReceiver
import com.tkroman.kerl.server.epmd.EpmdServer
import com.tkroman.kerl.server.node.KerlNode
import java.io.Closeable

// EPMD + erlang node
class FullServer(
    private val epmdServer: EpmdServer,
    private val node: KerlNode,
    private val rpcReceiver: RpcReceiver
) : Closeable {
    fun start() {
        epmdServer.start()
        node.start()
        val mailbox = node.mailbox()
        rpcReceiver.start(mailbox)
    }

    fun stop() {
        rpcReceiver.close()
        node.mailbox().close()
        node.stop()
        epmdServer.stop()
    }

    override fun close() {
        stop()
    }
}
