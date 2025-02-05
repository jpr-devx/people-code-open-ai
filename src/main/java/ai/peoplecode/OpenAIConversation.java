package ai.peoplecode;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;
import com.openai.models.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

/**
 * Class built on OpenAI's Beta Java API
 * @see <a href="https://github.com/openai/openai-java">OpenAI Java API GitHub Repository</a>
 * @see <a href="https://javadoc.io/doc/com.openai/openai-java/latest/index.html/">OpenAI Java API JavaDoc</a>
 */
public class OpenAIConversation {
    protected OpenAIClient client;
    protected final ChatModel modelName;
    protected Thread conversationThread;
    protected ArrayList<String> conversationMessages;
    private ChatCompletionCreateParams.Builder conversationMemory;

    /**
     * OpenAIConversation constructor
     * @param apiKey OpenAI API Key <a href="https://platform.openai.com/settings/organization/api-keys">generate apiKey here</a>
     * @param modelName OpenAI Chat Model (e.g. ChatModel.GPT_4O_MINI, ChatModel.GPT_4O, ChatModel.GPT_3_5_TURBO)
     * @apiNote ChatModel.GPT_4O_MINI as the is advised for development due to its high TPM <a href="https://platform.openai.com/docs/guides/rate-limits">see rate table</a>
     */
    public OpenAIConversation(String apiKey, ChatModel modelName) {
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
        this.modelName = modelName;
        this.conversationThread = this.client.beta().threads().create(BetaThreadCreateParams.builder().build());
        this.conversationMessages = new ArrayList<>();
        this.conversationMemory = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .maxCompletionTokens(2048);
    }

    /**
     * Returns OpenAI model response to a user text
     * @param context Developer message to OpenAI's ChatCompletion API <a href="https://github.com/openai/openai-java/blob/main/openai-java-example/src/main/java/com/openai/example/CompletionsExample.java">see example</a> to instruct model on how to answer
     * @param question User message to ask model
     * @return Model's response
     */
    public String askQuestion(String context, String question) {

        this.conversationMemory
                .addMessage(ChatCompletionDeveloperMessageParam.builder()
                        .role(ChatCompletionDeveloperMessageParam.Role.DEVELOPER)
                        .content(context)
                        .build())
                .addMessage(ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(question)
                        .build());

        ChatCompletionMessage message = this.client.chat().completions().create(this.conversationMemory.build()).choices().getFirst().message();

        String result;

        try {
            result = message.content().get().toString();
            this.conversationMessages.add("UserMessage: " + question);
            this.conversationMessages.add("AiMessage: " + result);
        } catch (NoSuchElementException error){
            throw new NoSuchElementException("Error in chat completion, no message was extracted.");
        }

        return result;
    }

    /**
     * Returns OpenAI assistant response to user text, requires assistantId corresponding to an already created assistant
     * @param context Additional instruction set sent to assistant to instruct assistant on how to answer
     * @param question User message to ask assistant
     * @param assistantId ID (beginning in asst_) corresponding to OpenAI assistant <a href="https://platform.openai.com/assistants/">Create assistant here</a>
     * @return Assistant's response
     */
    public String askQuestion(String context, String question, String assistantId){

        // Updates the assistant with the passed in modelName param in case there is a difference between param and currently selected model
        Assistant assistant = this.client.beta()
                .assistants()
                .update(BetaAssistantUpdateParams.builder()
                        .assistantId(assistantId)
                        .model(modelName._value())
                        .build());

        this.client.beta()
                .threads()
                .messages()
                .create(BetaThreadMessageCreateParams.builder()
                        .threadId(this.conversationThread.id())
                        .role(BetaThreadMessageCreateParams.Role.USER)
                        .content(question)
                        .build());
        try {
            Helper.runThread(this, assistant, context);
        } catch (Exception error) {
            throw new RuntimeException("Something went wrong in running thread.");
        }
        String message = Helper.getMessage(this);
        this.conversationMessages.add("UserMessage: " + question);
        this.conversationMessages.add("AiMessage: " + message);
        return message;
    }

