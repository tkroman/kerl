package com.tkroman.kerl.parser

import com.tkroman.kerl.CALL
import com.tkroman.kerl.GEN_CALL_TYPE
import com.tkroman.kerl.REX_CALL_TYPE
import com.tkroman.kerl.model.InvalidRpcCall
import com.tkroman.kerl.model.RpcCall
import com.tkroman.kerl.model.RpcCallType
import com.tkroman.kerl.model.RpcMethod
import com.tkroman.kerl.model.Unknown
import com.tkroman.kerl.model.ValidRpcCall
import io.appulse.encon.terms.Erlang.NIL
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.bstring
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.map
import io.appulse.encon.terms.Erlang.tuple
import io.appulse.encon.terms.type.ErlangAtom.ATOM_FALSE
import io.appulse.encon.terms.type.ErlangAtom.ATOM_TRUE
import io.appulse.encon.terms.type.ErlangTuple
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypeAwareCallBodyParserTest {
    private val parser = TypeAwareCallBodyParser()

    private fun assertBoth(
        call: ErlangTuple,
        expected: (RpcCallType) -> RpcCall,
    ) {
        assertEquals(expected(REX_CALL_TYPE), parser.parse(call, REX_CALL_TYPE))
        assertEquals(expected(GEN_CALL_TYPE), parser.parse(call, GEN_CALL_TYPE))
    }

    @Test
    fun `happy path - non-empty args`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            list(ATOM_TRUE),
        )
        assertBoth(call) { ValidRpcCall(it, RpcMethod("foo", "bar"), list(ATOM_TRUE)) }
    }

    @Test
    fun `happy path, empty args are represented as empty list`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
        )
        assertBoth(call) { ValidRpcCall(it, RpcMethod("foo", "bar"), list()) }
    }

    @Test
    fun `happy path, NIL args are represented as empty list`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            NIL,
        )
        assertBoth(call) { ValidRpcCall(it, RpcMethod("foo", "bar"), list()) }
    }

    @Test
    fun `invalid args - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("bar"),
            atom("not a list")
        )
        assertBoth(call) { InvalidRpcCall(it, "invalid arguments") }
    }

    @Test
    fun `no function - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
        )
        assertBoth(call) { InvalidRpcCall(it, "no function") }
    }

    @Test
    fun `no module - invalid rpc call`() {
        val call = tuple(
            CALL,
        )
        assertBoth(call) { InvalidRpcCall(it, "no module") }
    }

    @Test
    fun `module is a bstring - invalid rpc call`() {
        val call = tuple(
            CALL,
            bstring("not-an-atom")
        )
        assertBoth(call) { InvalidRpcCall(it, "no module") }
    }

    @Test
    fun `module is a list - invalid rpc call`() {
        val call = tuple(
            CALL,
            list(ATOM_TRUE)
        )
        assertBoth(call) {
            InvalidRpcCall(it, "no module")
        }
    }

    @Test
    fun `module is a map - invalid rpc call`() {
        val call = tuple(
            CALL,
            map(ATOM_TRUE, ATOM_FALSE)
        )
        assertBoth(call) { InvalidRpcCall(it, "no module") }
    }

    @Test
    fun `module is empty - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("")
        )
        assertBoth(call) { InvalidRpcCall(it, "no module") }
    }

    @Test
    fun `function is a bstring - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            bstring("bar")
        )
        assertBoth(call) { InvalidRpcCall(it, "no function") }
    }

    @Test
    fun `function is a list - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            list(ATOM_TRUE)
        )
        assertBoth(call) { InvalidRpcCall(it, "no function") }
    }

    @Test
    fun `function is a map - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            map(ATOM_TRUE, ATOM_FALSE)
        )
        assertBoth(call) { InvalidRpcCall(it, "no function") }
    }

    @Test
    fun `function is empty - invalid rpc call`() {
        val call = tuple(
            CALL,
            atom("foo"),
            atom("")
        )
        assertBoth(call) { InvalidRpcCall(it, "no function") }
    }

    @Test
    fun `no call - invalid rpc call`() {
        val call = tuple()
        assertBoth(call) { InvalidRpcCall(it, "invalid call section") }
    }

    @Test
    fun `call is not atom(call) - invalid rpc call`() {
        val call = tuple(
            atom("something-else")
        )
        assertBoth(call) {
            InvalidRpcCall(it, "invalid call section")
        }
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
