package com.tkroman.kerl.server.epmd

import com.tkroman.kerl.server.KerlServerConfig
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class InProcessEpmdServerTest {
    @Test
    fun `start-stop`() {
        val port = 9090
        val host = "127.0.0.1"
        val config = KerlServerConfig.EpmdConfig(
            port,
            host,
            false
        )
        InProcessEpmdServer(config).use { server -> server.start() }
    }

    @Test
    fun `node add ok`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            val node = mockk<NodeHandle>()
            every { node.isAlive }.returns(true)
            every { node.name }.returns("name")
            server.addNode(node)
            assertNotNull(server.getNode("name"))
            verify { node.isAlive }
            verify { node.name }
            confirmVerified(node)
        }
    }

    @Test
    fun `node get for present node`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            val node = mockk<NodeHandle>()
            every { node.isAlive }.returns(true)
            every { node.name }.returns("node0")
            server.addNode(node)
            assertEquals(node, server.getNode("node0"))
            assertEquals(listOf(node), server.getNodes())
            verify { node.isAlive }
            verify { node.name }
            verify { node.equals(any()) }
            confirmVerified(node)
        }
    }

    @Test
    fun `node get for present node which isn't alive`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            val node = mockk<NodeHandle>()
            every { node.isAlive }.returns(false)
            every { node.name }.returns("node0")
            server.addNode(node)
            assertNull(server.getNode("node0"))
            assertTrue(server.getNodes().isEmpty())
            verify { node.isAlive }
            verify { node.name }
            confirmVerified(node)
        }
    }

    @Test
    fun `node get for absent node`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            assertNull(server.getNode("node0"))
            assertTrue(server.getNodes().isEmpty())
        }
    }


    @Test
    fun `node remove for absent node`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            assertNull(server.removeNode("node0"))
        }
    }

    @Test
    fun `node remove for present node`() {
        InProcessEpmdServer(KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)).use { server ->
            server.start()
            val node = mockk<NodeHandle>()
            every { node.isAlive }.returns(true)
            every { node.name }.returns("node0")
            server.addNode(node)
            assertEquals(node, server.removeNode("node0"))
            assertNull(server.getNode("node0"))
            assertTrue(server.getNodes().isEmpty())
            verify { node.isAlive }
            verify { node.name }
            verify { node.equals(any()) }
            confirmVerified(node)
        }
    }
}
