package com.simpaylog.generatorcore.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.dto.NameDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class NameUtil {
    private final Random random = new Random();
    private HashMap<Character, HashMap<Integer, ArrayList<String>>> names = new HashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("JSON 데이터 로딩을 시작합니다...");
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("names.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<NameDto> users = objectMapper.readValue(inputStream, new TypeReference<List<NameDto>>() {
            });

            for (NameDto user : users) {
                char genderKey = user.getGender().charAt(0);
                int ageGroupKey = user.getAgeGroup();

                this.names.computeIfAbsent(genderKey, k -> new HashMap<>())
                        .computeIfAbsent(ageGroupKey, k -> new ArrayList<>())
                        .add(user.getFullName());
            }

            System.out.println("JSON 데이터 로딩 완료.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRandomName(char ch, int age) {
        HashMap<Integer, ArrayList<String>> ageMap = names.get(ch);

        if (ageMap == null) {
            return "이수현";
        }
        ArrayList<String> nameList = ageMap.get(age);

        if (nameList == null || nameList.isEmpty()) {
            return "이수현";
        }

        int randomIndex = random.nextInt(nameList.size());

        return nameList.get(randomIndex);
    }
}


