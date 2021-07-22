package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.GEN_CALL_TYPE
import com.tkroman.kerl.REX_CALL_TYPE
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.bstring
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.type.ErlangAtom.ATOM_TRUE
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypeAwareCallBodyParserTest {
    private val parser = TypeAwareCallBodyParser()

    @Test
    fun `happy path - non-empty args`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            list(ATOM_TRUE),
        )
        assertEquals(
            ValidRpcCall(REX_CALL_TYPE, RpcMethod("foo", "bar"), list(ATOM_TRUE)),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            ValidRpcCall(GEN_CALL_TYPE, RpcMethod("foo", "bar"), list(ATOM_TRUE)),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `happy path, empty args are represented as empty list`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
        )
        assertEquals(
            ValidRpcCall(REX_CALL_TYPE, RpcMethod("foo", "bar"), list()),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            ValidRpcCall(GEN_CALL_TYPE, RpcMethod("foo", "bar"), list()),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `happy path, NIL args are represented as empty list`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            NIL,
        )
        assertEquals(
            ValidRpcCall(REX_CALL_TYPE, RpcMethod("foo", "bar"), list()),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            ValidRpcCall(GEN_CALL_TYPE, RpcMethod("foo", "bar"), list()),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `invalid args - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            atom("not a list")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "invalid arguments"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "invalid arguments"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `no function - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no function"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no function"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `no module - invalid rpc call`() {
        val call = tuple(
            CALL,
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no module"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no module"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `module is not an atom - invalid rpc call`() {
        val call = tuple(
            CALL,
            bstring("not-an-atom")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no module"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no module"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `module is empty - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no module"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no module"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `function is not an atom - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            bstring("bar")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no function"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no function"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `function is empty - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            bstring("")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "no function"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "no function"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `no call - invalid rpc call`() {
        val call = tuple()
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "invalid call section"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "invalid call section"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `call is not atom(call) - invalid rpc call`() {
        val call = tuple(
            atom("something-else")
        )
        assertEquals(
            InvalidRpcCall(REX_CALL_TYPE, "invalid call section"),
            parser.parse(call, REX_CALL_TYPE)
        )
        assertEquals(
            InvalidRpcCall(GEN_CALL_TYPE, "invalid call section"),
            parser.parse(call, GEN_CALL_TYPE)
        )
    }

    @Test
    fun `unknown call type`() {
        val result = parser.parse(atom("doesnt-matter"), Unknown)
        assertEquals(
            InvalidRpcCall(Unknown, "unknown call type"),
            result
        )
    }
}
