package com.example.aeroalarm.ai;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiAlarmController {
    private final GenerativeModelFutures modelFutures;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public interface AiCallback {
        void onSuccess(AlarmAction action);
        void onError(Throwable throwable);
    }

    public GeminiAlarmController(String apiKey) {
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                apiKey
        );
        this.modelFutures = GenerativeModelFutures.from(gm);
    }

    public void parseUserCommand(String userPrompt, AiCallback callback) {
        String prompt = "You are an AI assistant for 'AeroAlarm', a feature-rich native Android alarm clock app. " +
                "Parse the user's natural language command into a JSON object with the following fields:\n" +
                "- actionType: one of [SET_ALARM, SNOOZE, DELETE_ALARM, LIST_ALARMS, SWITCH_TAB, SET_WALLPAPER, GET_SLEEP_STATS, UNKNOWN]\n" +
                "- hour: integer (1-12)\n" +
                "- minute: integer (0-59)\n" +
                "- second: integer (0-59)\n" +
                "- ampm: \"AM\" or \"PM\"\n" +
                "- label: string (e.g. \"Gym\", \"Meeting\", \"Dinner time\")\n" +
                "- repeatDays: array of integers (0=Sun, 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat)\n" +
                "- targetTab: string if SWITCH_TAB (one of \"alarm\", \"world_clock\", \"timer\", \"stopwatch\")\n" +
                "- wallpaperTheme: string if SET_WALLPAPER (one of \"black\", \"navy\", \"purple\", \"emerald\")\n\n" +
                "User Command: \"" + userPrompt + "\"\n\n" +
                "Return ONLY valid JSON with no markdown formatting.";

        Content content = new Content.Builder().addText(prompt).build();

        com.google.common.util.concurrent.ListenableFuture<com.google.ai.client.generativeai.type.GenerateContentResponse> future =
                modelFutures.generateContent(content);

        com.google.common.util.concurrent.Futures.addCallback(
                future,
                new com.google.common.util.concurrent.FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
                    @Override
                    public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                        try {
                            String jsonText = result.getText().trim();
                            if (jsonText.startsWith("```json")) {
                                jsonText = jsonText.substring(7);
                            }
                            if (jsonText.startsWith("```")) {
                                jsonText = jsonText.substring(3);
                            }
                            if (jsonText.endsWith("```")) {
                                jsonText = jsonText.substring(0, jsonText.length() - 3);
                            }
                            jsonText = jsonText.trim();

                            JsonObject jsonObj = JsonParser.parseString(jsonText).getAsJsonObject();
                            
                            AlarmAction.ActionType type = AlarmAction.ActionType.valueOf(
                                    jsonObj.has("actionType") ? jsonObj.get("actionType").getAsString() : "SET_ALARM"
                            );
                            int hour = jsonObj.has("hour") ? jsonObj.get("hour").getAsInt() : 7;
                            int minute = jsonObj.has("minute") ? jsonObj.get("minute").getAsInt() : 0;
                            int second = jsonObj.has("second") ? jsonObj.get("second").getAsInt() : 0;
                            String ampm = jsonObj.has("ampm") ? jsonObj.get("ampm").getAsString() : "AM";
                            String label = jsonObj.has("label") ? jsonObj.get("label").getAsString() : "Alarm";

                            List<Integer> days = new ArrayList<>();
                            if (jsonObj.has("repeatDays") && jsonObj.get("repeatDays").isJsonArray()) {
                                for (com.google.gson.JsonElement el : jsonObj.getAsJsonArray("repeatDays")) {
                                    days.add(el.getAsInt());
                                }
                            }

                            String targetTab = jsonObj.has("targetTab") ? jsonObj.get("targetTab").getAsString() : "alarm";
                            String wallpaperTheme = jsonObj.has("wallpaperTheme") ? jsonObj.get("wallpaperTheme").getAsString() : "black";

                            AlarmAction action = new AlarmAction(type, hour, minute, second, ampm, label, days, targetTab, wallpaperTheme);
                            callback.onSuccess(action);
                        } catch (Exception e) {
                            AlarmAction fallbackAction = parseWithRegex(userPrompt);
                            callback.onSuccess(fallbackAction);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        AlarmAction fallbackAction = parseWithRegex(userPrompt);
                        callback.onSuccess(fallbackAction);
                    }
                },
                executor
        );
    }

    private AlarmAction parseWithRegex(String prompt) {
        String lower = prompt.toLowerCase();
        int hour = 7;
        int minute = 0;
        String ampm = "AM";
        String label = "AI Alarm";

        Pattern timePat = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePat.matcher(lower);
        if (matcher.find()) {
            try {
                hour = Integer.parseInt(matcher.group(1));
                if (matcher.group(2) != null) {
                    minute = Integer.parseInt(matcher.group(2));
                }
                if (matcher.group(3) != null) {
                    ampm = matcher.group(3).toUpperCase();
                } else if (lower.contains("pm") && hour < 12) {
                    ampm = "PM";
                }
            } catch (Exception ignored) {}
        }

        if (lower.contains("gym")) label = "Gym";
        else if (lower.contains("workout")) label = "Workout";
        else if (lower.contains("meeting")) label = "Meeting";
        else if (lower.contains("study")) label = "Study";
        else if (lower.contains("wake up")) label = "Wake Up";

        return new AlarmAction(AlarmAction.ActionType.SET_ALARM, hour, minute, 0, ampm, label, new ArrayList<>(), "alarm", "black");
    }
}
