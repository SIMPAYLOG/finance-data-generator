package com.simpaylog.generatorsimulator.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class StoreNameGenerator {

    private final StringRedisTemplate redisTemplate;
    private static final Random RND = new Random(123456789); // deterministic seed 사용하고 싶으면 고정, 아니면 제거

    private static final Map<String, List<String>> REGION_AREAS = Map.of(
            "서울", List.of(
                    "강남", "역삼", "삼성", "신사", "청담", "압구정", "논현", "서초", "방배", "잠실",
                    "송파", "잠원", "반포", "이태원", "한남", "용산", "명동", "종로", "광화문", "을지로",
                    "신촌", "홍대", "상수", "합정", "마포", "건대", "왕십리", "성수", "노원", "수유"
            ),

            "부산", List.of(
                    "서면", "해운대", "광안리", "남포동", "동래", "연산동", "부전동", "전포동", "수영", "사직",
                    "사상", "하단", "부산진", "기장", "온천장", "대연동", "문현동", "감전동", "남구", "중구",
                    "영도", "우동", "동삼동", "재송동", "명륜동", "괴정동", "모라동", "가야동", "장전동", "금정"
            ),

            "인천", List.of(
                    "송도", "연수동", "옥련동", "문학동", "주안", "간석동", "부평", "삼산동", "청천동", "갈산동",
                    "계산동", "작전동", "서구청", "검암동", "청라", "석남동", "가좌동", "인천터미널", "송림동", "용현동",
                    "남항동", "신포동", "도화동", "동춘동", "송현동", "논현동", "마전동", "백석동", "운서동", "영종도"
            ),

            "대구", List.of(
                    "동성로", "중앙로", "서문시장", "수성구", "범어동", "달서구", "성서", "두류동", "상인동", "용산동",
                    "대명동", "남구", "북구", "침산동", "산격동", "복현동", "칠성동", "신암동", "동구", "파호동",
                    "달성군", "현풍", "테크노폴리스", "수성못", "황금동", "만촌동", "지산동", "시지", "범물동", "고산동"
            ),

            "대전", List.of(
                    "둔산동", "은행동", "탄방동", "갈마동", "월평동", "유성", "죽동", "궁동", "봉명동", "노은동",
                    "도안동", "중구", "대흥동", "선화동", "용문동", "문화동", "정림동", "가양동", "용전동", "오정동",
                    "관저동", "변동", "복수동", "진잠동", "신성동", "문지동", "덕명동", "관평동", "송강동", "장대동"
            ),

            "광주", List.of(
                    "충장로", "금남로", "상무지구", "첨단", "운암동", "용봉동", "수완지구", "일곡동", "봉선동", "양림동",
                    "남구", "북구", "광천동", "화정동", "두암동", "중흥동", "산수동", "문흥동", "용두동", "동림동",
                    "신창동", "쌍촌동", "유촌동", "농성동", "학동", "백운동", "진월동", "효덕동", "송정동", "비아동"
            ),

            "울산", List.of(
                    "삼산동", "무거동", "신정동", "옥동", "남구", "달동", "중구", "성남동", "학성동", "반구동",
                    "태화동", "우정동", "병영동", "북구", "연암동", "명촌동", "진장동", "효문동", "농소동", "화봉동",
                    "울주군", "언양", "범서읍", "온양읍", "청량읍", "덕신리", "두동면", "삼동면", "간절곶", "정자동"
            ),

            "세종", List.of(
                    "도담동", "어진동", "종촌동", "아름동", "보람동", "대평동", "나성동", "소담동", "새롬동", "한솔동",
                    "다정동", "가람동", "반곡동", "해밀동", "합강동", "부강면", "연동면", "연서면", "전의면", "전동면",
                    "조치원읍", "신흥리", "침산리", "고운동", "제천리", "산울동", "도원리", "세종터미널", "가락리", "금남면"
            ),

            "제주도", List.of(
                    "제주시", "이도동", "노형동", "연동", "삼도동", "용담동", "아라동", "도남동", "오라동", "화북동",
                    "외도동", "애월읍", "한림읍", "한경면", "구좌읍", "조천읍", "서귀포시", "중문", "대정읍", "남원읍",
                    "표선면", "성산읍", "안덕면", "강정동", "하예동", "동홍동", "신효동", "법환동", "보목동", "토평동"
            )
    );

    // ---------------- 유틸/공공(지역별 일관성)
    private static final Map<String, Map<String, String>> UTIL_BY_REGION = Map.of(
            "서울", Map.of("수도", "서울상수도사업본부", "가스", "서울도시가스"),
            "부산", Map.of("수도", "부산상수도사업본부", "가스", "부산도시가스"),
            "대구", Map.of("수도", "대구상수도사업본부", "가스", "대구대성에너지"),
            "대전", Map.of("수도", "대전상수도사업본부", "가스", "대전CNCITY에너지"),
            "광주", Map.of("수도", "광주상수도사업본부", "가스", "광주도시가스"),
            "인천", Map.of("수도", "인천상수도사업본부", "가스", "인천도시가스"),
            "울산", Map.of("수도", "울산상수도사업본부", "가스", "울산도시가스"),
            "세종", Map.of("수도", "세종상하수도사업소", "가스", "세종도시가스"),
            "제주도", Map.of("수도", "제주상수도본부", "가스", "제주도시가스")
    );

    // ---------------- 고정 브랜드 그룹
    private static final List<String> TELECOM_BRANDS = List.of("KT", "SKT", "LG U+");
    private static final List<String> INTERNET_BRANDS = List.of("KT인터넷", "SK브로드밴드", "LG U+인터넷");
    private static final List<String> ELECTRIC_BRANDS = List.of("한국전력공사");

    private static final List<String> BANKS = List.of("KB국민은행", "신한은행", "우리은행", "하나은행", "NH농협은행", "IBK기업은행", "카카오뱅크");

    // ---------------- 체인/프랜차이즈 대표(예시)
    private static final List<String> CONVENIENCE_CHAINS = List.of("CU", "GS25", "세븐일레븐", "이마트24");
    private static final List<String> CAFE_CHAINS = List.of("스타벅스", "이디야", "투썸", "빽다방");
    private static final List<String> BULK_CHAINS = List.of("이마트 트레이더스", "코스트코", "노브랜드");
    private static final List<String> FASTFOOD_CHAINS = List.of("맥도날드", "버거킹", "KFC");
    private static final List<String> MART_CHAINS = List.of("이마트", "롯데마트", "홈플러스");
    private static final List<String> PHARMACY_CHAINS = List.of("우리약국", "메디팜", "굿모닝약국");
    private static final List<String> APPLIANCES_CHAINS = List.of("전자랜드", "하이마트", "일렉트로마트");
    private static final List<String> STATIONERY_CHAINS = List.of("교보문고", "영풍문고", "알라딘");

    // ---------------- 업종 그룹
    private enum Group {
        CAFE, FAST_FOOD, CONVENIENCE, FOOD_RESTAURANT, GROCERY, ALCOHOL, TOBACCO,
        CLOTHING, HOME_GOODS,
        HEALTH_CLINIC, HEALTH_HOSPITAL, PHARMACY, HEALTH_CHECK, SUPPLEMENTS,
        BEAUTY, LAUNDRY, REPAIR, HOUSEKEEPING,
        TRANSPORT, COMM, UTIL, BANK, STATIONERY,
        LEISURE_MOVIE, LEISURE_THEMEPARK, LEISURE_PERFORMANCE, LEISURE_BATH, ACCOMMODATION,
        EDUCATION, SERVICE, HOME_LIVING, FITNESS, BULK, HOME_APPLIANCES, MOTEL, HOTEL, PARK, ETC
    }

    public String getVendor(Long userId, String category, String region) {
        // 1) user region/area
//        String location = ensureUserRegion(userId, region);
        String area = ensureUserArea(userId, region);

        Group group = detectGroup(category);

        if (isUtility(category)) {
            String utilKey = utilityKey(category);
            String util = UTIL_BY_REGION.getOrDefault(region, Collections.emptyMap()).get(utilKey);
            if (util != null) return util;

            if ("수도".equals(utilKey)) return region + "상수도사업본부";
            if ("가스".equals(utilKey)) return region + "도시가스";
            if (category.toLowerCase().contains("전기")) return ELECTRIC_BRANDS.get(0);
        }

        if (group == Group.COMM) {
            if (category.toLowerCase().contains("인터넷")) {
                return pickFixedPerUser(userId, "internetBrand", INTERNET_BRANDS);
            } else {
                return pickFixedPerUser(userId, "telecomBrand", TELECOM_BRANDS);
            }
        }

        if (group == Group.BANK) {
            return ensureUserBank(userId);
        }

        String vendorKey = "user:" + userId + ":vendor:" + normalize(category);
        String existing = redisTemplate.opsForValue().get(vendorKey);
        if (existing != null) {
            incrementVendorCount(userId, existing);
            return existing;
        }

        List<String> candidates = buildCandidatesFor(group, category, region, area);

        String chosen = pickWeighted(userId, candidates);

        redisTemplate.opsForValue().set(vendorKey, chosen);
        incrementVendorCount(userId, chosen);

        return chosen;
    }


//    private String ensureUserRegion(Long userId, String region) {
//        String k = "user:" + userId + ":region";
//        String r = redisTemplate.opsForValue().get(k);
//        if (r == null) {
//            redisTemplate.opsForValue().set(k, region);
//        }
//        return r;
//    }

    private String ensureUserArea(Long userId, String region) {
        String k = "user:" + userId + ":area";
        String a = redisTemplate.opsForValue().get(k);
        if (a == null) {
            List<String> areas = REGION_AREAS.getOrDefault(region, List.of("서울"));
            a = areas.get(RND.nextInt(areas.size()));
            redisTemplate.opsForValue().set(k, a);
        }
        return a;
    }

    private boolean isUtility(String category) {
        String c = category == null ? "" : category.toLowerCase();
        return c.contains("수도") || c.contains("가스") || c.contains("전기");
    }

    private String utilityKey(String category) {
        String c = category == null ? "" : category.toLowerCase();
        if (c.contains("수도")) return "수도";
        if (c.contains("가스")) return "가스";
        if (c.contains("전기")) return "전기";
        return category;
    }

    private String pickFixedPerUser(Long userId, String suffix, List<String> brands) {
        String key = "user:" + userId + ":" + suffix;
        String v = redisTemplate.opsForValue().get(key);
        if (v == null) {
            v = brands.get(Math.abs(userId.hashCode()) % brands.size());
            redisTemplate.opsForValue().set(key, v);
        }
        return v;
    }

    private String ensureUserBank(Long userId) {
        String key = "user:" + userId + ":bank";
        String bank = redisTemplate.opsForValue().get(key);
        if (bank == null) {
            bank = BANKS.get(Math.abs(userId.hashCode()) % BANKS.size());
            redisTemplate.opsForValue().set(key, bank);
        }
        return bank;
    }

    private void incrementVendorCount(Long userId, String vendor) {
        String key = "user:" + userId + ":vendorcount:" + vendor;
        redisTemplate.opsForValue().increment(key);
    }

    private List<String> buildCandidatesFor(Group group, String category, String region, String area) {
        List<String> out = new ArrayList<>();

        switch (group) {
            case CAFE:
                // chain + indie local
                for (String c : CAFE_CHAINS) out.add(area + " " + c);
                out.add(area + " " + localName() + "카페");
                out.add(localName() + "카페");
                break;

            case FAST_FOOD:
                for (String f : FASTFOOD_CHAINS) out.add(area + " " + f);
                out.add(area + " " + localName() + "버거");
                break;

            case BULK:
                for (String f : BULK_CHAINS) out.add(area + " " + f);
                break;

            case CONVENIENCE:
                for (String c : CONVENIENCE_CHAINS) out.add(area + " " + c);
                out.add(area + " 24편의");
                out.add(localName() + "스토어");
                break;

            case FOOD_RESTAURANT:
                out.add(area + " " + localName() + "식당");
                out.add(localSurname() + "분식");
                out.add(area + " " + localName() + "포차");
                break;

            case GROCERY:
                for (String m : MART_CHAINS) out.add(m + " " + area);
                out.add(area + " " + localName() + "마트");
                out.add(localName() + "마켓");
                break;

            case ALCOHOL:
                out.add(area + " " + localName() + "주류");
                out.add(localName() + "포장주류");
                break;

            case TOBACCO:
                out.add(area + " " + CONVENIENCE_CHAINS.get(RND.nextInt(CONVENIENCE_CHAINS.size())));
                out.add(area + " " + localName() + "담배");
                break;

            case CLOTHING:
                out.add(area + " " + localName() + "의류");
                out.add(localName() + "샵");
                break;

            case HOME_LIVING:
                out.add(area + " 다이소");
                out.add(area + " " + localName() + "리빙");
                break;

            case HOME_GOODS:
                out.add(area + " 이케아");
                out.add(area + localName() + " 리빙");
                break;

            case HEALTH_CLINIC:
                out.add(area + " " + localName() + "의원");
                out.add(localName() + "클리닉");
                break;

            case STATIONERY:
                for (String p : STATIONERY_CHAINS) out.add(area + " " + p);
                break;

            case HEALTH_HOSPITAL:
                out.add(region + " 종합병원");
                break;

            case PHARMACY:
                for (String p : PHARMACY_CHAINS) out.add(area + " " + p);
                out.add(area + " " + localName() + "약국");
                break;

            case HEALTH_CHECK:
                out.add(region + " 건강검진센터");
                out.add(area + " " + localName() + "검진클리닉");
                break;

            case SUPPLEMENTS:
                out.add(area + " " + localName() + "영양제샵");
                out.add("헬스몰 " + localName());
                break;

            case BEAUTY:
                out.add(area + " " + localName() + "뷰티");
                break;

            case LAUNDRY:
                out.add(area + " " + localName() + "세탁");
                break;

            case REPAIR:
                out.add(area + " " + localName() + "수선소");
                out.add(localName() + "수선");
                break;

            case HOUSEKEEPING:
                out.add(localName() + "홈클리닝");
                break;

            case TRANSPORT:
                if (category.toLowerCase().contains("택시")) out.add(localName() + " 운수");
                if (category.toLowerCase().contains("지하철") || category.toLowerCase().contains("버스"))
                    out.add(region + "교통공사");
                if (category.toLowerCase().contains("기차")) out.add("코레일");
                if (category.toLowerCase().contains("킥보드") || category.toLowerCase().contains("자전거"))
                    out.add(localName() + " 공유모빌리티");
                break;

            case LEISURE_MOVIE:
                out.add(area + " CGV");
                out.add(area + " 메가박스");
                out.add(area + " 롯데시네마");
                break;

            case LEISURE_THEMEPARK:
                out.add("에버랜드");
                out.add("롯데월드");
                out.add("서울랜드");
                break;

            case LEISURE_PERFORMANCE:
                out.add(area + " 공연장");
                out.add(region + " 공연티켓처");
                break;

            case LEISURE_BATH:
                out.add(area + " 찜질방");
                out.add(localName() + " 사우나");
                break;

            case MOTEL:
                out.add(area + " " + localName() + "모텔");
                break;

            case HOTEL:
                out.add(area + " " + localName() + "호텔");
                break;

            case PARK:
                out.add(area + " " + localName() + "공원");
                break;

            case EDUCATION:
                out.add(area + " " + localName() + "학원");
                break;

            case SERVICE:
                out.add(area + " " + localName() + "서비스");
                out.add(localName() + "공방");
                break;

            case FITNESS:
                out.add(localName() + "피트니스");
                break;

            case HOME_APPLIANCES:
                for (String f : APPLIANCES_CHAINS) out.add(area + " " + f);
                break;

            default:
                out.add(area + " " + localName() + "상점");
                break;
        }

        // dedupe & ensure non-empty
        LinkedHashSet<String> set = new LinkedHashSet<>(out);
        if (set.isEmpty()) set.add(area + " " + localName() + "상점");
        return new ArrayList<>(set);
    }

    private String pickWeighted(Long userId, List<String> candidates) {
        // weight = 1 + user-specific count
        Map<String, Integer> weights = new LinkedHashMap<>();
        int total = 0;
        for (String c : candidates) {
            String cntKey = "user:" + userId + ":vendorcount:" + c;
            String s = redisTemplate.opsForValue().get(cntKey);
            int cnt = s == null ? 0 : Integer.parseInt(s);
            int w = 1 + cnt;
            weights.put(c, w);
            total += w;
        }
        int r = RND.nextInt(total);
        int cum = 0;
        for (Map.Entry<String, Integer> e : weights.entrySet()) {
            cum += e.getValue();
            if (r < cum) {
                // increment
                redisTemplate.opsForValue().increment("user:" + userId + ":vendorcount:" + e.getKey());
                return e.getKey();
            }
        }
        String fallback = candidates.get(0);
        redisTemplate.opsForValue().increment("user:" + userId + ":vendorcount:" + fallback);
        return fallback;
    }

    private Group detectGroup(String category) {
        String c = (category == null) ? "" : category.toLowerCase();

        // UTIL
        if (c.contains("수도") || c.contains("가스") || c.contains("전기")) return Group.UTIL;

        // COMM / BANK
        if (c.contains("통신") || c.contains("인터넷")) return Group.COMM;
        if (c.contains("은행") || c.contains("수수료")) return Group.BANK;

        // transport
        if (c.contains("버스") || c.contains("지하철") || c.contains("택시") || c.contains("기차") || c.contains("킥보드") || c.contains("자전거"))
            return Group.TRANSPORT;

        // leisure finer granularity
        if (c.contains("영화")) return Group.LEISURE_MOVIE;
        if (c.contains("테마파크")) return Group.LEISURE_THEMEPARK;
        if (c.contains("공연") || c.contains("대중 공연")) return Group.LEISURE_PERFORMANCE;
        if (c.contains("찜질") || c.contains("사우나")) return Group.LEISURE_BATH;
        if (c.contains("모텔")) return Group.MOTEL;
        if (c.contains("호텔")) return Group.HOTEL;
        if (c.contains("공원")) return Group.PARK;

        // food / convenience / grocery
        if (c.contains("카페")) return Group.CAFE;
        if (c.contains("패스트푸드") || c.contains("패스트푸드점")) return Group.FAST_FOOD;
        if (c.contains("편의") || c.contains("도시락") || c.contains("편의점")) return Group.CONVENIENCE;
        if (c.contains("배달") || c.contains("분식") || c.contains("외식") || c.contains("식당") || c.contains("국밥") || c.contains("찌개") || c.contains("국"))
            return Group.FOOD_RESTAURANT;
        if (c.contains("마트") || c.contains("장보기") || c.contains("냉동") || c.contains("간편식") || c.contains("hmr") || c.contains("요거트") || c.contains("식빵"))
            return Group.GROCERY;
        if (c.contains("소주") || c.contains("맥주") || c.contains("와인") || c.contains("양주") || c.contains("막걸리"))
            return Group.ALCOHOL;
        if (c.contains("담배") || c.contains("전자담배") || c.contains("궐련형")) return Group.TOBACCO;

        // clothing / shopping
        if (c.contains("의류") || c.contains("티셔츠") || c.contains("신발") || c.contains("슈즈") || c.contains("아울렛") || c.contains("중고"))
            return Group.CLOTHING;
        if (c.contains("생활용품") || c.contains("다이소") || c.contains("청소")) return Group.HOME_LIVING;

        if (c.contains("대용량")) return Group.BULK;

        if (c.contains("식기") || c.contains("가구") || c.contains("침구")) return Group.HOME_GOODS;

        // health split
        if (c.contains("병원") || c.contains("종합")) return Group.HEALTH_HOSPITAL;
        if (c.contains("건강검진")) return Group.HEALTH_CHECK;
        if (c.contains("의원") || c.contains("진료") || c.contains("진료비")) return Group.HEALTH_CLINIC;
        if (c.contains("약국") || c.contains("상비약") || c.contains("약값")) return Group.PHARMACY;
        if (c.contains("영양제")) return Group.SUPPLEMENTS;

        // beauty / laundry / repair / housekeeping
        if (c.contains("미용") || c.contains("헤어") || c.contains("화장품") || c.contains("향수")) return Group.BEAUTY;
        if (c.contains("세탁")) return Group.LAUNDRY;
        if (c.contains("수선")) return Group.REPAIR;
        if (c.contains("가사") || c.contains("도우미")) return Group.HOUSEKEEPING;

        if (c.contains("도서") || c.contains("문구")) return Group.STATIONERY;

        // education
        if (c.contains("학원") || c.contains("학습") || c.contains("인터넷 강의") || c.contains("자격증") || c.contains("학습지"))
            return Group.EDUCATION;

        // leisure / service fallback
        if (c.contains("영화") || c.contains("공연")) return Group.LEISURE_MOVIE;

        if (c.contains("테마파크")) return Group.LEISURE_THEMEPARK;

        if (c.contains(("피트니스"))) return Group.FITNESS;

        if (c.contains(("가전제품"))) return Group.HOME_APPLIANCES;

        return Group.ETC;
    }

    private String localName() {
        String[] parts = {
                "하늘", "푸름", "행복", "별", "온기", "모아", "정원", "달빛", "해솔",
                "구름", "바람", "이슬", "햇살", "노을", "새벽", "숲", "꽃잎", "바다",
                "사랑", "미소", "희망", "꿈", "추억", "설렘", "평온", "향기",
                "속삭임", "쉼"};
        return parts[RND.nextInt(parts.length)];
    }

    private String localSurname() {
        String[] s = {"김", "이", "박", "최", "정", "백", "강", "곽", "성", "하"};
        return s[RND.nextInt(s.length)];
    }

    private String normalize(String category) {
        return category == null ? "unknown" : category.replaceAll("\\s+", "_").toLowerCase();
    }
}
