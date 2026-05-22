package com.example.savemoneytime.network;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GeminiService {

    private static final String API_KEY    = AppConfig.OPENROUTER_KEY;
    private static final String BASE_URL   = "https://openrouter.ai/api/v1/chat/completions";

    private static final String MODEL_NAME = "google/gemini-2.5-flash";

    private final OkHttpClient client;
    private static GeminiService instance;

    public static GeminiService getInstance() {
        if (instance == null) {
            instance = new GeminiService();
        }
        return instance;
    }

    private GeminiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void parseTransaction(String userInput, GeminiCallback callback) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", MODEL_NAME);
            payload.addProperty("max_tokens", 300);

            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", buildTransactionPrompt());
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", userInput);
            messages.add(userMessage);

            payload.add("messages", messages);

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://localhost")
                    .addHeader("X-Title", "BudgetMate AI")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        String responseStr = responseBody != null ? responseBody.string() : "";

                        if (!response.isSuccessful()) {
                            try {
                                JsonObject errObj = JsonParser.parseString(responseStr).getAsJsonObject();
                                if (errObj.has("error")) {
                                    JsonObject innerError = errObj.getAsJsonObject("error");
                                    if (innerError.has("message")) {
                                        callback.onError("OpenRouter: " + innerError.get("message").getAsString());
                                        return;
                                    }
                                }
                            } catch (Exception ignored) {}
                            callback.onError("Error " + response.code() + ": " + responseStr);
                            return;
                        }

                        JsonObject resObj = JsonParser.parseString(responseStr).getAsJsonObject();
                        String aiContent = resObj.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("message")
                                .get("content").getAsString();

                        callback.onSuccess(aiContent);
                    } catch (Exception e) {
                        callback.onError("Data processing error: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Payload setup error: " + e.getMessage());
        }
    }

    private String buildTransactionPrompt() {
        return "You are an English financial assistant for BudgetMate. "
                + "TASK: Determine if the user input is a TRANSACTION or a QUESTION.\n\n"
                + "RULES:\n"
                + "1. If it describes spending or earning money → return ONLY a raw JSON object (No markdown backticks, no text outside):\n"
                + "   Expense: {\"amount\": NUMBER, \"category\": \"CATEGORY\", \"note\": \"NOTE\", \"type\": \"EXPENSE\"}\n"
                + "   Income:  {\"amount\": NUMBER, \"category\": \"CATEGORY\", \"note\": \"NOTE\", \"type\": \"REVENUE\"}\n\n"
                + "2. If it's a question or greeting → return this JSON structure: {\"intent\": \"query\", \"message\": \"HELPFUL_RESPONSE_IN_ENGLISH\"}\n\n"
                + "CURRENCY RULES ($):\n"
                + "- '$6' or '6$' = 6\n"
                + "- '200$' = 200\n"
                + "- Just return the raw number, do not include the '$' symbol inside the amount field.\n\n"
                + "CATEGORY OPTIONS (Expense): Food, Transport, Housing, Health, Entertainment, Shopping, Education, Travel, Utilities, Gifts, Phone, Other\n"
                + "CATEGORY OPTIONS (Income): Salary, Freelance, Investment, Savings, Gift, Rental, Refund, Other\n\n"
                + "IMPORTANT: Do not return any explanations or formatting, return ONLY the raw JSON object string.";
    }
}