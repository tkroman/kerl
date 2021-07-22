package com.tkroman.kerl.responder

import com.tkroman.kerl.GEN_CALL_TYPE
import com.tkroman.kerl.REX
import com.tkroman.kerl.REX_CALL_TYPE
import com.tkroman.kerl.model.Unknown
import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.tuple
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AggregateResponseFactoryTest {
    private val factory = AggregateResponseFactory()

    @Test
    fun `unknown call type - null`() {
        val result = factory.constructReply(Unknown, atom("anything"))
        assertNull(result)
    }

    @Test
    fun `gen_call response`() {
        val term = atom("term")
        val result = factory.constructReply(GEN_CALL_TYPE, term)
        assertEquals(
            GEN_CALL_TYPE.senderPid to tuple(GEN_CALL_TYPE.senderRef, term),
            result
        )
    }

    @Test
    fun `rex response`() {
        val term = atom("term")
        val result = factory.constructReply(REX_CALL_TYPE, term)
        assertEquals(
            REX_CALL_TYPE.senderPid to tuple(REX, term),
            result
        )
    }
}
