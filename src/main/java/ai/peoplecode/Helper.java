package ai.peoplecode;
import com.openai.core.JsonField;
import com.openai.models.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides helper methods for OpenAIConversation class
 * @see OpenAIConversation
 */
public class Helper {

    /**
     * Runs OpenAI Thread. This leads to the OpenAI Assistant generating a response to the user's questions and any other conversation history in the current window for the Thread
     * @param conversation OpenAIConversation object
     * @param assistant OpenAI Assistant object <a href="https://platform.openai.com/assistants/">Create assistant here</a>
     * @see OpenAIConversation
     * @apiNote See more information on how OpenAI Assistants and OpenAI Threads work <a href="https://platform.openai.com/docs/assistants/overview">here</a>
     */
    protected static void runThread(OpenAIConversation conversation, Assistant assistant) {

        Run run = conversation.client.beta()
                .threads()
                .runs()
                .create(BetaThreadRunCreateParams.builder()
                        .threadId(conversation.conversationThread.id())
                        .assistantId(assistant.id())
                        .build());
        while (run.status().equals(RunStatus.QUEUED) || run.status().equals(RunStatus.IN_PROGRESS)) {
//            System.out.println("Polling run...");
//            java.lang.Thread.sleep(1000);
            run = conversation.client.beta()
                    .threads()
                    .runs()
                    .retrieve(BetaThreadRunRetrieveParams.builder()
                            .threadId(conversation.conversationThread.id())
                            .runId(run.id())
                            .build());
        }
//        System.out.println("Run completed with status: " + run.status());
        if (run.status().equals(RunStatus.FAILED)){
            throw new RuntimeException("Thread Run Error: " + run.lastError().get().message());
        }
    }

    /**
     * Runs OpenAI Thread. This leads to the OpenAI Assistant generating a response to the user's questions and any other conversation history in the current window for the Thread
     * @param conversation OpenAIConversation object
     * @param assistant OpenAI Assistant object <a href="https://platform.openai.com/assistants/">Create assistant here</a>
     * @param instructions Additional instruction set to be sent to OpenAI Assistant to instruct assistant on how to answer
     * @see OpenAIConversation
     * @apiNote See more information on how OpenAI Assistants and OpenAI Threads work <a href="https://platform.openai.com/docs/assistants/overview">here</a>
     */
    protected static void runThread(OpenAIConversation conversation, Assistant assistant, String instructions) {

        Run run = conversation.client.beta()
                .threads()
                .runs()
                .create(BetaThreadRunCreateParams.builder()
                        .threadId(conversation.conversationThread.id())
                        .additionalInstructions(instructions)
                        .assistantId(assistant.id())
                        .build());
        while (run.status().equals(RunStatus.QUEUED) || run.status().equals(RunStatus.IN_PROGRESS)) {
//            System.out.println("Polling run...");
//            java.lang.Thread.sleep(1000);
            run = conversation.client.beta()
                    .threads()
                    .runs()
                    .retrieve(BetaThreadRunRetrieveParams.builder()
                            .threadId(conversation.conversationThread.id())
                            .runId(run.id())
                            .build());
        }
//        System.out.println("Run completed with status: " + run.status());
        if (run.status().equals(RunStatus.FAILED)){
            throw new RuntimeException("Thread Run Error: " + run.lastError().get().message());
        }
    }

    /**
     * Retrieves message from Message List
     * @param conversation OpenAIConversation object
     * @return message
     * @see OpenAIConversation
     * @throws NoSuchElementException OpenAI's Java API introduces optionals where there may or may not be a value returned. In the event that no message is present, .get() will throw an exception
     */
    public static String getMessage(OpenAIConversation conversation){

        BetaThreadMessageListPage page = conversation.client.beta()
                .threads()
                .messages()
                .list(BetaThreadMessageListParams.builder()
                        .threadId(conversation.conversationThread.id())
                        .order(BetaThreadMessageListParams.Order.ASC)
                        .build());
        try {
//            return page.data().getLast().content().getFirst().textContentBlock().get().text()._value().toString();


            JsonField<List<MessageContent>> content = page.data().getLast()._content();
            return page.data().getLast().content().getFirst().toString(); // todo: changed this, check later
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e);
        }
    }

}
