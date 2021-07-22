package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.GEN_CALL_TYPE
import com.tkroman.kerl.SENDER_PID
import com.tkroman.kerl.REX_CALL_TYPE
import com.tkroman.kerl.validGenCallCall
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.tuple
import kotlin.test.Test
import kotlin.test.assertEquals


internal class AggregateRpcCallParserTest {
    private val aggregateRpcCallParser = AggregateRpcCallParser()

    @Test
    fun `gen_call happy path`() {
        val result = aggregateRpcCallParser.parse(validGenCallCall(atom("x")))
        assertEquals(
            ValidRpcCall(GEN_CALL_TYPE, RpcMethod("foo", "bar"), list(atom("x"))),
            result
        )
    }

    @Test
    fun `rex happy path`() {
        val result = aggregateRpcCallParser.parse(
            tuple(
                SENDER_PID,
                tuple(
                    CALL,
                    atom("foo"),
                    atom("bar"),
                    list(atom("x"))
                )
            )
        )
        assertEquals(
            ValidRpcCall(REX_CALL_TYPE, RpcMethod("foo", "bar"), list(atom("x"))),
            result
        )
    }

    @Test
    fun `not a tuple - invalid call`() {
        val result = aggregateRpcCallParser.parse(
            atom("not-a-tuple")
        )
        assertEquals(
            InvalidRpcCall(Unknown, "body is not a tuple"),
            result
        )
    }

    @Test
    fun `empty tuple - invalid call`() {
        val result = aggregateRpcCallParser.parse(
            tuple()
        )
        assertEquals(
            InvalidRpcCall(Unknown, "body is an empty tuple"),
            result
        )
    }

    @Test
    fun `unexpected tuple - invalid call`() {
        val result = aggregateRpcCallParser.parse(
            tuple(
                atom("invalid-element")
            )
        )
        assertEquals(
            InvalidRpcCall(Unknown, "unknown call type"),
            result
        )
    }
}
