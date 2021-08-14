package com.tkroman.kerl.server.epmd.handler

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.Tag
import io.appulse.epmd.java.core.model.request.Kill
import io.appulse.epmd.java.core.model.request.Request
import io.appulse.epmd.java.core.model.response.KillResult
import io.appulse.utils.BytesUtils
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.test.Test

internal class ServerHandlerTest {
    private val socket = mockk<Socket>()
    private val server = mockk<InProcessEpmdServer>()

    @Test
    fun `processor not found`() {
        class FakeRequest : Request {
            override fun getTag(): Tag = Tag.UNKNOWN
            override fun toBytes(): ByteArray = byteArrayOf(1, 2, 3)
        }

        val sh = ServerHandler(socket, server)
        val req = FakeRequest().toBytes()
        val reqWithLength = req.size.toShort()
        val socketInputStream = ByteArrayInputStream(
            BytesUtils.concatenate(
                BytesUtils.toBytes(reqWithLength),
                req
            )
        )
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(9090))
        every { socket.getInputStream() }.returns(socketInputStream)
        sh.run()
        verify { socket.getInputStream() }
        confirmVerified(socket)
    }

    @Test
    fun `happy path`() {
        val sh = ServerHandler(socket, server)
        val req = Kill().toBytes()
        val socketInputStream = ByteArrayInputStream(req)
        every { socket.remoteSocketAddress }.returns(InetSocketAddress(9090))
        every { socket.getInputStream() }.returns(socketInputStream)
        every { server.config.unsafe }.returns(false)
        every { socket.getOutputStream().write(KillResult.NOK.toBytes()) }.returns(Unit)
        every { socket.getOutputStream().flush() }.returns(Unit)
        sh.run()
        verify { socket.remoteSocketAddress }
        verify { socket.getInputStream() }
        verify { socket.getOutputStream().write(KillResult.NOK.toBytes()) }
        verify { socket.getOutputStream().flush() }
        confirmVerified(socket)
    }
}
