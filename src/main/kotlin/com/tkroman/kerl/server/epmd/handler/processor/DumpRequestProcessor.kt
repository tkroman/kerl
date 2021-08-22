package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.request.GetEpmdDump
import io.appulse.epmd.java.core.model.response.EpmdDump
import io.appulse.epmd.java.core.model.response.Response
import java.net.Socket

internal class DumpRequestProcessor(
    request: GetEpmdDump,
    socket: Socket,
    private val server: InProcessEpmdServer,
) : RequestProcessor<GetEpmdDump>(request, socket) {
    override fun respond(): Response {
        return EpmdDump(
            server.config.port,
            server.getNodes().mapTo(mutableListOf()) {
                EpmdDump.NodeDump.builder()
                    .status(EpmdDump.NodeDump.Status.ACTIVE)
                    .name(it.name)
                    .port(it.port)
                    .fileDescriptor(-1)
                    .build()
            }
        )
    }
}
