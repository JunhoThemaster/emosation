package com.emosation.emosation.AnalyzeTest;


import com.emosation.emosation.sevices.GoogleNLPService;
import com.google.cloud.language.v1.Sentiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = GoogleNLPService.class)
@TestPropertySource(locations = "classpath:application.properties")
public class NLPTest {


    @Autowired
    private GoogleNLPService googleNLPService;



    @Test
    public void testAnalyze() {

        String text = "아 넘나리 화난다";


        Sentiment sentiment = googleNLPService.analyze(text);


        double score = sentiment.getScore();
        double magnitude = sentiment.getMagnitude();

//
        String[] sadWord = {"ㅠㅠ","힝","흑흑","",";"};

        System.out.println(score + " /// " + magnitude);

        if(score < 0.6 && score > 0){
            System.out.println("좋지도 나쁘지도 않아보여요.");

        } else if(score < 0 && score  > -0.5){
            if(magnitude < 0.5){
                System.out.println("조금은 짜증난거같기도?");
            } else{
                System.out.println("많이 짜증난거같기도?");
            }
        } 

        // 감정 점수와 강도에 따른 분류
//        if (score < -0.7) {
//            // 매우 강한 부정적 감정 (예: 매우 화남, 극심한 슬픔)
//            if (magnitude > 0.8) {
//                System.out.println("매우 화나신 것 같아요! 감정이 강하게 나타났습니다.");
//            } else if (magnitude > 0.6) {
//                System.out.println("화가 많이 나신 것 같아요.");
//            } else {
//                System.out.println("화난 기분이 강하게 묻어납니다.");
//            }
//        } else if (score < -0.4) {
//            // 강한 부정적 감정 (예: 화남, 기분 상함)
//            if (magnitude > 0.7) {
//                System.out.println("기분이 많이 상하신 것 같아요! 감정이 꽤 강합니다.");
//            } else if (magnitude > 0.5) {
//                System.out.println("기분이 좀 안 좋은 것 같아요.");
//            } else {
//                System.out.println("기분이 조금 나쁜 것 같아요.");
//            }
//        } else if (score < -0.1) {
//            // 약간 부정적인 감정 (예: 약간 슬픔, 불안)
//            if (magnitude > 0.6) {
//                System.out.println("슬프거나 불안한 기분이 강하게 나타났습니다.");
//            } else if (magnitude > 0.4) {
//                System.out.println("조금 슬프거나 불안한 느낌이 드는 것 같아요.");
//            } else {
//                System.out.println("기분이 조금 우울한 것 같습니다.");
//            }
//        } else if (score < 0.2) {
//            // 중립적 감정 (예: 약간의 불안, 혼란)
//            if (magnitude > 0.6) {
//                System.out.println("조금 불안하거나 혼란스러운 기분인 것 같아요.");
//            } else if (magnitude > 0.4) {
//                System.out.println("기분이 약간 불안정하거나 중립적인 것 같아요.");
//            } else {
//                System.out.println("기분이 좀 혼란스러울 수도 있습니다.");
//            }
//        } else if (score < 0.6) {
//            // 긍정적 감정 (예: 약간 기쁨, 가벼운 행복)
//            if (magnitude > 0.8) {
//                System.out.println("정말 기쁘거나 행복한 기분인 것 같습니다!");
//            } else if (magnitude > 0.6) {
//                System.out.println("기쁨이나 행복이 느껴집니다.");
//            } else {
//                System.out.println("기분이 약간 좋은 것 같습니다.");
//            }
//        } else {
//            // 매우 긍정적 감정 (예: 매우 기쁨, 사랑)
//            if (magnitude > 0.8) {
//                System.out.println("정말 기쁘고 행복한 기분입니다! 감정이 매우 강하게 나타났어요.");
//            } else if (magnitude > 0.6) {
//                System.out.println("매우 즐겁고 행복한 기분인 것 같아요.");
//            } else {
//                System.out.println("기분이 아주 좋은 것 같아요!");
//            }
//        }

    }
}
