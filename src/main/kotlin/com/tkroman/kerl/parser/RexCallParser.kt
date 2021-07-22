package com.tkroman.kerl.parser

import com.tkroman.kerl.eget
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RexCallType
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.ErlangTerm

class RexCallParser(
    private val typeAwareCallBodyParser: TypeAwareCallBodyParser,
) : RpcCallParser {
    override fun parse(body: ErlangTerm): RpcCall {
        val firstElement = body.eget(0) ?: return InvalidRpcCall(Unknown, "body is an empty tuple")
        if (!firstElement.isPid) {
            return InvalidRpcCall(Unknown, "not a rex call")
        }
        val sender = RexCallType(firstElement.asPid())
        return body.eget(1)?.asTuple()
            ?.let { typeAwareCallBodyParser.parse(it, sender) }
            ?: InvalidRpcCall(sender, "invalid payload: no call")
    }
}
