package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import io.appulse.epmd.java.core.model.NodeType
import io.appulse.epmd.java.core.model.Protocol
import io.appulse.epmd.java.core.model.Version
import io.appulse.epmd.java.core.model.request.GetEpmdInfo
import io.appulse.epmd.java.core.model.response.EpmdInfo
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.Test

internal class GetEpmdInfoRequestProcessorTest {
    private val socket = mockk<Socket>()
    private val server = mockk<InProcessEpmdServer>()

    @Test
    fun responds() {
        val processor = GetEpmdInfoRequestProcessor(
            GetEpmdInfo(),
            socket,
            server
        )
        every { server.config.port }.returns(1234)
        every { server.getNodes() }.returns(
            listOf(
                NodeHandle(
                    "node",
                    42,
                    NodeType.R6_ERLANG,
                    Protocol.TCP,
                    Version.R6,
                    Version.R6,
                    1,
                    socket
                )
            )
        )
        val dumpBytes = EpmdInfo(
            1234,
            listOf(
                EpmdInfo.NodeDescription.builder()
                    .name("node")
                    .port(42)
                    .build()
            )
        ).toBytes()
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(dumpBytes) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify { socket.close() }
        verify { socket.getOutputStream().write(dumpBytes) }
        confirmVerified(socket)
    }
}
