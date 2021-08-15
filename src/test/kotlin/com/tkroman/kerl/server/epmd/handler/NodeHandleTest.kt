package com.tkroman.kerl.server.epmd.handler

import io.appulse.epmd.java.core.model.NodeType
import io.appulse.epmd.java.core.model.Protocol
import io.appulse.epmd.java.core.model.Version
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NodeHandleTest {
    @Test
    fun `closes socket happily`() {
        val socket = mockk<Socket>()
        val nodeHandle = NodeHandle(
            "node",
            9090,
            NodeType.R6_ERLANG,
            Protocol.TCP,
            Version.UNKNOWN,
            Version.UNKNOWN,
            0,
            socket
        )
        every { socket.close() }.returns(Unit)
        nodeHandle.close()
        verify { socket.close() }
        confirmVerified(socket)
    }

    @Test
    fun `ignores socket close exceptions`() {
        val socket = mockk<Socket>()
        val nodeHandle = NodeHandle(
            "node",
            9090,
            NodeType.R6_ERLANG,
            Protocol.TCP,
            Version.UNKNOWN,
            Version.UNKNOWN,
            0,
            socket
        )
        every { socket.close() }.throws(IOException("oops"))
        nodeHandle.close()
        verify { socket.close() }
        confirmVerified(socket)
    }

    @Test
    fun `liveness check ok`() {
        ServerSocket(9090).use {
            Thread { it.accept() }.also { it.isDaemon = true }.start()
            val socket = mockk<Socket>()
            val nodeHandle = NodeHandle(
                "node",
                9090,
                NodeType.R6_ERLANG,
                Protocol.TCP,
                Version.UNKNOWN,
                Version.UNKNOWN,
                0,
                socket
            )
            every { socket.inetAddress }.returns(InetAddress.getLocalHost())
            assertTrue(nodeHandle.isAlive)
            verify { socket.inetAddress }
            confirmVerified(socket)
        }
    }

    @Test
    fun `liveness check fails`() {
        ServerSocket(9090).use {
            val socket = mockk<Socket>()
            val nodeHandle = NodeHandle(
                "node",
                9091,
                NodeType.R6_ERLANG,
                Protocol.TCP,
                Version.UNKNOWN,
                Version.UNKNOWN,
                0,
                socket
            )
            every { socket.inetAddress }.returns(InetAddress.getLocalHost())
            assertFalse(nodeHandle.isAlive)
            verify { socket.inetAddress }
            confirmVerified(socket)
        }
    }
}
