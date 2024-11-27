package com.emosation.emosation.sevices;


import com.nimbusds.jose.shaded.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenAiService  {


    @Value("${OPEN.AI.SECRETKEY}")
    private String SECRET;


    private final RestTemplate restTemplate;

    public OpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }

    public String genRepsAsyn(String text) {
        String url = "https://api.openai.com/v1/chat/completions";

        String model = "gpt-3.5-turbo"; // 모델 선택 4부터는 요금제가 좀 비쌈.. 감정분석정도는 3버젼도 잘함
        String prompt =   text +  " : 감정을 분석해줘 무조건 한문장으로 짧게 결과는 ex. 기분이 좋아보이시는데요? 등등 ";  // gpt의 범용성을 좀 줄이는 변수.. 추후 그냥 쓸수있게 해보자
        String requestBody = String.format("{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 50}", model, prompt); // content에 prompt 내용 삽입


        HttpHeaders headers = new HttpHeaders(); // 헤더정보로 발급받은 api키 와 콘텐츠타입 설정
        headers.set("Authorization", "Bearer " + SECRET);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity  = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST,entity,String.class);  // resp응답 객체의 형식은 json이다.. content 만 잘라서 추출해야함

            JSONObject jsonObject = new JSONObject(resp.getBody());
            JSONArray firstch = jsonObject.getJSONArray("choices");
            JSONObject firstChoice = firstch.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");
            return content; // 일정 바이트 이상되면 잘리는 현상이 있기에 한문장으로 설명해달라고함.


        }catch (HttpClientErrorException | HttpServerErrorException e) {
            return "Error"  + e.getResponseBodyAsString();

        }
    }

}
