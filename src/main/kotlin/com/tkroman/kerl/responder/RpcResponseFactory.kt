package com.tkroman.kerl.responder

import com.tkroman.kerl.model.RpcCallType
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid

interface RpcResponseFactory {
    fun constructReply(sender: RpcCallType, result: ErlangTerm): Pair<ErlangPid, ErlangTerm>?
}
