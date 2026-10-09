package ph.appbuilders.offlinehealth.domain

/** Pure text shaping for a streamed model reply. */
object ReplyText {

    /**
     * The part of a partial reply that is safe to show while streaming: whole words only, and never a trailing
     * number, because the next word ("mg", "tablets") may turn it into a dose the guardrail must see first.
     */
    fun visiblePrefix(text: String): String {
        val end = text.indexOfLast { it.isWhitespace() }
        if (end < 0) return ""
        var shown = text.substring(0, end).trimEnd()
        while (shown.isNotEmpty()) {
            val lastSpace = shown.indexOfLast { it.isWhitespace() }
            if (shown.substring(lastSpace + 1).none { it.isDigit() }) break
            shown = shown.substring(0, lastSpace.coerceAtLeast(0)).trimEnd()
        }
        return shown
    }

    /** The model writes markdown, but the reply is shown as plain text: drop emphasis and headings, keep bullets. */
    fun plain(text: String): String = text
        .replace("**", "")
        .replace("__", "")
        .replace(HEADING, "")
        .replace(BULLET, "• ")

    /**
     * The final reply: trimmed, cut back to the last full sentence if the token cap stopped it mid-sentence,
     * and without a dangling list number ("2.") left at the end.
     */
    fun finish(text: String): String {
        val trimmed = dropListNumber(text.trim())
        if (trimmed.isEmpty() || trimmed.last() in SENTENCE_END) return trimmed
        val lastEnd = trimmed.indexOfLast { it in SENTENCE_END }
        return if (lastEnd >= 0) dropListNumber(trimmed.substring(0, lastEnd + 1)) else trimmed
    }

    private fun dropListNumber(text: String) = text.replace(DANGLING_NUMBER, "").trimEnd()

    private const val SENTENCE_END = ".!?…"
    private val HEADING = Regex("""(?m)^#{1,6}\s+""")
    private val BULLET = Regex("""(?m)^[ \t]*[*-][ \t]+""")
    private val DANGLING_NUMBER = Regex("""(?:\s*\n\s*\d+[.)])+$""")
}
