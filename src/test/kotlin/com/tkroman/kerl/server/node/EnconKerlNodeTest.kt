package com.tkroman.kerl.server.node

import com.tkroman.kerl.server.KerlServerConfig
import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import kotlin.test.Test


internal class EnconKerlNodeTest {
    @Test
    fun `start happy path`() {
        val epmdConfig = KerlServerConfig.EpmdConfig(9090, "127.0.0.1", false)
        val node = EnconKerlNode(
            KerlServerConfig(
                KerlServerConfig.ServerConfig(9091, 2, 2),
                KerlServerConfig.NodeConfig("node0@127.0.0.1", "cookie", false),
                epmdConfig
            )
        )
        val epmd = InProcessEpmdServer(epmdConfig)
        epmd.start()
        node.start()
    }
}
