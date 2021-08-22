package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.request.GetEpmdInfo
import io.appulse.epmd.java.core.model.response.EpmdInfo
import io.appulse.epmd.java.core.model.response.Response
import java.net.Socket

internal class GetEpmdInfoRequestProcessor(
    request: GetEpmdInfo,
    socket: Socket,
    private val server: InProcessEpmdServer,
) : RequestProcessor<GetEpmdInfo>(request, socket) {
    override fun respond(): Response {
        return EpmdInfo(
            server.config.port,
            server.getNodes().mapTo(mutableListOf()) {
                EpmdInfo.NodeDescription.builder()
                    .name(it.name)
                    .port(it.port)
                    .build()
            }
        )
    }
}
