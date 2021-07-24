package com.tkroman.kerl.server.epmd

import com.tkroman.kerl.server.KerlServerConfig
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import com.tkroman.kerl.server.epmd.handler.ServerHandler
import io.appulse.utils.threads.AppulseExecutors
import io.appulse.utils.threads.AppulseThreadFactory
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private val ANY_ADDRESS = InetAddress.getByName("0.0.0.0")
private val LOOPBACK_ADDRESS = InetAddress.getLoopbackAddress()
private val LOCALHOST = InetAddress.getLocalHost()

class InProcessEpmdServer(
    internal val config: KerlServerConfig.EpmdConfig,
) : EpmdServer {
    private val logger = LoggerFactory.getLogger(InProcessEpmdServer::class.java)
    private val ip = InetAddress.getByName(config.host)
    private val nodes = ConcurrentHashMap<String, NodeHandle>()
    internal val epmdExecutor = AppulseExecutors.newCachedThreadPool()
        .threadFactory(AppulseThreadFactory.builder().name("epmd-%d").build())
        .corePoolSize(2)
        .maxPoolSize(8)
        .keepAliveTime(100L)
        .unit(TimeUnit.MILLISECONDS)
        .queueLimit(1000)
        .build()

    private var serverThread: Thread? = null

    override fun start() {
        synchronized(this) {
            serverThread = Thread({ runEpmd() }, "epmd-runner").also { it.start() }
        }
    }

    override fun stop() {
        synchronized(this) {
            serverThread?.interrupt()
            serverThread = null
            epmdExecutor.shutdown()
            val terminated = epmdExecutor.awaitTermination(5, TimeUnit.SECONDS)
            logger.info("EPMD server terminated successfully: $terminated")
        }
    }

    private fun runEpmd() {
        ServerSocket(config.port, 1024).use { serverSocket ->
            logger.info("EPMD server started (config: $config)")
            while (serverThread?.isInterrupted != true) {
                val clientSocket = serverSocket.accept()
                val remoteAddress = (clientSocket.remoteSocketAddress as? InetSocketAddress)?.address
                if (remoteAddress == null) {
                    logger.warn("Remote address is null, ignore")
                    continue
                } else if (remoteAddress.allowed()) {
                    clientSocket.close()
                    logger.warn("Unacceptable remote client's address $remoteAddress")
                    continue
                }
                logger.debug("$remoteAddress - a new incoming connection")
                epmdExecutor.execute(ServerHandler(clientSocket, this))
            }
        }
    }

    private fun InetAddress.allowed() =
        ip != ANY_ADDRESS && this != ip && this != LOOPBACK_ADDRESS && this != LOCALHOST

    internal fun getNodes(): Iterable<NodeHandle> {
        nodes.entries.removeIf { (_, value) -> !value.isAlive }
        return nodes.values
    }

    internal fun getNode(name: String): NodeHandle? {
        return nodes[name]?.takeIf { it.isAlive }
    }

    internal fun addNode(node: NodeHandle): Boolean {
        return if (node.isAlive) {
            nodes.computeIfAbsent(node.name) { node }
            true
        } else {
            false
        }
    }

    internal fun removeNode(name: String): NodeHandle? {
        return nodes.remove(name)
    }
}
