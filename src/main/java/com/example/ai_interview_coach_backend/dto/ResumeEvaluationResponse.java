package com.example.ai_interview_coach_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEvaluationResponse {

    private String quality;
    private int score;
    private String message;
    private List<String> suggestions;
    private List<String> questions;
}