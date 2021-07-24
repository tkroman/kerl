package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import io.appulse.epmd.java.core.model.NodeType
import io.appulse.epmd.java.core.model.Protocol
import io.appulse.epmd.java.core.model.Version
import io.appulse.epmd.java.core.model.request.Registration
import io.appulse.epmd.java.core.model.request.Stop
import io.appulse.epmd.java.core.model.response.RegistrationResult
import io.appulse.epmd.java.core.model.response.StopResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.Test

internal class StopRequestProcessorTest {
    private val socket = mockk<Socket>()
    private val server = mockk<InProcessEpmdServer>()

    @Test
    fun `response on non-unsafe server`() {
        val processor = StopRequestProcessor(
            Stop("node"),
            socket,
            server
        )
        every { server.config.unsafe }.returns(false)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(StopResult.NOEXIST.toBytes()) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 1) { socket.close() }
        verify { socket.getOutputStream().write(StopResult.NOEXIST.toBytes()) }
        confirmVerified(socket)
    }

    @Test
    fun `response on unsafe server & absent node`() {
        val processor = StopRequestProcessor(
            Stop("node"),
            socket,
            server
        )

        every { server.removeNode("node") }.returns(null)
        every { server.config.unsafe }.returns(true)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(StopResult.NOEXIST.toBytes()) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 1) { socket.close() }
        verify { socket.getOutputStream().write(StopResult.NOEXIST.toBytes()) }
        confirmVerified(socket)
    }

    @Test
    fun `response on unsafe server & present node`() {
        val processor = StopRequestProcessor(
            Stop("node"),
            socket,
            server
        )

        val nodeSocket = mockk<Socket>()
        every { server.removeNode("node") }.returns(NodeHandle("node", 1111, NodeType.R6_ERLANG, Protocol.TCP, Version.R6, Version.R6, 1, nodeSocket))
        every { server.config.unsafe }.returns(true)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.close() }.returns(Unit)
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(StopResult.STOPPED.toBytes()) }.returns(Unit)
        every { nodeSocket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 1) { socket.close() }
        verify(exactly = 1) { nodeSocket.close() }
        verify { socket.getOutputStream().write(StopResult.STOPPED.toBytes()) }
        confirmVerified(socket, nodeSocket)
    }
}
