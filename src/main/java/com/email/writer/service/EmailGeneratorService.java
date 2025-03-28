package com.email.writer.service;

import com.email.writer.DTO.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    private String geminiApiUrl="";
    private String apiKey="";

    public EmailGeneratorService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateEmailReply(EmailRequest emailRequest) throws JsonProcessingException {
        //build the prompt
        //craft reqquest get response
        String prompt=buildPrompt(emailRequest);
        Map<String ,Object> requestBody=Map.of("contents",new Object[]{
                Map.of("parts",new Object[]{
                        Map.of("text",prompt)
                })
        });

        String response=webClient.post()
                .uri(geminiApiUrl+apiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractEmailResponse(response);
    }

    private String extractEmailResponse(String response) throws JsonProcessingException {
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            JsonNode jsonNode=objectMapper.readTree(response);
            return jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        }
        catch (Exception e){
            return "Error generating email response";
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("Generate a professional email response  Don't generate the subject.\n\n");
        if (emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()){
            prompt.append("Tone should be: "+emailRequest.getTone()+"\n\n");
        }
        prompt.append("Original Email : "+emailRequest.getEmailContent());
        return prompt.toString();
    }
}
