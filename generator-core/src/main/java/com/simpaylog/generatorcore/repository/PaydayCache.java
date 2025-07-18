package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.exception.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PaydayCache {
    private Map<Long, Map<YearMonth, Set<LocalDate>>> userPaydayMap;


    public void init(List<User> users, LocalDate from, LocalDate to) {
        userPaydayMap = users.stream().collect(Collectors.toMap(
                User::getId,
                user -> {
                    Map<YearMonth, Set<LocalDate>> monthMap = new LinkedHashMap<>();
                    YearMonth start = YearMonth.from(from);
                    YearMonth end = YearMonth.from(to);

                    YearMonth cur = start;
                    while(!cur.isAfter(end)) {
                        monthMap.put(cur, new HashSet<>());
                        cur = cur.plusMonths(1);
                    }
                    return monthMap;
                }
        ));
    }

    public void register(Long userId, YearMonth yearMonth, List<LocalDate> paydays) {
        if(userPaydayMap.containsKey(userId)) {
            throw new CoreException("존재하지 않는 유저ID입니다.");
        }
        if(!userPaydayMap.get(userId).get(yearMonth).isEmpty()) {
            log.error("이미 데이터가 할당되어 있습니다.");
        }
        userPaydayMap.get(userId).get(yearMonth).addAll(paydays);
    }

    public boolean isPayday(Long userId, YearMonth yearMonth, LocalDate date) {
        if(userPaydayMap.containsKey(userId)) {
            throw new CoreException("존재하지 않는 유저ID입니다.");
        }

        return userPaydayMap.get(userId).get(yearMonth).contains(date);
    }

    public int numberOfPaydays(Long userId, YearMonth yearMonth) {
        return userPaydayMap.get(userId).get(yearMonth).size();
    }

    public void clear() {
        userPaydayMap.clear();
    }

}
