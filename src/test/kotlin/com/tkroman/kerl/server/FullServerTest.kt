package com.tkroman.kerl.server

import com.tkroman.kerl.executor.SyncRpcCallExecutor
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.parser.AggregateRpcCallParser
import com.tkroman.kerl.receiver.RpcReceiver
import com.tkroman.kerl.registry.MapBasedRpcRegistry
import com.tkroman.kerl.responder.AggregateResponseFactory
import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.node.EnconKerlNode
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.type.ErlangAtom.ATOM_FALSE
import io.appulse.encon.terms.type.ErlangAtom.ATOM_TRUE
import io.appulse.epmd.java.client.EpmdDefaults
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals


internal class FullServerTest {
    @Test
    fun `start-stop`() {
        val epmdConfig = KerlServerConfig.EpmdConfig(EpmdDefaults.PORT, "127.0.0.1", false)
        val serverConfig = KerlServerConfig(
            KerlServerConfig.ServerConfig(9091, 2, 2),
            KerlServerConfig.NodeConfig("node0@127.0.0.1", "cookie", false),
            epmdConfig
        )

        val clientConfig = KerlServerConfig(
            KerlServerConfig.ServerConfig(9092, 2, 2),
            KerlServerConfig.NodeConfig("node1@127.0.0.1", "cookie", false),
            epmdConfig
        )

        val epmdServer = InProcessEpmdServer(epmdConfig)
        val serverNode = EnconKerlNode(serverConfig)
        val receiver = RpcReceiver(
            SyncRpcCallExecutor(
                AggregateRpcCallParser(),
                MapBasedRpcRegistry(mapOf(RpcMethod("foo", "bar") to { t -> atom(!t.asList().first().asBoolean()) })),
                AggregateResponseFactory()
            )
        )
        val clientNode = EnconKerlNode(clientConfig)
        val server = FullServer(epmdServer, serverNode, receiver)
        server.use { s ->
            s.start()
            clientNode.use { c ->
                c.start()
                c.mailbox().call(serverConfig.node.name, "foo", "bar", ATOM_TRUE)
                assertEquals(ATOM_FALSE, c.mailbox().receiveRemoteProcedureResult(10, TimeUnit.SECONDS))
            }
        }
    }
}
