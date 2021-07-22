package com.tkroman.kerl

import io.appulse.encon.terms.Erlang.atom
import io.appulse.encon.terms.Erlang.bstring
import io.appulse.encon.terms.Erlang.list
import io.appulse.encon.terms.Erlang.map
import io.appulse.encon.terms.Erlang.tuple
import kotlin.test.Test
import kotlin.test.assertNull

internal class SharedKtTest {
    @Test
    fun `eget on invalid indexes`() {
        assertNull(list().eget(0))
        assertNull(tuple().eget(0))
        assertNull(map().eget(0))
        assertNull(atom("doesnt-even-make-sense").eget(0))
        assertNull(bstring("doesnt-even-make-sense").eget(0))

        assertNull(list(atom("a")).eget(10))
        assertNull(tuple(atom("a")).eget(10))
        assertNull(map(atom("x"), atom("y")).eget(10))
        assertNull(atom("doesnt-even-make-sense").eget(10))
        assertNull(bstring("doesnt-even-make-sense").eget(10))
    }
}
