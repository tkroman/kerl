package com.tkroman.kerl.receiver

import com.tkroman.kerl.RECEIVER_MAILBOX
import com.tkroman.kerl.SENDER_PID
import com.tkroman.kerl.executor.RpcExecutor
import io.appulse.encon.connection.control.SendToRegisteredProcess
import io.appulse.encon.connection.regular.Message
import io.appulse.encon.mailbox.Mailbox
import io.appulse.encon.terms.type.ErlangAtom.ATOM_FALSE
import io.appulse.encon.terms.type.ErlangAtom.ATOM_TRUE
import io.appulse.encon.terms.type.ErlangPid
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import org.awaitility.kotlin.await
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RpcReceiverTest {
    private val mailbox = mockk<Mailbox>()
    private val executor = mockk<RpcExecutor>()
    private val rpcReceiver = RpcReceiver(mailbox, executor)

    @AfterTest
    fun cleanUp() {
        rpcReceiver.close()
    }

    @Test
    fun `throws if failed to start`() {
        every { mailbox.receive(any(), any()) }.returns(null)
        val executor = RpcReceiver(mailbox, executor)
        executor.start()
        assertFailsWith<IllegalStateException> { executor.start() }.also {
            assertEquals(
                "Couldn't start RpcReceiver. Make sure it was stopped correctly before reuse",
                it.message
            )
        }
    }

    @Test
    fun `happy path`() {
        every { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
            .returns(
                Message(
                    SendToRegisteredProcess(SENDER_PID, RECEIVER_MAILBOX),
                    ATOM_TRUE
                )
            )
            .andThen(null)
        every { executor.execute(ATOM_TRUE) }
            .returns(
                CompletableFuture.completedFuture(
                    SENDER_PID to ATOM_FALSE
                )
            )
        every { mailbox.send(SENDER_PID, ATOM_FALSE) }.returns(Unit)
        rpcReceiver.start()

        await
            .pollDelay(5, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .timeout(1, TimeUnit.SECONDS)
            .during(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                verify(atLeast = 1) { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
                verify(exactly = 1) { executor.execute(ATOM_TRUE) }
                verify(exactly = 1) { mailbox.send(SENDER_PID, ATOM_FALSE) }
            }
        excludeRecords { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
        confirmVerified(mailbox, executor)
    }

    @Test
    fun `no send() on failed handler`() {
        every { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
            .returns(
                Message(
                    SendToRegisteredProcess(SENDER_PID, RECEIVER_MAILBOX),
                    ATOM_TRUE
                )
            )
            .andThen(null)
        every { executor.execute(ATOM_TRUE) }
            .returns(
                CompletableFuture.failedFuture(IllegalStateException("foo"))
            )
        every { mailbox.send(SENDER_PID, ATOM_FALSE) }.returns(Unit)
        rpcReceiver.start()

        await
            .pollDelay(5, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .timeout(1, TimeUnit.SECONDS)
            .during(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                verify(atLeast = 1) { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
                verify(exactly = 1) { executor.execute(ATOM_TRUE) }
                verify(exactly = 0) { mailbox.send(any<ErlangPid>(), any()) }
            }
        excludeRecords { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
        confirmVerified(mailbox, executor)
    }

    @Test
    fun `no send() on null from handler`() {
        every { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
            .returns(
                Message(
                    SendToRegisteredProcess(SENDER_PID, RECEIVER_MAILBOX),
                    ATOM_TRUE
                )
            )
            .andThen(null)
        every { executor.execute(ATOM_TRUE) }
            .returns(
                CompletableFuture.completedFuture(null)
            )
        every { mailbox.send(SENDER_PID, ATOM_FALSE) }.returns(Unit)
        rpcReceiver.start()

        await
            .pollDelay(5, TimeUnit.MILLISECONDS)
            .pollInterval(5, TimeUnit.MILLISECONDS)
            .timeout(1, TimeUnit.SECONDS)
            .during(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                verify(atLeast = 1) { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
                verify(exactly = 1) { executor.execute(ATOM_TRUE) }
                verify(exactly = 0) { mailbox.send(any<ErlangPid>(), any()) }
            }
        excludeRecords { mailbox.receive(1L, TimeUnit.MILLISECONDS) }
        confirmVerified(mailbox, executor)
    }
}
