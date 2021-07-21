package com.tkroman.kerl.model

import io.appulse.encon.terms.ErlangTerm

sealed interface RpcCall {
    val sender: RpcSender
}

data class ValidRpcCall(
    override val sender: RpcSender,
    val args: ErlangTerm,
    val method: RpcMethod,
) : RpcCall

data class InvalidRpcCall(
    val reason: String,
    override val sender: RpcSender,
) : RpcCall
