package com.tkroman.kerl.responder

import com.tkroman.kerl.REX
import com.tkroman.kerl.model.GenCallSender
import com.tkroman.kerl.model.RexSender
import com.tkroman.kerl.model.RpcSender
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.Erlang
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid

class GenCallOrRexResponseFactory : RpcResponseFactory {
    override fun constructReply(
        sender: RpcSender,
        result: ErlangTerm
    ): Pair<ErlangPid, ErlangTerm>? {
        return when (sender) {
            // no way to respond with certainty
            // (e.g. gen_call requires a reference and we weren't able to get to it)
            Unknown -> null

            is GenCallSender ->
                sender.pid to Erlang.tuple(sender.ref, result)

            is RexSender ->
                sender.pid to Erlang.tuple(REX, result)
        }
    }
}
