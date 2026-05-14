package com.example.ai_interview_coach_backend.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadResponse {

    private String fileName;
    private String resumeText;
    private String message;
}