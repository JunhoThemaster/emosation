//package com.emosation.emosation.sevices;
//
//
//import com.google.api.gax.core.FixedCredentialsProvider;
//import com.google.api.gax.core.GoogleCredentialsProvider;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.language.v1.*;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Service
//public class GoogleNLPService {
//
//    private  LanguageServiceClient languageServiceClient;
//
//
//    @Value("classpath:woven-art-442312-k4-9570b94ae4da.json")
//    private InputStream GOOGLE_APPLICATION_CREDENTIALS;
//
//
//    @PostConstruct
//    public void init() throws IOException {
////        String credentialsPath = "src/main/resources/woven-art-442312-k4-9570b94ae4da.json";
//
//        GoogleCredentials credentials = GoogleCredentials.fromStream(GOOGLE_APPLICATION_CREDENTIALS);
//
//        languageServiceClient = LanguageServiceClient.create(LanguageServiceSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build());
//
//    }
//
////    /**
////     * 주어진 텍스트에 대한 감정 분석을 수행합니다.
////     *
////     * @param text 분석할 텍스트
////     * @return 텍스트의 감정 분석 결과
////     */
//
//
//
//
//    public Sentiment analyze(String text) {
//        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
//
//
//        AnalyzeSentimentRequest req = AnalyzeSentimentRequest.newBuilder().setDocument(doc).setEncodingType(EncodingType.UTF8).build();
//
//        return languageServiceClient.analyzeSentiment(req).getDocumentSentiment();
//    }
//
//
//
//    public String getResult(Sentiment sentiment) {
//
//        float score = sentiment.getScore();
//        float magnitude = sentiment.getMagnitude();
//
//        String definedEmo = defineEmo(score, magnitude);
//
//        return definedEmo;
//    }
//
//
//    private String  defineEmo(float score,float magnitude){
//
//        String result = "";
//
//        if (score < -0.7) {
//            result = handleNegativeEmotion(magnitude, "화나신 것 같아요!");
//        } else if (score < -0.4) {
//            result = handleNegativeEmotion(magnitude, "기분이 많이 상하신 것 같아요!");
//        } else if (score < -0.1) {
//            result = handleSadEmotion(magnitude);
//        } else if (score < 0.2) {
//            result = handleNeutralEmotion(magnitude);
//        } else if (score < 0.6) {
//            result = handlePositiveEmotion(magnitude, "기쁨이나 행복이 느껴집니다.");
//        } else {
//            result = handlePositiveEmotion(magnitude, "정말 기쁘고 행복한 기분입니다!");
//        }
//
//        return result;
//
//
//
//    }
//
//    private String handleNegativeEmotion(float magnitude, String baseEmotion) {
//        if (magnitude > 0.8) {
//            return baseEmotion + " 감정이 매우 강하게 나타났습니다.";
//        } else if (magnitude > 0.6) {
//            return baseEmotion + " 감정이 꽤 강하게 나타났습니다.";
//        } else {
//            return baseEmotion + " 감정이 조금 약하게 나타났습니다.";
//        }
//    }
//
//    // 슬픔에 대한 감정 처리
//    private String handleSadEmotion(float magnitude) {
//        if (magnitude > 0.6) {
//            return "슬프거나 불안한 기분이 강하게 나타났습니다.";
//        } else if (magnitude > 0.4) {
//            return "조금 슬프거나 불안한 느낌이 드는 것 같아요.";
//        } else {
//            return "기분이 조금 우울한 것 같습니다.";
//        }
//    }
//
//    // 중립적인 감정 처리
//    private String handleNeutralEmotion(float magnitude) {
//        if (magnitude > 0.6) {
//            return "조금 불안하거나 혼란스러운 기분인 것 같아요.";
//        } else if (magnitude > 0.4) {
//            return "기분이 약간 불안정하거나 중립적인 것 같아요.";
//        } else {
//            return "기분이 좀 혼란스러울 수도 있습니다.";
//        }
//    }
//
//    // 긍정적인 감정 처리
//    private String handlePositiveEmotion(float magnitude, String baseEmotion) {
//        if (magnitude > 0.8) {
//            return baseEmotion + " 감정이 매우 강하게 나타났습니다!";
//        } else if (magnitude > 0.6) {
//            return baseEmotion + " 기쁨이 느껴집니다.";
//        } else {
//            return "기분이 아주 좋은 것 같아요!";
//        }
//    }
//
//
//
//    public void close() {
//        languageServiceClient.close();
//    }
//
//
//
//
//}
