package com.tkroman.kerl.server

data class KerlServerConfig(
    val server: ServerConfig,
    val node: NodeConfig,
    val epmd: EpmdConfig,
) {
    data class ServerConfig(
        var port: Int,
        var bossThreads: Int,
        var workerThreads: Int,
    )

    data class NodeConfig(
        val name: String,
        val cookie: String,
        val shortName: Boolean,
    )

    data class EpmdConfig(
        val port: Int,
        val host: String,
        val unsafe: Boolean,
    )
}
