package com.tkroman.kerl.model

import io.appulse.encon.terms.type.ErlangList

sealed interface RpcCall {
    val callType: RpcCallType
}

data class ValidRpcCall(
    override val callType: RpcCallType,
    val method: RpcMethod,
    val args: ErlangList,
) : RpcCall

data class InvalidRpcCall(
    override val callType: RpcCallType,
    val reason: String,
) : RpcCall
