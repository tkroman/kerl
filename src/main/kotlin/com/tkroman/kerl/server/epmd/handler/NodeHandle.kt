package com.tkroman.kerl.server.epmd.handler

import io.appulse.epmd.java.core.model.NodeType
import io.appulse.epmd.java.core.model.Protocol
import io.appulse.epmd.java.core.model.Version
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal class NodeHandle(
    val name: String,
    val port: Int,
    val type: NodeType,
    val protocol: Protocol,
    val high: Version,
    val low: Version,
    val creation: Int,
    val socket: Socket
) : Closeable {
    override fun close() {
        try {
            socket.close()
        } catch (ex: IOException) {
            // noop
        }
    }

    val isAlive: Boolean
        get() {
            val nodeSocketAddress = InetSocketAddress(socket.inetAddress, port)
            try {
                Socket().use { nodeSocket -> nodeSocket.connect(nodeSocketAddress, 2000) }
            } catch (ex: Exception) {
                return false
            }
            return true
        }
}
