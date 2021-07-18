package com.tkroman.kerl

import io.appulse.encon.terms.ErlangTerm

class MapBasedRpcRegistry(
    private val calls: Map<RpcMethod, (ErlangTerm) -> ErlangTerm>
) : RpcRegistry {
    override operator fun get(rpcMethod: RpcMethod): (ErlangTerm) -> ErlangTerm {
        return calls[rpcMethod] ?: { badrpc("no handler for $rpcMethod") }
    }
}
