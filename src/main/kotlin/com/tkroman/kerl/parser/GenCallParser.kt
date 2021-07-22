package com.tkroman.kerl.parser

import com.tkroman.kerl.GEN_CALL
import com.tkroman.kerl.eget
import com.tkroman.kerl.model.GenCallCallType
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.ErlangTerm

class GenCallParser(
    private val typeAwareCallBodyParser: TypeAwareCallBodyParser,
) : RpcCallParser {
    // [:$gen_call, [#PID<1626610823.111.0>, [:alias]], [:call, :Elixir.Foo, :bar, [:true], #PID<1626610823.71.0>]]
    override fun parse(body: ErlangTerm): RpcCall {
        if (body.eget(0) != GEN_CALL) {
            return InvalidRpcCall(Unknown, "not a gen_call")
        }
        val pidRef = body.eget(1) ?: return InvalidRpcCall(
            Unknown,
            "invalid payload: no pid-ref pair"
        )
        val pid = pidRef.eget(0)?.asPid() ?: return InvalidRpcCall(
            Unknown,
            "invalid payload: no pid"
        )
        val ref = pidRef.eget(1)?.asReference() ?: return InvalidRpcCall(
            Unknown,
            "invalid payload: no ref"
        )
        val sender = GenCallCallType(pid, ref)
        return body.eget(2)?.asTuple()
            ?.let { typeAwareCallBodyParser.parse(it, sender) }
            ?: return InvalidRpcCall(sender, "invalid payload: no call")
    }
}
