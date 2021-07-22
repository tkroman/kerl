package com.tkroman.kerl.responder

import com.tkroman.kerl.REX
import com.tkroman.kerl.model.GenCallCallType
import com.tkroman.kerl.model.RexCallType
import com.tkroman.kerl.model.RpcCallType
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid

class AggregateResponseFactory : RpcResponseFactory {
    override fun constructReply(
        sender: RpcCallType,
        result: ErlangTerm
    ): Pair<ErlangPid, ErlangTerm>? {
        return when (sender) {
            // no way to respond with certainty
            // (e.g. gen_call requires a reference and we weren't able to get to it)
            Unknown -> null

            is GenCallCallType ->
                sender.senderPid to tuple(sender.senderRef, result)

            is RexCallType ->
                sender.senderPid to tuple(REX, result)
        }
    }
}