    /**
     * Returns ArrayList of sample questions
     * @param context Developer message to OpenAI's ChatCompletion API <a href="https://github.com/openai/openai-java/blob/main/openai-java-example/src/main/java/com/openai/example/CompletionsExample.java">see example</a> to instruct model on how to formulate sample questions
     * @param count Number of sample questions to be generated
     * @param maxWords Maximum number of words each sample question to be limited to
     * @return ArrayList of sample questions
     */
    public ArrayList<String> generateSampleQuestions(String context, int count, int maxWords){

        this.conversationMemory
                .addMessage(ChatCompletionDeveloperMessageParam.builder()
                        .role(ChatCompletionDeveloperMessageParam.Role.DEVELOPER)
                        .content("Please provide" + count + " sample questions with '%%%' as the delimiter between " +
                                "questions and omit any numbering of questions. Provide nothing else " +
                                "but your sample questions. Ensure the maximum length of each question is " +
                                maxWords + " words long.")
                        .build())
                .addMessage(ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(context)
                        .build());

        ChatCompletionMessage message = this.client.chat().completions().create(this.conversationMemory.build()).choices().getFirst().message();

        try {
            ArrayList<String> unrefinedMessages = new ArrayList(Arrays.asList(message.content().get().split("%{3}")));
            ArrayList<String> messages = new ArrayList<>();
            unrefinedMessages.forEach(sampleQuestion -> {messages.add(sampleQuestion.trim());}); // Note: I did the .forEach to make things concise, should I do a for loop?
            this.conversationMessages.add("UserMessage: " + context);
            this.conversationMessages.add("AiMessage: " + messages.toString());
            return messages;
        } catch (NoSuchElementException error) {
            throw new NoSuchElementException("Error in getting messages");
        }
    }

    /**
     * Returns ArrayList of sample questions
     * @param context Additional instruction set sent to assistant to instruct assistant on how to answer
     * @param count Number of sample questions to be generated
     * @param maxWords Maximum number of words each sample questions to be generated
     * @param assistantId ID (beginning in "asst_") corresponding to OpenAI assistant <a href="https://platform.openai.com/assistants/">Create assistant here</a>
     * @return ArrayList of sample questions
     */
    public ArrayList<String> generateSampleQuestions(String context, int count, int maxWords, String assistantId){

        Assistant assistant = this.client.beta()
                .assistants()
                .retrieve(BetaAssistantRetrieveParams.builder().assistantId(assistantId).build());

        this.client.beta()
                .threads()
                .messages()
                .create(BetaThreadMessageCreateParams.builder()
                        .threadId(this.conversationThread.id())
                        .role(BetaThreadMessageCreateParams.Role.USER)
                        .content(context)
                        .build());
        try {
            Helper.runThread(this,
                             assistant,
                    "Please provide" + count + " sample questions with '%%%' as the delimiter between " +
                             "questions and omit any numbering of questions. Provide nothing else " +
                             "but your sample questions. Ensure the maximum length of each question is " +
                             maxWords + " words long.");
            String message = Helper.getMessage(this);
            ArrayList<String> unrefinedMessages = new ArrayList(Arrays.asList(message.split("%{3}")));
            ArrayList<String> messages = new ArrayList<>();
            unrefinedMessages.forEach(sampleQuestion -> {messages.add(sampleQuestion.trim());}); // Note: I did the .forEach to make things concise, should I do a for loop?
            return messages;
        } catch (Exception error) {
            throw new RuntimeException("Something went wrong in generating sample questions");
        }
    }

    /**
     * Clears conversationHistory ArrayList data member
     */
    public void reset(){
        this.conversationMessages.clear();
        this.conversationThread = this.client.beta().threads().create(BetaThreadCreateParams.builder().build());
    }

    /**
     * Provides String representation of OpenAIConversation object
     * @return String representation of conversationHistory ArrayList
     */
    @Override
    public String toString(){
        return this.conversationMessages.toString();
    }

    public static void main(String[] args){

        String apiKey = System.getenv("OPENAI_API_KEY");
        String assistantID = System.getenv("ASSISTANT1");

//        // Example conversation
        OpenAIConversation conversation = new OpenAIConversation(apiKey, ChatModel.GPT_4O_MINI);
//
//        // Generate sample questions
        ArrayList<String> questions = conversation.generateSampleQuestions("Questions about films in the 1960s", 3, 10);
        System.out.println("Sample questions: " + questions);

        // Note: it seems like the context that's injected in the generateSampleQuestions method (insert %%% as delimeter)
        //  persists for the askQuestion method call below, even though a new developer message is added
        // Ask a question
//        String response = conversation.askQuestion("You are a film expert, be snobby", "What are the three best Quentin Tarantino movies?");
        String response = conversation.askQuestion("You are a film expert, be snobby", "What is the best Quentin Tarantino movie?");
        System.out.println("Response: " + response);

        // Ask another question to show continuation-- openAI knows 'he' is Tarantino from memory
        response = conversation.askQuestion("You are a film expert, be snobby", "How old is he");
        System.out.println("Response: " + response);

        // Print conversation history
        System.out.println("\nConversation History:");
        System.out.println(conversation);

        conversation.askQuestion("You are a film expert", "What are your top three Christopher Nolan films?");
        conversation.askQuestion("You are a film expert", "How old is the director?");

        System.out.println(conversation.askQuestion("You are a film expert", "What are your top three Christopher Nolan films?"));
        System.out.println(conversation.askQuestion("You are a film expert", "How old is the director?"));

//        conversation.test();

        System.out.println(conversation);
    }
}
