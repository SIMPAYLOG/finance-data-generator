package com.simpaylog.generatorcore.utils;

import com.simpaylog.generatorcore.cache.UserNameLocalCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class NameUtil {
    private final Random random = new Random();
    private final UserNameLocalCache userNameLocalCache;

    //TODO : 나이와 성별에 해당하는 이름이 적어서 중복된 이름이 다수 발생함으로 개선 필요.
    public String getRandomName(char gender, int ageGroup) {
        ArrayList<String> nameList = userNameLocalCache.get(gender, ageGroup);

        return nameList.get(random.nextInt(nameList.size()));
    }
}


