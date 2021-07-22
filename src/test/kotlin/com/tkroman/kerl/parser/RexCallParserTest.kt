package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.SENDER_PID
import com.tkroman.kerl.REX_CALL_TYPE
import com.tkroman.kerl.validRexCall
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.tuple
import kotlin.test.Test
import kotlin.test.assertEquals


internal class RexCallParserTest {
    private val rexCallParser = RexCallParser(TypeAwareCallBodyParser())

    @Test
    fun `happy path`() {
        val result = rexCallParser.parse(validRexCall(atom("x")))
        assertEquals(
            ValidRpcCall(REX_CALL_TYPE, RpcMethod("foo", "bar"), list(atom("x"))),
            result
        )
    }

    @Test
    fun `no pid - invalid call`() {
        val result = rexCallParser.parse(
            tuple()
        )
        assertEquals(
            InvalidRpcCall(Unknown, "body is an empty tuple"),
            result
        )
    }

    @Test
    fun `not a pid - invalid call`() {
        val result = rexCallParser.parse(
            tuple(
                atom("something-else"),
                tuple(
                    CALL,
                    atom("foo"),
                    atom("bar"),
                    NIL
                )
            )
        )
        assertEquals(
            InvalidRpcCall(Unknown, "not a rex call"),
            result
        )
    }

    @Test
    fun `no call - invalid call`() {
        val result = rexCallParser.parse(
            tuple(
                SENDER_PID,
            )
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "invalid payload: no call"),
            result
        )
    }

    @Test
    fun `call is not a tuple - invalid call`() {
        val result = rexCallParser.parse(
            tuple(
                SENDER_PID,
                atom("something-else"),
            )
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "invalid payload: no call"),
            result
        )
    }
}
