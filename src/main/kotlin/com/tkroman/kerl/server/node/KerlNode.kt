package com.tkroman.kerl.server.node

import io.appulse.encon.mailbox.Mailbox

interface KerlNode {
    fun start()
    fun stop()
    fun mailbox(): Mailbox
}

