package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class NameUtilTest extends TestConfig {
    @Autowired
    NameUtil nameUtil;

    @Test
    void 랜덤이름을_생성한다() {
        //given
        char gender = 'M';
        int ageGroup = 10;
        // When
        String manName = nameUtil.getRandomName(gender, ageGroup);
        // Then
        System.out.println(manName);
        assertNotNull(manName, "생성된 이름은 null이 아니어야 합니다.");
    }
}