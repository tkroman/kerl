package com.tkroman.kerl.server.epmd.handler.processor

import io.appulse.epmd.java.core.model.request.Request
import io.appulse.epmd.java.core.model.response.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Socket

internal abstract class RequestProcessor<R : Request>(
    protected val request: R,
    protected val socket: Socket
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    fun process() {
        respond()
            ?.also {
                send(it)
                afterSend(it)
            }
            ?: run {
                socket.close()
            }
    }

    protected abstract fun respond(): Response?

    private fun send(response: Response) {
        logger.debug("sending a response to {}", socket.remoteSocketAddress)
        val responseBytes = response.toBytes()
        socket.getOutputStream().write(responseBytes)
        socket.getOutputStream().flush()
        logger.debug("{} was sent to {}", response, socket.remoteSocketAddress)
    }

    protected open fun afterSend(response: Response) {
        logger.debug("close connection to {}", socket.remoteSocketAddress)
        socket.close()
    }

}
