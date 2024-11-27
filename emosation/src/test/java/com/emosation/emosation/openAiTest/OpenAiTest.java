package com.emosation.emosation.openAiTest;


import com.emosation.emosation.sevices.OpenAiService;
import org.assertj.core.condition.Not;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OpenAiTest {

    @Autowired
    private OpenAiService openAiService;

    @Test
    public void test() {
        String text = "하.. 진짜";
        String prompt = "지금부터 내 감정을 분석해줘 짧고 간결하게 한문장으로  : " + text ;

        String result = openAiService.genRepsAsyn(prompt);

        System.out.println(result);
    }


}
