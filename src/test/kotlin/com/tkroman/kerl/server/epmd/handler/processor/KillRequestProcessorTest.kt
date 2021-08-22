package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import io.appulse.epmd.java.core.model.request.Kill
import io.appulse.epmd.java.core.model.response.KillResult
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import kotlin.test.Test


internal class KillRequestProcessorTest {
    private val socket = mockk<Socket>()
    private val server = mockk<InProcessEpmdServer>()

    @Test
    fun `responds with NOK if not running in unsafe mode`() {
        val processor = KillRequestProcessor(
            Kill(),
            socket,
            server
        )
        every { server.config.unsafe }.returns(false)
        val dumpBytes = KillResult.NOK.toBytes()
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(dumpBytes) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify(exactly = 0) { socket.close() }
        verify { socket.getOutputStream().write(dumpBytes) }
        confirmVerified(socket)
    }

    @Test
    fun `responds with OK if running in unsafe mode`() {
        val processor = KillRequestProcessor(
            Kill(),
            socket,
            server
        )
        val executor = mockk<ExecutorService>()
        every { server.epmdExecutor }.returns(executor)
        every { executor.shutdownNow() }.returns(emptyList())
        every { server.config.unsafe }.returns(true)
        val n1 = mockk<NodeHandle>()
        every { n1.close() }.returns(Unit)
        val n2 = mockk<NodeHandle>()
        every { n2.close() }.returns(Unit)
        every { server.getNodes() }.returns(listOf(n1, n2))
        val dumpBytes = KillResult.OK.toBytes()
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(999))
        every { socket.getOutputStream().flush() }.returns(Unit)
        every { socket.getOutputStream().write(dumpBytes) }.returns(Unit)
        every { socket.close() }.returns(Unit)
        processor.process()
        verify { socket.remoteSocketAddress }
        verify { socket.getOutputStream().write(dumpBytes) }
        verify { n1.close() }
        verify { n2.close() }
        verify { executor.shutdownNow() }
        confirmVerified(socket, n1, n2, executor)
    }
}
