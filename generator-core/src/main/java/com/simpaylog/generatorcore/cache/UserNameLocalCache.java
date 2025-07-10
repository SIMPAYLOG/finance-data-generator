package com.simpaylog.generatorcore.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpaylog.generatorcore.cache.dto.NameInfo;
import com.simpaylog.generatorcore.cache.dto.preference.PreferenceInfo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Getter
@Component
public class UserNameLocalCache {
    private final HashMap<Character, HashMap<Integer, ArrayList<String>>> names = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("JSON DATA LOADING...");
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("names.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<NameInfo> users = objectMapper.readValue(inputStream, new TypeReference<List<NameInfo>>() {
            });

            for (NameInfo user : users) {
                char genderKey = user.gender().charAt(0);
                int ageGroupKey = user.ageGroup();

                this.names.computeIfAbsent(genderKey, k -> new HashMap<>())
                        .computeIfAbsent(ageGroupKey, k -> new ArrayList<>())
                        .add(user.fullName());
            }

            log.info("JSON DATA LOADING FINISH.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> get(char gender, int ageGroup){
        return names.get(gender).get(ageGroup);
    }
}
