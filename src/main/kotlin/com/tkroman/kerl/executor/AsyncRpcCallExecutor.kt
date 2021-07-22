package com.tkroman.kerl.executor

import io.appulse.encon.terms.ErlangTerm
import io.appulse.encon.terms.type.ErlangPid
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/**
 * This is very naive for now (just delegates to sync executor),
 * we may do better in future (no pun intended).
 */
class AsyncRpcCallExecutor(
    private val syncRpcCallExecutor: SyncRpcCallExecutor
) : RpcExecutor {
    private val executor = Executors.newWorkStealingPool()

    override fun execute(body: ErlangTerm): CompletableFuture<Pair<ErlangPid, ErlangTerm>?> {
        return CompletableFuture.supplyAsync(
            {
                // ok to call get here since sync executor is just wrapping the result
                // into the CompletedFuture directly
                syncRpcCallExecutor.execute(body).get()
            },
            executor
        )
    }
}
