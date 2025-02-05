# PeopleCodeOpenAI
## Overview

This is a high-level Java library for accessing OpenAI. The target audience is novice Java programmers.

OpenAI has provided a Beta Java API that is constantly being added to with additional features to make it easier to leverage the benefits of their models. This library seeks to extend the accessiblity of OpenAI's benefits to novice Java programmers so that they can learn how to use the tools and benefit rather than be burdened with knowing how these tools work. 

The REST API documentation can be found on [platform.openai.com](https://platform.openai.com/docs). Javadocs are also available on [javadoc.io](https://javadoc.io/doc/com.openai/openai-java/0.0.1).

## Installation

Once this repository is cloned, you must have an OpenAI Developer account [API Login](https://platform.openai.com/docs/overview). 
This is required so that an API key can be generated and OpenAI assistants can be created if desired.

If using `IntelliJIDEA`, an environment variable can be set up by going to Run>Edit Configurations and creating a new configuration [see more on IntelliJIDEA environment variables](https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html)

Once an environment variable has been configured, enter the label entered that corresponds to the API key entered as an environment variable in the `.getenv()` method

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

Other options for ChatModel are

```GPT_4O```, ```GPT_3_5_TURBO```, ```O1```, and ```O1_MINI```

All other model options can be viewed at their [ChatModel Class JavaDoc](https://javadoc.io/doc/com.openai/openai-java/latest/com/openai/models/ChatModel.html)

Once a ```OpenAIConversation``` object has been created, String responses (either from OpenAI's chat completion or from an already created Assistant) can be generated.

```java
String apiKey = System.getenv("OPENAI_API_KEY");

OpenAIConversation conversation = new OpenAIConversation(apiKey, ChatModel.GPT_4O_MINI);

String response = conversation.askQuestion("You are a film expert, be snobby", "What are the three best Quentin Tarantino movies?", "asst_v1IzCzUzPPPdpAgyz5xoV5y0");
```





