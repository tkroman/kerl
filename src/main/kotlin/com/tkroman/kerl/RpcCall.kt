package com.tkroman.kerl

import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid
import io.appulse.encon.terms.type.ErlangReference

sealed interface Early
data class EarlyRex(val pid: ErlangPid) : Early
data class EarlyGenCall(val pid: ErlangPid, val ref: ErlangReference) : Early

sealed interface RpcCall {
    val sender: ErlangPid
    val args: ErlangTerm
    val method: RpcMethod
}

data class Broken(val reason: String) : RpcCall {
    override val sender: ErlangPid = ZERO_PID
    override val args: ErlangTerm = NIL
    override val method: RpcMethod = UNDEFINED_RPC_METHOD
}

data class EarlyGenCallBadrpc(
    val reason: String,
    override val sender: ErlangPid,
    val ref: ErlangReference,
): RpcCall {
    override val args: ErlangTerm = NIL
    override val method: RpcMethod = UNDEFINED_RPC_METHOD
}

data class EarlyRexBadrpc(
    val reason: String,
    override val sender: ErlangPid,
): RpcCall {
    override val args: ErlangTerm = NIL
    override val method: RpcMethod = UNDEFINED_RPC_METHOD
}

data class GenCall(
    override val sender: ErlangPid,
    val ref: ErlangReference,
    override val args: ErlangTerm,
    override val method: RpcMethod,
) : RpcCall

data class Rex(
    override val sender: ErlangPid,
    override val args: ErlangTerm,
    override val method: RpcMethod,
) : RpcCall

data class RpcMethod(val module: String, val function: String)
