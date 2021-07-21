package com.tkroman.kerl.registry

import com.tkroman.kerl.model.RpcMethod
import io.appulse.encon.terms.ErlangTerm

interface RpcRegistry {
    operator fun get(rpcMethod: RpcMethod): (ErlangTerm) -> ErlangTerm
}
