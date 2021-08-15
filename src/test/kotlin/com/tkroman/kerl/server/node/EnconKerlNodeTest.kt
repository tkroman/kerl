package com.tkroman.kerl.server.node

import com.tkroman.kerl.server.KerlServerConfig
import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import kotlin.test.Test
import kotlin.test.assertNotNull


internal class EnconKerlNodeTest {
    @Test
    fun `node methods`() {
        val epmdConfig = KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)
        val serverConfig = KerlServerConfig(
            KerlServerConfig.ServerConfig(9091, 2, 2),
            KerlServerConfig.NodeConfig("node0@127.0.0.1", "cookie", false),
            epmdConfig
        )
        InProcessEpmdServer(epmdConfig).use {
            it.start()
            EnconKerlNode(serverConfig).use {
                it.start()
                assertNotNull(it.mailbox()).close()
            }
        }
    }
}
