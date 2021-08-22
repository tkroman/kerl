package com.tkroman.kerl.server.epmd.handler.processor

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.epmd.handler.NodeHandle
import io.appulse.epmd.java.core.model.request.Registration
import io.appulse.epmd.java.core.model.response.RegistrationResult
import io.appulse.epmd.java.core.model.response.Response
import java.net.Socket

internal class RegistrationRequestProcessor(
    request: Registration,
    socket: Socket,
    private val parent: InProcessEpmdServer,
) : RequestProcessor<Registration>(request, socket) {
    override fun respond(): Response {
        val node = NodeHandle(
            request.name,
            request.port,
            request.type,
            request.protocol,
            request.high,
            request.low,
            (System.currentTimeMillis() % 3 + 1).toInt(),
            socket,
        )
        parent.addNode(node)
        return RegistrationResult(true, node.creation)
    }

    override fun afterSend(response: Response) {
        if ((response as RegistrationResult).isOk.not()) {
            socket.close()
        }
    }
}
