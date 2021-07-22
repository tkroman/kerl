package com.tkroman.kerl.parser

import com.tkroman.kerl.GEN_CALL
import com.tkroman.kerl.eget
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.ErlangTerm

class AggregateRpcCallParser : RpcCallParser {
    private val standaloneCallParser = TypeAwareCallBodyParser()
    private val genCallParser = GenCallParser(standaloneCallParser)
    private val rexCallParser = RexCallParser(standaloneCallParser)

    override fun parse(body: ErlangTerm): RpcCall {
        if (!body.isTuple) {
            return InvalidRpcCall(Unknown, "body is not a tuple")
        }
        val firstElement = body.eget(0) ?: return InvalidRpcCall(Unknown, "body is an empty tuple")
        return when {
            firstElement == GEN_CALL -> genCallParser.parse(body)
            firstElement.isPid -> rexCallParser.parse(body)
            else -> InvalidRpcCall(Unknown, "unknown call type")
        }
    }
}

