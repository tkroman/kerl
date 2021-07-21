package com.tkroman.kerl.responder

import com.tkroman.kerl.model.RpcSender
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid

interface RpcResponseFactory {
    fun constructReply(sender: RpcSender, result: ErlangTerm): Pair<ErlangPid, ErlangTerm>?
}
