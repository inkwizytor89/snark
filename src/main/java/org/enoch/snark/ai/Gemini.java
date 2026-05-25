package org.enoch.snark.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.expression.function.AbstractSpelFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Gemini {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public List<String> transformToSpellFunctions(String message) {
        String prompt = buildPrompt(message);
        String answer = extractContent(call(prompt));
        List<AstExpression> astList = new Gson().fromJson(answer, new TypeToken<List<AstExpression>>(){}.getType());
        final AstToSpelConverter astExecutor = new AstToSpelConverter();
        return astList.stream().map(astExecutor::toSpel).toList();
    }

    private String buildPrompt(String userMessage) {
        return "You can only use the following functions.\n" +
                new Gson().toJson(AbstractSpelFunction.definitions())+"\n" +
                "Return ONLY JSON in this format:\n" +
                "                   [ \n" +
                "                    {\n" +
                "                      \"function\": \"...\",\n" +
                "                      \"args\": {\n" +
                "                        ...\n" +
                "                      }\n" +
                "                    },\n" +
                "                  ...\n" +
                "             ]\n" +
                "Message to transform: " + userMessage;
    }


    private String call(String prompt) {
        if(StringUtils.isEmpty(geminiApiKey)) return "missing geminiApiKey to call AI";
        try {

            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonArray partsArray = new JsonArray();
            partsArray.add(textPart);

            JsonObject content = new JsonObject();
            content.add("parts", partsArray);

            JsonArray contentsArray = new JsonArray();
            contentsArray.add(content);

            JsonObject requestBodyJson = new JsonObject();
            requestBodyJson.add("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    gson.toJson(requestBodyJson),
                    MediaType.get("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            String responseBody = response.body().string();

            System.out.println(responseBody);

            if (response.isSuccessful()) {

                JsonObject jsonResponse =
                        gson.fromJson(responseBody, JsonObject.class);

                return jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text")
                        .getAsString();

            } else {

                System.out.println("API ERROR:");
                System.out.println(responseBody);

                return "unknown";
            }

        } catch (Exception e) {

            e.printStackTrace();
            return "unknown";
        }
    }

    private String extractContent(String input) {
        return input.replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "");
    }
}
