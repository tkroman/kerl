package com.tkroman.kerl.parser

import com.tkroman.kerl.GEN_CALL
import com.tkroman.kerl.GEN_CALL_TYPE
import com.tkroman.kerl.SENDER_PID
import com.tkroman.kerl.SENDER_REF
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


internal class GenCallParserTest {
    private val genCallParser = GenCallParser(TypeAwareCallBodyParser())

    @Test
    fun `happy path`() {
        val result = genCallParser.parse(validGenCallCall(atom("x")))
        assertEquals(
            ValidRpcCall(GEN_CALL_TYPE, RpcMethod("foo", "bar"), list(atom("x"))),
            result
        )
    }

    @Test
    fun `no pid-ref pair - invalid call`() {
        val result = genCallParser.parse(
            tuple(
                GEN_CALL,
            )
        )
        assertEquals(
            InvalidRpcCall(Unknown, "invalid payload: no pid-ref pair"),
            result
        )
    }

    @Test
    fun `no pid in pid-ref pair - invalid call`() {
        val result = genCallParser.parse(
            tuple(
                GEN_CALL,
                tuple()
            )
        )
        assertEquals(
            InvalidRpcCall(Unknown, "invalid payload: no pid"),
            result
        )
    }

    @Test
    fun `no ref in pid-ref pair - invalid call`() {
        val result = genCallParser.parse(
            tuple(
                GEN_CALL,
                tuple(
                    SENDER_PID,
                )
            )
        )
        assertEquals(
            InvalidRpcCall(Unknown, "invalid payload: no ref"),
            result
        )
    }

    @Test
    fun `no call - invalid call`() {
        val result = genCallParser.parse(
            tuple(
                GEN_CALL,
                tuple(
                    SENDER_PID,
                    SENDER_REF,
                )
            )
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "invalid payload: no call"),
            result
        )
    }

    @Test
    fun `call is not a tuple - invalid call`() {
        val result = genCallParser.parse(
            tuple(
                GEN_CALL,
                tuple(
                    SENDER_PID,
                    SENDER_REF,
                ),
                atom("not-a-tuple")
            )
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "invalid payload: no call"),
            result
        )
    }
}
