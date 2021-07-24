package com.tkroman.kerl.server

import com.tkroman.kerl.receiver.RpcReceiver
import com.tkroman.kerl.server.epmd.EpmdServer
import com.tkroman.kerl.server.node.KerlNode

// EPMD + erlang node
class FullServer(
    private val epmdServer: EpmdServer,
    private val node: KerlNode,
    private val rpcReceiver: RpcReceiver
) {
    fun start() {
        epmdServer.start()
        node.start()
        rpcReceiver.start(node.mailbox())
    }

    fun stop() {
        node.mailbox().close()
        node.stop()
        epmdServer.stop()
    }
}
