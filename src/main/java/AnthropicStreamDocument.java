import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.TextBlockParam;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
public class AnthropicStreamDocument {
    public static void main(String[] args) {
        try {
// Uses ANTHROPIC_API_KEY environment variable by default
            AnthropicClient client = AnthropicOkHttpClient.fromEnv();
// 1. Read the essay as plain text
            Path textPath = Paths.get("Machines-of-Loving-Grace.txt");
            String essayText = Files.readString(textPath);
// 2. Build the structured message parameters matching your Python structure
            MessageCreateParams params = MessageCreateParams.builder()
                    .model("claude-opus-5-5")
                    .maxTokens(20000L)
                    .system("""
You answer questions about the attached essay. For each question:
Find the passages that actually address it — the essay is the only source material.
Answer in two parts: a direct answer first, then the supporting evidence as short quotes with citations. Never paraphrase a claim you could quote.
Stay inside the text. If the essay doesn't address the question, say so — don't fill gaps with outside knowledge or speculation about the author's views.
Keep answers tight: most questions need one or two quotes, not a survey of the whole essay.
""")
                    .addUserMessageOfBlockParams(List.of(
// Document Block matching Python's document type
                            ContentBlockParam.ofDocument(
                                    DocumentBlockParam.builder()
                                            .source(PlainTextSource.builder().data(essayText).build())
                                            .title("Machines of Loving Grace")
                                            .citations(CitationsConfigParam.builder().enabled(true).build())
                                            .cacheControl(CacheControlEphemeral.builder().build())
                                            .build()
                            ),
// Prompt Text Block
                            ContentBlockParam.ofText(
                                    TextBlockParam.builder()
                                            .text("What does the essay predict about biology and health in the years after powerful AI arrives, and what does it claim is the real bottleneck to progress?")
                                            .build()
                            )
                    ))
                    .build();
// 3. Request the stream and process incoming text tokens
            try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
// Filter events for content blocks containing text fragments
                stream.stream()
                        .flatMap(event -> event.contentBlockDelta().stream())
                        .flatMap(deltaEvent -> deltaEvent.delta().text().stream())
                        .forEach(textDelta -> {
                            System.out.print(textDelta.text());
                            System.out.flush();
                        });
            }
        } catch (IOException e) {
            System.err.println("Error reading the essay text file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred during API communication: " + e.getMessage());
        }
    }
}
