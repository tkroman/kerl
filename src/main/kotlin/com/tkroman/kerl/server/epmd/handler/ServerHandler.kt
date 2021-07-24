package com.tkroman.kerl.server.epmd.handler

import com.tkroman.kerl.server.epmd.InProcessEpmdServer
import com.tkroman.kerl.server.epmd.handler.processor.DumpRequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.GetEpmdInfoRequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.GetNodeInfoRequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.KillRequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.RegistrationRequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.RequestProcessor
import com.tkroman.kerl.server.epmd.handler.processor.StopRequestProcessor
import io.appulse.epmd.java.core.model.Tag
import io.appulse.epmd.java.core.model.request.GetEpmdDump
import io.appulse.epmd.java.core.model.request.GetEpmdInfo
import io.appulse.epmd.java.core.model.request.GetNodeInfo
import io.appulse.epmd.java.core.model.request.Kill
import io.appulse.epmd.java.core.model.request.Registration
import io.appulse.epmd.java.core.model.request.Request
import io.appulse.epmd.java.core.model.request.Stop
import io.appulse.utils.BytesUtils
import io.appulse.utils.SocketUtils
import org.slf4j.LoggerFactory
import java.net.Socket

internal class ServerHandler(
    private val socket: Socket,
    private val server: InProcessEpmdServer,
) : Runnable {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun run() {
        try {
            val requestLengthBytes = SocketUtils.read(socket, 2)
            val requestLength = BytesUtils.asShort(requestLengthBytes)
            val requestBytes = SocketUtils.read(socket, requestLength.toInt())
            val request = Request.parse<Request>(requestBytes, requestLength.toInt())
            logger.debug("Request: $request")
            findProcessor(request, socket)?.also {
                logger.debug("request processor is ${it.javaClass.simpleName}", )
                it.process()
            }
        } catch (ex: Exception) {
            logger.error("connection error", ex)
        }
    }

    private fun findProcessor(
        request: Request,
        clientSocket: Socket
    ): RequestProcessor<*>? {
        return when (request.tag) {
            Tag.ALIVE2_REQUEST -> RegistrationRequestProcessor(
                request as Registration,
                clientSocket,
                server
            )
            Tag.DUMP_REQUEST -> DumpRequestProcessor(request as GetEpmdDump, clientSocket, server)
            Tag.KILL_REQUEST -> KillRequestProcessor(request as Kill, clientSocket, server)
            Tag.PORT_PLEASE2_REQUEST -> GetNodeInfoRequestProcessor(
                request as GetNodeInfo,
                clientSocket,
                server
            )
            Tag.NAMES_REQUEST -> GetEpmdInfoRequestProcessor(
                request as GetEpmdInfo,
                clientSocket,
                server
            )
            Tag.STOP_REQUEST -> StopRequestProcessor(request as Stop, clientSocket, server)
            else -> {
                logger.warn("unsupported request tag ${request.tag}")
                null
            }
        }
    }
}
