package com.example.demo.controller;

import com.example.demo.service.VoiceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class VoiceController {

    private final VoiceService voiceService;

    public VoiceController(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response sendVoice(@RequestParam("audio") MultipartFile audio) throws IOException {
        String text = voiceService.convertSpeechToText(audio);
        String aiResponse = voiceService.sendToGemini(text);
        return new Response(aiResponse);
    }

    static class Response {
        private String text;
        public Response(String text) { this.text = text; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}