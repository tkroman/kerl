package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.GEN_CALL
import com.tkroman.kerl.eget
import com.tkroman.kerl.model.GenCallSender
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RexSender
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.RpcSender
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangTuple

class GenCallOrRexRpcCallParser : RpcCallParser {
    override fun parse(body: ErlangTerm): RpcCall {
        if (!body.isTuple) {
            return InvalidRpcCall("body is not a tuple", Unknown)
        }
        // [:$gen_call, [#PID<1626610823.111.0>, [:alias]], [:call, :Elixir.Foo, :bar, [:true], #PID<1626610823.71.0>]]
        val firstElement = body.eget(0) ?: return InvalidRpcCall("body is an empty tuple", Unknown)
        return when {
            firstElement == GEN_CALL -> parseGenCall(body)
            firstElement.isPid -> parseRexCall(firstElement, body)
            else -> InvalidRpcCall("unknown call type: $body", Unknown)
        }
    }

    private fun parseRexCall(
        firstElement: ErlangTerm,
        body: ErlangTerm
    ): RpcCall {
        val sender = RexSender(firstElement.asPid())
        return body.eget(1)?.asTuple()
            ?.let { parseCall(it, sender) }
            ?: InvalidRpcCall("invalid payload: no :call", sender)
    }

    private fun parseGenCall(body: ErlangTerm): RpcCall {
        val secondElement = body.eget(1) ?: return InvalidRpcCall("invalid gen_call payload: no pid-ref pair", Unknown)
        val pid = secondElement.eget(0)?.asPid() ?: return InvalidRpcCall("invalid payload: no pid", Unknown)
        val ref = secondElement.eget(1)?.asReference() ?: return InvalidRpcCall("invalid payload: no ref", Unknown)
        val sender = GenCallSender(pid, ref)
        return body.eget(2)?.asTuple()
            ?.let { parseCall(it, sender) }
            ?: return InvalidRpcCall("invalid payload: no :call", sender)
    }

    private fun parseCall(
        call: ErlangTuple,
        sender: RpcSender
    ): RpcCall {
        if (sender == Unknown) {
            return InvalidRpcCall("Unknown sender", sender)
        }

        if (call.eget(0) != CALL) {
            return InvalidRpcCall("invalid call section", sender)
        }
        val module = call.eget(1)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return InvalidRpcCall("no module", sender)

        val function = call.eget(2)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return InvalidRpcCall("no function", sender)

        val args = call.eget(2)?.asList() ?: Erlang.NIL

        return ValidRpcCall(sender, args, RpcMethod(module, function))
    }
}

