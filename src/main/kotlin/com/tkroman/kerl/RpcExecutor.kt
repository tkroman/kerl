package com.tkroman.kerl

import io.appulse.encon.mailbox.Mailbox
import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangTuple
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool

class RpcExecutor(
    private val registry: RpcRegistry
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newWorkStealingPool()

    fun executeAsync(body: ErlangTerm, mb: Mailbox) {
        executor.execute {
            ForkJoinPool.managedBlock(object : ForkJoinPool.ManagedBlocker {
                var blocking = false
                override fun block(): Boolean {
                    when (val call = readBody(body)) {
                        is Rex -> {
                            val logic = registry[call.method]
                            val handled = blockingLogic(logic, call)
                            mb.send(call.sender, tuple(REX, handled))
                        }
                        is GenCall -> {
                            val logic = registry[call.method]
                            val handled = blockingLogic(logic, call)
                            mb.send(call.sender, tuple(call.ref, handled))
                        }
                        is EarlyGenCallBadrpc ->
                            mb.send(call.sender, tuple(call.ref, badrpc(call.reason)))
                        is EarlyRexBadrpc ->
                            mb.send(call.sender, tuple(REX, badrpc(call.reason)))
                        is Broken ->
                            // no way to respond with certainty
                            // (e.g. gen_call requires a reference and we weren't able to get to it)
                            logger.error("Don't know how to reply to $body")
                    }
                    return !blocking
                }

                private inline fun blockingLogic(
                    logic: (ErlangTerm) -> ErlangTerm,
                    call: RpcCall
                ): ErlangTerm {
                    blocking = true
                    return try {
                        logic(call.args)
                    } finally {
                        blocking = false
                    }
                }

                override fun isReleasable(): Boolean {
                    return !blocking
                }
            })
        }
    }

    private fun readBody(body: ErlangTerm): RpcCall {
        if (!body.isTuple) {
            return Broken("body is not a tuple")
        }
        // [:$gen_call, [#PID<1626610823.111.0>, [:alias]], [:call, :Elixir.Foo, :bar, [:true], #PID<1626610823.71.0>]]
        val firstElement = body.eget(0) ?: return Broken("body is an empty tuple")
        if (firstElement == GEN_CALL) {
            val secondElement = body.eget(1) ?: return Broken("invalid gen_call payload: no pid-ref pair")
            val pid = secondElement.eget(0)?.asPid() ?: return Broken("invalid gen_call payload: no pid")
            val ref = secondElement.eget(1)?.asReference() ?: return Broken("invalid gen_call payload: no ref")
            val thirdElement = body.eget(2)?.asTuple() ?: return EarlyGenCallBadrpc(
                "invalid gen_call payload: no :call",
                pid,
                ref,
            )
            return parseCall(thirdElement, EarlyGenCall(pid, ref))
        } else if (firstElement.isPid) {
            val pid = firstElement.asPid()
            val secondElement = body.eget(1)?.asTuple() ?: return EarlyRexBadrpc("invalid rex payload: no :call", pid)
            return parseCall(secondElement, EarlyRex(pid))
        } else {
            return Broken("unknown call type: $body")
        }
    }

    private fun parseCall(
        call: ErlangTuple,
        early: Early
    ): RpcCall {
        if (call.eget(0) != CALL) {
            return when (early) {
                is EarlyGenCall -> EarlyGenCallBadrpc(
                    "invalid gen_call payload: invalid call section",
                    early.pid,
                    early.ref
                )
                is EarlyRex -> EarlyRexBadrpc("invalid rex payload: invalid call section", early.pid)
            }
        }
        val module = call.eget(1)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return when (early) {
                is EarlyGenCall -> EarlyGenCallBadrpc(
                    "invalid gen_call payload: no module",
                    early.pid,
                    early.ref
                )
                is EarlyRex -> EarlyRexBadrpc("invalid rex payload: no module", early.pid)
            }

        val function = call.eget(2)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return when (early) {
                is EarlyGenCall -> EarlyGenCallBadrpc(
                    "invalid gen_call payload: no function",
                    early.pid,
                    early.ref
                )
                is EarlyRex -> EarlyRexBadrpc("invalid rex payload: no function", early.pid)
            }

        val args = call.eget(2)?.asList() ?: NIL

        return when (early) {
            is EarlyGenCall -> GenCall(early.pid, early.ref, args, RpcMethod(module, function))
            is EarlyRex -> Rex(early.pid, args, RpcMethod(module, function))
        }
    }
}

