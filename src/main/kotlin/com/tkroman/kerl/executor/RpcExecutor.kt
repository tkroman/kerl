package com.tkroman.kerl.executor

import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid
import java.util.concurrent.CompletableFuture

interface RpcExecutor {
    /**
     * @return future of:
     *          - `null` if we don't know how to reply with the result,
     *          - pair of `<pid, body>` to respond with `body` to `pid`
     *
     */
    fun execute(body: ErlangTerm): CompletableFuture<Pair<ErlangPid, ErlangTerm>?>
}
