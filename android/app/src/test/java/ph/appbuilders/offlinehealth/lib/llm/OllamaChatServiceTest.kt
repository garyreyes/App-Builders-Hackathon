package ph.appbuilders.offlinehealth.lib.llm

import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.domain.model.AiStatus
import ph.appbuilders.offlinehealth.domain.model.ChatResult
import ph.appbuilders.offlinehealth.domain.model.DangerMessage
import ph.appbuilders.offlinehealth.domain.model.DangerSignId
import ph.appbuilders.offlinehealth.domain.model.Language
import ph.appbuilders.offlinehealth.domain.model.TopicCard
import ph.appbuilders.offlinehealth.domain.model.TopicId

class OllamaChatServiceTest {

    private class FakeClient(
        var reachable: Boolean = true,
        val chunks: List<String> = emptyList(),
        val failAfter: Int? = null,
    ) : LlmClient {
        var replies = 0
        override suspend fun warmUp() = reachable
        override fun reply(userText: String): Flow<String> = flow {
            replies++
            chunks.forEachIndexed { i, chunk ->
                if (i == failAfter) throw IOException("connection reset")
                emit(chunk)
            }
        }
    }

    private val card = TopicCard(TopicId.CHILD_DIARRHEA, "Diarrhea", listOf("Give fluids."), listOf("blood"), "WHO")
    private val withCard = ChatResult(emptyList(), card, emptySet(), emptyList(), ai = null)
    private val noCard = ChatResult(emptyList(), null, emptySet(), emptyList(), ai = null)

    private fun TestScope.service(client: LlmClient, result: ChatResult = withCard) =
        OllamaChatService(client, { _, _ -> result }, backgroundScope)

    private suspend fun OllamaChatService.states(text: String = "q") =
        send(text, Language.WAR).toList().map { it.ai }

    @Test fun noTopicMeansNoAiAndNoModelCall() = runTest {
        val client = FakeClient(chunks = listOf("Hello "))
        val results = service(client, noCard).send("toothache", Language.ENG).toList()
        assertEquals(1, results.size)
        assertNull(results.single().ai)
        assertEquals(0, client.replies)
    }

    @Test fun safeReplyStreamsWholeWordsThenFinishes() = runTest {
        val client = FakeClient(chunks = listOf("Hatagi ", "Hatagi hin tu", "Hatagi hin tubig.", "Hatagi hin tubig. Ngan"))
        val states = service(client).states()
        assertTrue(states.contains(AiReplyState.Streaming("Hatagi")))
        assertTrue(states.contains(AiReplyState.Streaming("Hatagi hin")))
        assertEquals(AiReplyState.Done("Hatagi hin tubig."), states.last())
    }

    @Test fun unsafeReplyIsWithheldAndTheDrugNameIsNeverShown() = runTest {
        val client = FakeClient(chunks = listOf("Ihatag ", "Ihatag an para", "Ihatag an paracetamol ", "Ihatag an paracetamol ngan"))
        val states = service(client).states()
        assertEquals(AiReplyState.Withheld, states.last())
        assertFalse(states.any { it is AiReplyState.Streaming && "paracetamol" in it.text })
    }

    @Test fun aNumberIsNeverShownBeforeTheGuardrailSeesItsUnit() = runTest {
        val client = FakeClient(chunks = listOf("Give ", "Give 5 ", "Give 5 ml "))
        val states = service(client).states()
        assertEquals(AiReplyState.Withheld, states.last())
        assertFalse(states.any { it is AiReplyState.Streaming && "5" in it.text })
    }

    @Test fun downplayIsWithheldWhenADangerIsShown() = runTest {
        val danger = withCard.copy(dangers = listOf(DangerMessage(DangerSignId.BLOOD_IN_STOOL, "Blood in the stool.")))
        val client = FakeClient(chunks = listOf("It's not serious. "))
        assertEquals(AiReplyState.Withheld, service(client, danger).states().last())
    }

    @Test fun unreachableModelMeansBasicMode() = runTest {
        val client = FakeClient(reachable = false, chunks = listOf("Hello. "))
        val chat = service(client)
        val states = chat.states()
        assertEquals(AiReplyState.BasicMode, states.last())
        assertEquals(AiStatus.BASIC, chat.aiStatus.value)
        assertEquals(0, client.replies)
    }

    @Test fun modelThatComesBackIsRetriedOnTheNextSend() = runTest {
        val client = FakeClient(reachable = false, chunks = listOf("Drink water. "))
        val chat = service(client)
        chat.states() // BASIC
        client.reachable = true
        assertEquals(AiReplyState.Done("Drink water."), chat.states().last())
        assertEquals(AiStatus.READY, chat.aiStatus.value)
    }

    @Test fun basicModeChosenByTheUserIsNotOverridden() = runTest {
        val client = FakeClient(chunks = listOf("Drink water. "))
        val chat = service(client)
        chat.useBasicMode()
        assertEquals(AiReplyState.BasicMode, chat.states().last())
        assertEquals(0, client.replies)
    }

    @Test fun connectionLostMidReplyIsWithheld() = runTest {
        val client = FakeClient(chunks = listOf("Drink ", "Drink water ", "x"), failAfter = 2)
        assertEquals(AiReplyState.Withheld, service(client).states().last())
    }
}
