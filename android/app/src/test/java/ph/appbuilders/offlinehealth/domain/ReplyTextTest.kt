package ph.appbuilders.offlinehealth.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ReplyTextTest {

    @Test fun visiblePrefixHoldsBackThePartialLastWord() {
        assertEquals("Hatagi hin", ReplyText.visiblePrefix("Hatagi hin tub"))
        assertEquals("", ReplyText.visiblePrefix("Hello"))
    }

    @Test fun visiblePrefixHoldsBackATrailingNumberUntilTheNextWordIsKnown() {
        // "500" could be followed by "mg"; never show it before the guardrail has seen the next word.
        assertEquals("Give", ReplyText.visiblePrefix("Give 500"))
        assertEquals("Give", ReplyText.visiblePrefix("Give 500 "))
        assertEquals("Cool it for 20 minutes", ReplyText.visiblePrefix("Cool it for 20 minutes now"))
    }

    @Test fun finishCutsAReplyThatStoppedMidSentence() {
        assertEquals("Drink water.", ReplyText.finish("Drink water. Rest and keep"))
        assertEquals("Drink water. Rest!", ReplyText.finish("  Drink water. Rest!  "))
        assertEquals("no punctuation at all", ReplyText.finish("no punctuation at all"))
    }

    @Test fun finishDropsADanglingListNumber() {
        // Seen on the emulator: the token cap stopped right after "2." of a numbered list.
        assertEquals("Drink water.", ReplyText.finish("Drink water.\n\n2."))
        assertEquals("1. Drink water.", ReplyText.finish("1. Drink water.\n2. Re"))
    }

    @Test fun plainRemovesMarkdownTheUiWouldShowLiterally() {
        assertEquals("Mga Tip\n\n1. Pag-inom: Uminom hin tubig.",
            ReplyText.plain("## **Mga Tip**\n\n1. **Pag-inom**: Uminom hin tubig."))
        assertEquals("Rest well", ReplyText.plain("__Rest__ well"))
    }
}
