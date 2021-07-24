package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.request.GetNodeInfo
import io.appulse.epmd.java.core.model.response.NodeInfo
import io.appulse.epmd.java.core.model.response.Response
import java.net.Socket

internal class GetNodeInfoRequestProcessor(
    request: GetNodeInfo,
    socket: Socket,
    private val server: InProcessEpmdServer,
) : RequestProcessor<GetNodeInfo>(request, socket) {
    override fun respond(): Response? {
        return server.getNode(request.name)
            ?.let {
                NodeInfo(
                    true,
                    it.port,
                    it.type,
                    it.protocol,
                    it.high,
                    it.low,
                    it.name,
                    null
                )
            }
            ?: NodeInfo.builder().ok(false).build()
    }
}
