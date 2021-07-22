package com.tkroman.kerl

import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.bstring
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangTuple

internal val BADRPC = atom("badrpc")
internal val GEN_CALL = atom("\$gen_call")
internal val CALL = atom("call")
internal val REX = atom("rex")

internal fun ErlangTerm.eget(i: Int): ErlangTerm? {
    if (!(isList || isTuple)) {
        return null
    }
    if (i < 0 || i > size()) {
        return null
    }
    return getUnsafe(i)
}

internal fun badrpc(why: String): ErlangTuple {
    return tuple(BADRPC, bstring(why))
}
