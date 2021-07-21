package com.tkroman.kerl

import com.tkroman.kerl.model.RpcMethod
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.bstring
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.TermType
import io.appulse.encon.terms.type.ErlangPid
import io.appulse.encon.terms.type.ErlangTuple

internal val BADRPC = atom("badrpc")
internal val GEN_CALL = atom("\$gen_call")
internal val CALL = atom("call")
internal val REX = atom("rex")

internal val UNDEFINED_RPC_METHOD = RpcMethod("<n/a>", "<n/a>")

internal fun ErlangTerm.eget(i: Int): ErlangTerm? {
    return if (i > 0 && i < size()) {
        getUnsafe(i)
    } else {
        null
    }
}

internal fun badrpc(why: String): ErlangTuple {
    return tuple(BADRPC, bstring(why))
}

internal fun ErlangTerm.badrpc(): Boolean {
    return isTuple && size() >= 1 && getUnsafe(0) == BADRPC
}

internal val ZERO_PID = ErlangPid.builder()
    .type(TermType.NEW_PID)
    .id(0)
    .serial(0)
    .creation(0)
    .node("")
    .build()
