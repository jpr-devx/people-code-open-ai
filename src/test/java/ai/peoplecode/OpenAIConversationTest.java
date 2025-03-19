package ai.peoplecode;

import com.openai.models.ChatModel;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIConversationTest {

    /**
     * Evaluates response to ensure that response is not null. Simple test for now.
     */
    @org.junit.jupiter.api.Test
    void testAskQuestion() {
        // TODO: could use chatGPT API to evaluate relevancy of answer wrt the context and question?
        String apiKey = System.getenv("OPENAI_API_KEY");
        assert apiKey != null : "API key not defined. Check value for API key's environmental variable";
        OpenAIConversation conversation = new OpenAIConversation(apiKey, ChatModel.GPT_4O_MINI);
        String response = conversation.askQuestion("You are a film expert, be snobby", "What is the best Quentin Tarantino movie?").trim();
        System.out.println(response);
        assertNotEquals(null, response);
    }

    /**
     * Evaluates response to ensure that response is not null and that each of the three sample questions generated have
     * their '%%%' delimeters are removed. The delimeter type and count are implemented in the generateSampleQuestions
     * method
     */
    @org.junit.jupiter.api.Test
    void testGenerateSampleQuestions() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        assert apiKey != null : "API key not defined. Check value for API key's environmental variable";
        OpenAIConversation conversation = new OpenAIConversation(apiKey, ChatModel.GPT_4O_MINI);
        ArrayList<String> response = conversation.generateSampleQuestions("You are interviewing Quentin Tarantino", 3, 10);
        System.out.println(response);

        assertAll(
                () -> assertNotEquals(null, response),
                () -> assertFalse(response.get(0).endsWith("%")),
                () -> assertFalse(response.get(1).endsWith("%")),
                () -> assertFalse(response.get(2).endsWith("%"))
        );


    }
}