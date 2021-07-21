package com.tkroman.kerl.parser

import com.tkroman.kerl.model.RpcCall
import io.appulse.encon.terms.ErlangTerm

interface RpcCallParser {
    fun parse(body: ErlangTerm): RpcCall
}
