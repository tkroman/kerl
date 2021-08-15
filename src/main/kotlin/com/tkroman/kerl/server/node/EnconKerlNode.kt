package com.tkroman.kerl.server.node

import com.tkroman.kerl.server.KerlServerConfig
import io.appulse.encon.Node
import io.appulse.encon.Nodes
import io.appulse.encon.config.Defaults
import io.appulse.encon.config.NodeConfig
import io.appulse.encon.config.ServerConfig
import io.appulse.encon.mailbox.Mailbox
import java.io.Closeable
import java.net.InetAddress
import java.util.concurrent.ArrayBlockingQueue

private const val REX_MAILBOX = "rex"

class EnconKerlNode(
    private val config: KerlServerConfig
) : KerlNode, Closeable {
    private var node: Node? = null

    override fun start() {
        synchronized(this) {
            node = Nodes.singleNode(
                config.node.name,
                nodeConfig()
            )
        }
    }

    override fun close() {
        stop()
    }

    override fun stop() {
        synchronized(this) {
            node?.close()
            node = null
        }
    }

    override fun mailbox(): Mailbox {
        return requireNotNull(node) { "Node isn't started yet" }.let {
            it.mailbox(REX_MAILBOX)
                ?: it.mailbox()
                    .name(REX_MAILBOX)
                    .queue(ArrayBlockingQueue(1024))
                    .build()
        }
    }

    private fun nodeConfig(): NodeConfig {
        return NodeConfig(
            config.epmd.port,
            InetAddress.getByName(config.epmd.host),
            Defaults.INSTANCE.type,
            config.node.shortName,
            config.node.cookie,
            Defaults.INSTANCE.protocol,
            Defaults.INSTANCE.lowVersion,
            Defaults.INSTANCE.highVersion,
            Defaults.INSTANCE.distributionFlags,
            emptyList(),
            ServerConfig(
                config.server.port,
                config.server.bossThreads,
                config.server.workerThreads,
            ),
            Defaults.INSTANCE.compression,
        )
    }

}
