package com.tkroman.kerl.executor

import com.tkroman.kerl.badrpc
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.ValidRpcCall
import com.tkroman.kerl.parser.RpcCallParser
import com.tkroman.kerl.registry.RpcRegistry
import com.tkroman.kerl.responder.RpcResponseFactory
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid
import java.util.concurrent.CompletableFuture

class SyncRpcCallExecutor(
    private val parser: RpcCallParser,
    private val registry: RpcRegistry,
    private val responseFactory: RpcResponseFactory,
) : RpcExecutor {
    override fun execute(body: ErlangTerm): CompletableFuture<Pair<ErlangPid, ErlangTerm>?> {
        return when (val call = parser.parse(body)) {
            is ValidRpcCall -> {
                val logic = registry[call.method]
                val result = logic(call.args)
                CompletableFuture.completedFuture(
                    responseFactory.constructReply(
                        call.callType,
                        result
                    )
                )
            }
            is InvalidRpcCall -> {
                CompletableFuture.completedFuture(
                    responseFactory.constructReply(
                        call.callType,
                        badrpc("${call.reason}: ${call.callType}")
                    )
                )
            }
        }
    }
}
