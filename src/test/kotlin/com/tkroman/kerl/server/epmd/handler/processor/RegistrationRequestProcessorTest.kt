package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.NodeType
import io.appulse.epmd.java.core.model.Protocol
import io.appulse.epmd.java.core.model.Version
import io.appulse.epmd.java.core.model.request.Registration
import io.appulse.epmd.java.core.model.response.RegistrationResult
import io.appulse.epmd.java.core.model.response.Response
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.Test

internal class RegistrationRequestProcessorTest {
    private val socket = mockk<Socket>()
    private val server = mockk<InProcessEpmdServer>()

    @Test
    fun `response on successful node registration`() {
        val processor = RegistrationRequestProcessor(
            Registration(1010, NodeType.R6_ERLANG, Protocol.TCP, Version.R6, Version.R6, "new", byteArrayOf()),
            socket,
            server
        )
        every { server.config.port }.returns(1234)
        every {
            server.addNode(
                match {
                    it.name == "new" &&
                            it.port == 1010 &&
                            it.type == NodeType.R6_ERLANG &&
                            it.protocol == Protocol.TCP &&
                            it.high == Version.R6 &&
                            it.low == Version.R6 &&
                            it.creation in (1 .. 3)
                }
            )
        }.returns(true)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every {
            socket
                .getOutputStream()
                .write(
                    match<ByteArray> {
                        Response.parse(it, RegistrationResult::class.java).let {
                            it.isOk && it.creation in (1 .. 3)
                        }
                    }
                )
        }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 0) { socket.close() }
        verify {
            socket.getOutputStream().write(
                match<ByteArray> {
                    Response.parse(it, RegistrationResult::class.java).let {
                        it.isOk && it.creation in (1 .. 3)
                    }
                }
            )
        }
        confirmVerified(socket)
    }

    @Test
    fun `response on failed node registration`() {
        val processor = RegistrationRequestProcessor(
            Registration(1010, NodeType.R6_ERLANG, Protocol.TCP, Version.R6, Version.R6, "new", byteArrayOf()),
            socket,
            server
        )
        every { server.config.port }.returns(1234)
        every {
            server.addNode(
                match {
                    it.name == "new" &&
                            it.port == 1010 &&
                            it.type == NodeType.R6_ERLANG &&
                            it.protocol == Protocol.TCP &&
                            it.high == Version.R6 &&
                            it.low == Version.R6 &&
                            it.creation in (1 .. 3)
                }
            )
        }.returns(false)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        val response = RegistrationResult(false, 0).toBytes()
        every { socket.getOutputStream().write(response) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 1) { socket.close() }
        verify { socket.getOutputStream().write(response) }
        confirmVerified(socket)
    }
}
