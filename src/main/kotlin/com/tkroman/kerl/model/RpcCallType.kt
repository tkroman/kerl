package com.tkroman.kerl.model

import io.appulse.encon.terms.type.ErlangPid
import io.appulse.encon.terms.type.ErlangReference

sealed interface RpcCallType
sealed interface Known : RpcCallType
data class GenCallCallType(val senderPid: ErlangPid, val senderRef: ErlangReference) : Known
data class RexCallType(val senderPid: ErlangPid) : Known
object Unknown : RpcCallType
