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
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.type.ErlangAtom.ATOM_TRUE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SyncRpcCallExecutorTest {
    private val syncRpcCallExecutor = SyncRpcCallExecutor(
        AggregateRpcCallParser(),
        MapBasedRpcRegistry(
            mapOf(
                RpcMethod("foo", "bar") to { atom(it.asBoolean().not()) }
            )
        ),
        AggregateResponseFactory()
    )

    @Test
    fun `gen_call happy path`() {
        val result = syncRpcCallExecutor.execute(validGenCallCall(ATOM_TRUE))
        assertTrue(result.isDone)
        assertEquals(
            SENDER_PID to tuple(SENDER_REF, ATOM_TRUE),
            result.get()
        )
    }

    @Test
    fun `rex happy path`() {
        val result = syncRpcCallExecutor.execute(validRexCall(ATOM_TRUE))
        assertTrue(result.isDone)
        assertEquals(
            SENDER_PID to tuple(REX, ATOM_TRUE),
            result.get()
        )
    }

    @Test
    fun `invalid call`() {
        val result = syncRpcCallExecutor.execute(tuple(atom("something-invalid")))
        assertTrue(result.isDone)
        assertEquals(null, result.get())
    }
}
