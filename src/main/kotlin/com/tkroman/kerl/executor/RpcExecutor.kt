package com.tkroman.kerl.executor

import com.tkroman.kerl.badrpc
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.ValidRpcCall
import com.tkroman.kerl.parser.RpcCallParser
import com.tkroman.kerl.registry.RpcRegistry
import com.tkroman.kerl.responder.RpcResponseFactory
import io.appulse.encon.mailbox.Mailbox
import io.appulse.encon.terms.ErlangTerm
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

class RpcExecutor(
    private val registry: RpcRegistry,
    private val parser: RpcCallParser,
    private val responseFactory: RpcResponseFactory,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newWorkStealingPool()

    fun executeAsync(body: ErlangTerm, origin: Mailbox) {
        executor.execute {
            val call = parser.parse(body)
            val result = when (call) {
                is ValidRpcCall -> {
                    val logic = registry[call.method]
                    val result = logic(call.args)
                    responseFactory.constructReply(call.sender, result)
                }
                is InvalidRpcCall -> {
                    responseFactory.constructReply(
                        call.sender,
                        badrpc("${call.reason}: ${call.sender}")
                    )
                }
            }
            if (result == null) {
                logger.error("Don't know how to reply to $body (parsed call: $call)")
                return@execute
            } else {
                origin.send(result.first, result.second)
            }
        }
    }
}
