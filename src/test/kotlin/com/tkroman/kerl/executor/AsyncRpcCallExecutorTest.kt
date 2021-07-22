package com.tkroman.kerl.executor

import com.tkroman.kerl.REX
import com.tkroman.kerl.SENDER_PID
import com.tkroman.kerl.SENDER_REF
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.parser.AggregateRpcCallParser
import com.tkroman.kerl.registry.MapBasedRpcRegistry
import com.tkroman.kerl.responder.AggregateResponseFactory
import com.tkroman.kerl.validGenCallCall
import com.tkroman.kerl.validRexCall
import io.appulse.encon.terms.Erlang
import io.appulse.encon.terms.type.ErlangAtom
import org.awaitility.kotlin.await
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AsyncRpcCallExecutorTest {
    private val asyncRpcCallExecutor = AsyncRpcCallExecutor(
        SyncRpcCallExecutor(
            AggregateRpcCallParser(),
            MapBasedRpcRegistry(
                mapOf(
                    RpcMethod("foo", "bar") to { Erlang.atom(it.asBoolean().not()) }
                )
            ),
            AggregateResponseFactory()
        )
    )

    @Test
    fun `gen_call happy path`() {
        val result = asyncRpcCallExecutor.execute(validGenCallCall(ErlangAtom.ATOM_TRUE))
        await
            .atMost(500, TimeUnit.MILLISECONDS)
            .pollDelay(5, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertTrue(result.isDone)
                assertEquals(
                    SENDER_PID to Erlang.tuple(SENDER_REF, ErlangAtom.ATOM_TRUE),
                    result.get()
                )
            }
    }

    @Test
    fun `rex happy path`() {
        val result = asyncRpcCallExecutor.execute(validRexCall(ErlangAtom.ATOM_TRUE))
        await
            .atMost(500, TimeUnit.MILLISECONDS)
            .pollDelay(5, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertTrue(result.isDone)
                assertEquals(
                    SENDER_PID to Erlang.tuple(REX, ErlangAtom.ATOM_TRUE),
                    result.get()
                )
            }
    }
}
