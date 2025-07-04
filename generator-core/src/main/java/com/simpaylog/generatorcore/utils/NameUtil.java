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
    private final HashMap<Character, HashMap<Integer, ArrayList<String>>> names = new HashMap<>();

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

    public String getRandomName(char gender, int ageGroup) {
        HashMap<Integer, ArrayList<String>> ageMap = names.get(gender);
        ArrayList<String> nameList = ageMap.get(ageGroup);
        int randomIndex = random.nextInt(nameList.size());

        return nameList.get(randomIndex);
    }
}


