package com.tkroman.kerl.model

import io.appulse.encon.terms.type.ErlangPid
import io.appulse.encon.terms.type.ErlangReference

sealed interface RpcSender
sealed interface Known : RpcSender
data class GenCallSender(val pid: ErlangPid, val ref: ErlangReference) : Known
data class RexSender(val pid: ErlangPid) : Known
object Unknown : RpcSender
