package com.example.ai_interview_coach_backend.controller;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.ai_interview_coach_backend.dto.InterviewQuestionResponse;
import com.example.ai_interview_coach_backend.dto.ResumeAnalysisResponse;
import com.example.ai_interview_coach_backend.dto.ResumeEvaluationResponse;
import com.example.ai_interview_coach_backend.dto.ResumeUploadResponse;
import com.example.ai_interview_coach_backend.service.AiService;
import com.example.ai_interview_coach_backend.service.PdfService;

@RestController
@CrossOrigin
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final PdfService pdfService;
    private final AiService aiService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeEvaluationResponse> uploadResume(
            @RequestParam("file") MultipartFile file) throws Exception {

        String resumeText = pdfService.extractText(file);

        ResumeAnalysisResponse analysis = aiService.analyzeResume(resumeText);

        if ("POOR".equals(analysis.getQuality())) {
            ResumeEvaluationResponse response =
                    new ResumeEvaluationResponse(
                            analysis.getQuality(),
                            analysis.getScore(),
                            analysis.getMessage(),
                            analysis.getSuggestions(),
                            Collections.emptyList()
                    );

            return ResponseEntity.ok(response);
            
        }

        List<String> questions = aiService.generateInterviewQuestions(resumeText);

        ResumeEvaluationResponse response =
                new ResumeEvaluationResponse(
                        analysis.getQuality(),
                        analysis.getScore(),
                        analysis.getMessage(),
                        Collections.emptyList(),
                        questions
                );


        return ResponseEntity.ok(response);
    }

}