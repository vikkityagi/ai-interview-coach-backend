package com.example.ai_interview_coach_backend.service;

import com.example.ai_interview_coach_backend.dto.ResumeAnalysisResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String model;

    public List<String> generateInterviewQuestions(String resumeText) throws Exception {

        if (!isResume(resumeText)) {
            throw new RuntimeException("Uploaded file is not a valid resume");
        }

        String prompt = """
                You are a senior technical interviewer.

                Generate EXACTLY 40 technical interview questions based on this resume.

                STRICT RULES:
                1. Return ONLY valid JSON
                2. No explanation
                3. No intro text
                4. No footer text
                5. No markdown formatting

                Required JSON format:
                {
                  "questions": [
                    "Question 1",
                    "Question 2",
                    "Question 3"
                  ]
                }

                Resume:
                """ + resumeText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                groqApiUrl,
                entity,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode groqResponse = objectMapper.readTree(response.getBody());

        String aiContent = groqResponse
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        System.out.println("AI RAW RESPONSE = " + aiContent);

        if (aiContent.startsWith("```json")) {
            aiContent = aiContent
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        JsonNode aiJson = objectMapper.readTree(aiContent);

        List<String> questions = new ArrayList<>();

        for (JsonNode question : aiJson.path("questions")) {
            questions.add(question.asText());
        }

        return questions;
    }

    public boolean isResume(String text) throws JsonMappingException, JsonProcessingException {
        String prompt = """
                                Determine whether this document is a professional resume.

                                Rules:
                                - Answer ONLY YES or NO

                                Document:
                """ + text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                groqApiUrl,
                entity,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode groqResponse = objectMapper.readTree(response.getBody());

        String aiContent = groqResponse
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim()
                .toLowerCase();

        return aiContent.equals("yes");
    }

    public ResumeAnalysisResponse analyzeResume(String resumeText) throws Exception {

        String prompt = """
                You are an expert resume reviewer.

                Analyze this resume.

                Evaluate:
                1. Resume structure
                2. Technical skills relevance
                3. Project descriptions
                4. Achievements
                5. Professional presentation
                6. Overall interview readiness

                Rules:
                - Score between 1 to 10
                - If score < 6, quality = POOR
                - If score >= 6, quality = GOOD
                - Return ONLY valid JSON
                - No explanation outside JSON

                Required JSON format:

                {
                  "quality": "GOOD",
                  "score": 8,
                  "message": "Resume looks strong",
                  "suggestions": [
                    "Improve project achievements",
                    "Add measurable results"
                  ]
                }

                Resume:
                """ + resumeText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        messages.add(message);

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                groqApiUrl,
                entity,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode groqResponse = objectMapper.readTree(response.getBody());

        String aiContent = groqResponse
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        aiContent = aiContent.trim();

        if (aiContent.startsWith("```")) {
            aiContent = aiContent
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();
        }

        JsonNode aiJson = objectMapper.readTree(aiContent);

        String quality = aiJson.path("quality").asText();
        int score = aiJson.path("score").asInt();
        String responseMessage = aiJson.path("message").asText();

        List<String> suggestions = new ArrayList<>();

        for (JsonNode suggestion : aiJson.path("suggestions")) {
            suggestions.add(suggestion.asText());
        }

        return new ResumeAnalysisResponse(
                quality,
                score,
                responseMessage,
                suggestions);
    }

}