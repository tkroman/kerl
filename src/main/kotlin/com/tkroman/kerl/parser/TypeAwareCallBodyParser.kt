package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.eget
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.RpcCallType
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.ErlangTerm

class TypeAwareCallBodyParser {
    fun parse(call: ErlangTerm, callType: RpcCallType): RpcCall {
        if (callType == Unknown) {
            return InvalidRpcCall(callType, "unknown call type")
        }
        if (call.eget(0) != CALL) {
            return InvalidRpcCall(callType, "invalid call section")
        }
        val module = call.eget(1)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return InvalidRpcCall(callType, "no module")

        val function = call.eget(2)?.asAtom()?.asText()?.takeIf { it.isNotBlank() }
            ?: return InvalidRpcCall(callType, "no function")

        val args = call.eget(3)
        return when {
            args == null -> ValidRpcCall(callType, RpcMethod(module, function), list())
            args == NIL -> ValidRpcCall(callType, RpcMethod(module, function), list())
            args.isList -> ValidRpcCall(callType, RpcMethod(module, function), args.asList())
            else -> InvalidRpcCall(callType, "invalid arguments")
        }
    }
}
