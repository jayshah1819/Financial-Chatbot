package com.chatBot.Chatboot;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AIChatbot {
    // Replace this with your OpenAI API Key
    private static final String API_KEY = "OEZGW095HRKWGPE4";
    private static final String ALPHA_VANTAGE_API_KEY = "YOUR_ALPHA_VANTAGE_API_KEY";

    // Map common company names to their ticker symbols
    private static final Map<String, String> COMPANY_TO_TICKER = new HashMap<>();
    static {
        COMPANY_TO_TICKER.put("apple", "AAPL");
        COMPANY_TO_TICKER.put("google", "GOOGL");
        COMPANY_TO_TICKER.put("alphabet", "GOOGL");
        COMPANY_TO_TICKER.put("microsoft", "MSFT");
        COMPANY_TO_TICKER.put("amazon", "AMZN");
        COMPANY_TO_TICKER.put("tesla", "TSLA");
        COMPANY_TO_TICKER.put("meta", "META");
        COMPANY_TO_TICKER.put("facebook", "META");
        COMPANY_TO_TICKER.put("netflix", "NFLX");
        COMPANY_TO_TICKER.put("nvidia", "NVDA");
        // Add more as needed
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String userInput;

        System.out.println("AI Chatbot: Hello! I am your AI-powered financial assistant. Type 'exit' to quit.");
        System.out.println("You can ask about:");
        System.out.println("1. Stock Market");
        System.out.println("2. Personal Finance");
        System.out.println("3. Investment Advice");
        System.out.println("4. Economic News");
        System.out.println("5. Cryptocurrency");
        System.out.println("6. Budgeting");
        System.out.println("7. Retirement Planning");
        System.out.println("8. Tax Strategies");
        System.out.println("9. Real Estate");
        System.out.println("10. Business Finance");

        while (true) {
            System.out.print("You: ");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("AI Chatbot: Goodbye!");
                break;
            }

            try {
                String response = getAIResponse(userInput);
                System.out.println("AI Chatbot: " + response);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("AI Chatbot: Something went wrong.");
            }
        }

        scanner.close();
    }

    public static String getAIResponse(String userInput) throws IOException {
        // Check if user asks for stock price (e.g., "price of AAPL" or "AAPL price")
        String stockSymbol = extractStockSymbol(userInput);
        if (stockSymbol != null) {
            return getStockPrice(stockSymbol);
        }

        OkHttpClient client = new OkHttpClient();
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                + API_KEY;

        // Build user message
        JSONObject userPart = new JSONObject();
        userPart.put("text", userInput);

        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        userContent.put("parts", new org.json.JSONArray().put(userPart));

        // Put into contents array
        org.json.JSONArray contents = new org.json.JSONArray().put(userContent);
        JSONObject json = new JSONObject();
        json.put("contents", contents);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(GEMINI_API_URL)
                .post(body)
                .build();

        int retries = 3;
        int waitTime = 2000;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject responseJson = new JSONObject(responseBody);
                    org.json.JSONArray contentsArr = responseJson.optJSONArray("candidates");
                    if (contentsArr != null && contentsArr.length() > 0) {
                        JSONObject candidate = contentsArr.getJSONObject(0);
                        org.json.JSONArray partsArr = candidate.getJSONObject("content").getJSONArray("parts");
                        if (partsArr.length() > 0) {
                            return partsArr.getJSONObject(0).getString("text");
                        }
                    }
                    return "[No response from Gemini API]";
                } else if (response.code() == 429) {
                    System.out.println("Rate limited (429). Retrying after " + (waitTime / 1000) + " seconds...");
                    Thread.sleep(waitTime);
                    waitTime *= 2;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    System.out.println("Error response body: " + errorBody);
                    throw new IOException("HTTP Error: " + response.code() + " - " + response.message());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        throw new IOException("Failed after retries due to rate limits or other errors.");
    }

    // Extract stock symbol from user input (very basic, e.g., "price of AAPL")
    private static String extractStockSymbol(String userInput) {
        String lower = userInput.toLowerCase();
        // Try to match company names first
        for (String company : COMPANY_TO_TICKER.keySet()) {
            if (lower.contains(company)) {
                return COMPANY_TO_TICKER.get(company);
            }
        }
        // Fallback to original logic only if no company match
        if (lower.contains("price of ")) {
            String[] parts = userInput.split("price of ", 2);
            if (parts.length > 1) {
                String symbol = parts[1].split(" ")[0].replaceAll("[^A-Za-z]", "").toUpperCase();
                if (!symbol.isEmpty())
                    return symbol;
            }
        } else if (lower.endsWith(" price")) {
            String symbol = userInput.replaceAll(" price", "").replaceAll("[^A-Za-z]", "").toUpperCase();
            if (!symbol.isEmpty())
                return symbol;
        }
        return null;
    }

    // Fetch stock price from Alpha Vantage
    private static String getStockPrice(String symbol) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey="
                + ALPHA_VANTAGE_API_KEY;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                JSONObject quote = json.optJSONObject("Global Quote");
                if (quote != null && quote.has("05. price")) {
                    String price = quote.getString("05. price");
                    return "The latest price for " + symbol + " is $" + price;
                } else {
                    return "Sorry, I couldn't find the price for " + symbol + ".";
                }
            } else {
                return "Error fetching stock price (" + response.code() + ")";
            }
        }
    }
}
