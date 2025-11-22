package org.TBFV4R.llm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.TBFV4R.utils.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.server.ExportException;
import java.util.Map;

public class Model {
    private final ModelConfig config;
    private final String promptCode2IFSF;
    private final String promptIFSF2FSF;
    ObjectMapper mapper = new ObjectMapper();
    public Model(){
        mapper = new ObjectMapper();
        try {
            config = mapper.readValue(new File("ModelConfig.json"), ModelConfig.class);
            if("YOUR_OPENAI_API_KEY".equals(config.getApiKey())) throw new Error("Please replace YOUR_OPENAI_API_KEY with your APIKey");
        } catch (IOException e) {
            throw new Error("Missing ModelConfig.json, please rename ModelConfigExample.json to ModelConfig.json");
        }
        try {
            this.promptCode2IFSF= FileUtil.readLinesAsString("resources/prompts/Code2IFSF.txt","\n");
        } catch (IOException e) {
            throw new Error("resources/prompts/Code2IFSF.txt not found!");
        }
        try {
            this.promptIFSF2FSF= FileUtil.readLinesAsString("resources/prompts/IFSF2FSF.txt","\n");
        } catch (IOException e) {
            throw new Error("resources/prompts/IFSF2FSF.txt not found!");
        }
    }
    public String predict(String input){
        String requestBody = null;
        try {
            requestBody = mapper.writeValueAsString(Map.of(
                    "model", config.getModel(),
                    "input", input,
                    "reasoning", config.getReasoning()
            ));
        } catch (JsonProcessingException ignore) {

        }
        String apiKey = config.getApiKey();

        var client = HttpClient.newHttpClient();
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
        } catch (URISyntaxException ignore) {

        }

        HttpResponse<String> response = null;
        int attempt = 0;

        while (attempt < config.getMaxRetries()) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt >= config.getMaxRetries()) {
                    throw new RuntimeException("Network Error! All retries failed.");
                }
                System.out.println("Network Error! Retrying... (" + attempt + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted!");
                }
            }
        }
        JSONObject json = new JSONObject(response.body());

        StringBuilder messageText = new StringBuilder();
        JSONArray outputArray = json.getJSONArray("output");

        for (int i = 0; i < outputArray.length(); i++) {
            JSONObject outputObj = outputArray.getJSONObject(i);
            if ("message".equals(outputObj.getString("type"))) {
                JSONArray contentArray = outputObj.getJSONArray("content");
                for (int j = 0; j < contentArray.length(); j++) {
                    JSONObject contentObj = contentArray.getJSONObject(j);
                    if (contentObj.has("text")) {
                        messageText.append(contentObj.getString("text"));
                    }
                }
            }
        }

        return messageText.toString();
    }
    public String code2IFSF(String code) {
        String text = (this.predict(promptCode2IFSF+"\n"+code));
        return text;
    }

    public String IFSF2FSF(String IFSF) {
        String text = (this.predict(promptIFSF2FSF+"\n"+IFSF));
        return text;
    }

    public String file2IFSF(String filename) {
        try {
            String text = (this.predict(promptCode2IFSF+"\n"+FileUtil.readLinesAsString(filename,"\n")));
            return text;
        } catch (IOException e) {
            throw new RuntimeException(filename+" not found!");
        }
    }
}
