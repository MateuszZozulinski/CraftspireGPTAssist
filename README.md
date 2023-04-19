# Craftspire GPT Assist 

This is Intellij Idea plugin using OpenAI-Java API(https://github.com/TheoKanning/openai-java) to integrate with OpenAI's GPT apis. It supports multiple different GPT API's and may be tuned to user needs at any time.

## Initial Configuration
1. Request an OpenAI API key (https://platform.openai.com/account/api-keys) or use existing one.
2. Setup the key in Intellij Idea Properties (Settings -> GPT Code Assist -> OpenAI API Key) - the key is stored in secure format, so there will be no option to retrieve it later
3. Use the plugin

## Additional Options
Besides API Key configuration the plugin supports additional configuration options:
* Temperature - value between 0 and 2, GPT temperature parameter, which impacts how focused and deterministic the response is
* GPT model - one of OpenAI models - only Chat models are supported (https://platform.openai.com/docs/models)
