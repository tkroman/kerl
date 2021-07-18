package com.tkroman.kerl

import io.appulse.encon.terms.ErlangTerm

interface RpcRegistry {
    operator fun get(rpcMethod: RpcMethod): (ErlangTerm) -> ErlangTerm
}
