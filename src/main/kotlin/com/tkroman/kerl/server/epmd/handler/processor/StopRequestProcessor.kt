package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.request.Stop
import io.appulse.epmd.java.core.model.response.Response
import io.appulse.epmd.java.core.model.response.StopResult
import java.net.Socket

internal class StopRequestProcessor(
    request: Stop,
    socket: Socket,
    private val server: InProcessEpmdServer
) : RequestProcessor<Stop>(request, socket) {
    override fun respond(): Response {
        return if (!server.config.unsafe) {
            StopResult.NOEXIST
        } else {
            server.removeNode(request.name)
                ?.let {
                    it.close()
                    StopResult.STOPPED
                }
                ?: StopResult.NOEXIST
        }
    }
}
