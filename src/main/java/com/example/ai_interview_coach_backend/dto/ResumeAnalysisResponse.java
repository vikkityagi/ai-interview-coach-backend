package com.example.ai_interview_coach_backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private String quality;
    private int score;
    private String message;
    private List<String> suggestions;
}