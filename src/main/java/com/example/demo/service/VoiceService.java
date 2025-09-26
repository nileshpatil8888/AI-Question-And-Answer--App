package com.example.demo.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class VoiceService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key";

    private final RestTemplate restTemplate = new RestTemplate();

    public String convertSpeechToText(MultipartFile audio) throws IOException {
        String jsonPath = "C:\\Users\\niles\\Downloads\\springboot_gemini_audio\\src\\main\\resources\\cred.json";
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        try (SpeechClient speechClient = SpeechClient.create(settings)) {

            ByteString audioBytes = ByteString.copyFrom(audio.getBytes());

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(16000)
                    .build();

            RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response = speechClient.recognize(config, recognitionAudio);
            StringBuilder transcript = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                transcript.append(result.getAlternativesList().get(0).getTranscript());
            }

            return transcript.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error converting audio to text";
        }
    }

    public String sendToGemini(String text) {
        String payload = "{\"contents\": [{\"parts\": [{\"text\": \"" + text + "\"}], \"role\": \"user\"}]}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL, HttpMethod.POST, entity, String.class
        );
        return response.getBody();
    }
}