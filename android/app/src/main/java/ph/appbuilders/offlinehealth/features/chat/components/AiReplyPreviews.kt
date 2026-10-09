package ph.appbuilders.offlinehealth.features.chat.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ph.appbuilders.offlinehealth.domain.model.AiReplyState
import ph.appbuilders.offlinehealth.fakes.FakeSamples
import ph.appbuilders.offlinehealth.fakes.PreviewFrame

// Every AiReply state, mirroring the AIReply row of Specs.dc.html.

@Composable
private fun AiReplyPreview(state: AiReplyState) = PreviewFrame { AiReply(state, onDownloadAi = {}) }

@Preview(widthDp = 360) @Composable private fun AiThinking() = AiReplyPreview(AiReplyState.Thinking)
@Preview(widthDp = 360) @Composable private fun AiWarming() = AiReplyPreview(AiReplyState.WarmingUp)
@Preview(widthDp = 360) @Composable
private fun AiStreaming() = AiReplyPreview(AiReplyState.Streaming(FakeSamples.REPLY_CEB_PARTIAL))
@Preview(widthDp = 360) @Composable private fun AiDone() = AiReplyPreview(AiReplyState.Done(FakeSamples.REPLY_CEB))
@Preview(widthDp = 360) @Composable private fun AiWithheld() = AiReplyPreview(AiReplyState.Withheld)
@Preview(widthDp = 360) @Composable private fun AiBasic() = AiReplyPreview(AiReplyState.BasicMode)
