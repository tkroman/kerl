package com.tkroman.kerl.server.epmd

interface EpmdServer {
    fun start(): Unit
    fun stop(): Unit
}

