package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import io.appulse.epmd.java.core.model.request.Kill
import io.appulse.epmd.java.core.model.response.KillResult
import io.appulse.epmd.java.core.model.response.Response
import java.net.Socket

internal class KillRequestProcessor(
    request: Kill,
    socket: Socket,
    private val server: InProcessEpmdServer,
) : RequestProcessor<Kill>(request, socket) {

    override fun respond(): Response {
        return if (!server.config.unsafe) {
            KillResult.NOK
        } else {
            server.getNodes().forEach { it.close() }
            KillResult.OK
        }
    }

    override fun afterSend(response: Response) {
        if (response == KillResult.OK) {
            server.epmdExecutor.shutdownNow()
        }
    }
}
