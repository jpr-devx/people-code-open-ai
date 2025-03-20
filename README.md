# PeopleCodeOpenAI
## Overview

This is a high-level Java library for accessing OpenAI. The target audience is novice Java programmers.

OpenAI has provided a Beta Java API that is constantly being added to with additional features to make it easier to 
leverage the benefits of their models. This library seeks to extend the accessiblity of OpenAI's benefits to novice 
Java programmers so that they can learn how to use the tools and benefit rather than be burdened with knowing how these 
tools work. 

The REST API documentation can be found on [platform.openai.com](https://platform.openai.com/docs). Javadocs are also available on [javadoc.io](https://javadoc.io/doc/com.openai/openai-java/0.0.1).

## Installation

Once this repository is cloned, you must have an OpenAI Developer account [API Login](https://platform.openai.com/docs/overview). 
This is required so that an API key can be generated and OpenAI assistants can be created if desired.

If using `IntelliJIDEA`, an environment variable can be set up by going to Run>Edit Configurations and creating a new 
configuration [see more on IntelliJIDEA environment variables](https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html)

Once an environment variable has been configured, enter the label entered that corresponds to the API key entered as an 
environment variable in the `.getenv()` method

Ensure JDK 23 is being used

```java
String apiKey = System.getenv("OPENAI_API_KEY");
```

### Starting the conversation

Use the OpenAIConversation constructor to create an object for the conversation to take place. 

```java
public OpenAIConversation(String apiKey, ChatModel modelName) {
        this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
        this.modelName = modelName;
        this.conversationThread = this.client.beta().threads().create(BetaThreadCreateParams.builder().build());
        this.conversationHistory = new ArrayList<>();
    }
```

Rather than a string model name, we use the constants that OpenAI's API provides through their ChatModel class

```java
import com.openai.models.ChatModel;

ChatModel modelName = ChatModel.GPT_4O_MINI;
```

GPT_40_MINI is the most cost effective option, so it is great for testing your code. <br>
Other options for ChatModel are

```GPT_4O```, ```GPT_3_5_TURBO```, ```O1```, and ```O1_MINI```

All other model options can be viewed at their [ChatModel Class JavaDoc](https://javadoc.io/doc/com.openai/openai-java/latest/com/openai/models/ChatModel.html)

Once an **OpenAIConversation** object has been created, String responses (either from OpenAI's chat completion or from 
an already created Assistant) can be generated.

```java
String apiKey = System.getenv("OPENAI_API_KEY");
String assistantID = System.getenv("ASSISTANT1");

OpenAIConversation conversation = new OpenAIConversation(apiKey, ChatModel.GPT_4O_MINI);

String response = conversation.askQuestion("You are a film expert, be snobby", "What are the three best Quentin Tarantino movies?", assistantID);
```

#### Structured Output

In the code snippet below, we use OpenAI's structured output to put the LLM's response in stricter structure. When requesting
a response with a given structure through normal chat completion, there can be variation in the form that the response 
is in from answer to answer. Structured outputs limits this variation with the **schema** we give it.

```
    Map<String, Map<String, String>> properties = new HashMap<>();
    properties.put("n", Map.of(
                                "type", "number",
                                "description", "The number of questions to be generated"));

    properties.put("m", Map.of(
                            "type", "number",
                            "description", "The maximum word count allowed for each question"));

    properties.put("questions", Map.of("type", "string",
                                    "description", "A string containing questions separated by '%%%."));


    JsonSchema.Schema schema = JsonSchema.Schema.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(properties))
            .build();
```

Above we give three parameters:
<ul>
    <li>n - an integer (number in OpenAI's semantics) for how many questions we want generated</li>
    <li>m - an integer (number in OpenAI's semantics) for how many words we want in each question</li>
    <li>questions - a string that represents a question to be generated</li>
</ul>

Notice that each 'property' is itself a Map, and it put into another Map that is turned into a JsonValue with 
```JsonValue.from(Object value)```

Since structured output is used, the response comes back as JSON. Now I need to deserialize it. 

If we were to ask the question "What are iconic films from the 1960s?" with 3 questions at a max word length of 10 and 
we split be delimiter we would get the following: <br>
If I don't this is what result will be returned as:<br><br>
result[0] : {"questions":"What are iconic films from the 1960s?<br>
result[1] : Who directed 'Psycho' in the 1960s?<br>
result[2] : Which actress starred in 'Breakfast at Tiffany's'?","m":10,"n":3}<br>
<br>
I need to get rid of everything besides the value for "questions". To do this I use the jackson library to read the JSON 
and get the value for 'questions'. We need to encapsulate this code in a try/catch block in the event that there is a 
JSON parsing error
```
    ObjectMapper objectMapper = new ObjectMapper();
    String rawQuestions;
    try {
        JsonNode rootNode = objectMapper.readTree(rawResult.toString());
        rawQuestions = rootNode.get("questions").asText();
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Error occurred in parsing JSON holding sample questions");
    }

    ArrayList<String> result = new ArrayList<>(Arrays.asList(rawQuestions.split("%{3}")));
```





