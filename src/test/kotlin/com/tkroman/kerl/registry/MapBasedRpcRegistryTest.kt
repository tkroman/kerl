package com.tkroman.kerl.registry

import com.tkroman.kerl.badrpc
import com.tkroman.kerl.model.RpcMethod
import io.appulse.encon.terms.Erlang.atom
import kotlin.test.Test
import kotlin.test.assertEquals


internal class MapBasedRpcRegistryTest {
    @Test
    fun `present entries execute appropriately`() {
        val mbr = MapBasedRpcRegistry(
            mapOf(
                RpcMethod("foo", "bar") to { it }
            )
        )
        val result = mbr[RpcMethod("foo", "bar")].invoke(atom("x"))
        assertEquals(atom("x"), result)
    }

    @Test
    fun `absent entries return badrpc`() {
        val mbr = MapBasedRpcRegistry(emptyMap())
        val method = RpcMethod("foo", "bar")
        val result = mbr[method].invoke(atom("x"))
        assertEquals(badrpc("no handler for $method"), result)
    }
}
