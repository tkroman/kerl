package com.tkroman.kerl

import com.tkroman.kerl.model.GenCallCallType
import com.tkroman.kerl.model.RexCallType
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.TermType
import io.appulse.encon.terms.type.ErlangPid
import io.appulse.encon.terms.type.ErlangReference
import io.appulse.encon.terms.type.ErlangTuple

internal val SENDER_PID = ErlangPid.builder()
    .creation(1)
    .id(2)
    .node("sender-node")
    .serial(3)
    .type(TermType.NEW_PID)
    .build()
internal val RECEIVER_MAILBOX = atom("rex")
internal val SENDER_REF = ErlangReference.builder()
    .creation(1)
    .id(2)
    .node("node")
    .type(TermType.NEWER_REFERENCE)
    .build()
internal val GEN_CALL_TYPE = GenCallCallType(SENDER_PID, SENDER_REF)
internal val REX_CALL_TYPE = RexCallType(SENDER_PID)

internal fun validGenCallCall(vararg args: ErlangTerm): ErlangTuple {
    return tuple(
        GEN_CALL,
        tuple(
            SENDER_PID,
            SENDER_REF
        ),
        tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            list(*args)
        )
    )
}

internal fun validRexCall(vararg args: ErlangTerm): ErlangTuple {
    return tuple(
        SENDER_PID,
        tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            list(*args)
        )
    )
}
