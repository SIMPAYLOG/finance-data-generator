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


    public String getVendor(Long userId, String key, String category, String region) {
        region = ensureUserRegion(userId, region);
        String area = ensureUserArea(userId, region);

        switch (key) {
            case "groceriesNonAlcoholicBeverages":
                switch (category) {
                    case "시장 반찬":
                    case "즉석 국/찌개/반찬류": return localName() + " 반찬";
                    case "식빵/베이커리 번들": return pickRandom("뚜레쥬르", "파리바게뜨") + region + area + "점";
                    case "간편식·HMR":
                    case "편의점 도시락":
                    case "라면 및 봉지면류": return pickRandom("CU", "GS25", "세븐일레븐") + region + area + "점";
                    case "제철 채소·과일 박스":
                    case "저가 채소/과일": return localName() + " 시장";
                    case "요거트, 우유, 유제품 묶음":
                    case "생수/음료 묶음":
                    case "유통기한 임박 제품":
                    case "냉동식품":
                    case "대형마트 장보기": return pickRandom("이마트", "롯데마트", "홈플러스") + region + area + "점";
                    case "대용량 음료 및 간식": return pickRandom("이마트트레이더스", "코스트코") + region + area + "점";
                    default: return localName() + " 식료품점";
                }

            case "alcoholicBeveragesTobacco":
                switch (category) {
                    case "수입 맥주": return localName() + " " + pickRandom("보틀샵", "비어", "맥주");
                    case "막걸리": return localName() + " " + pickRandom("막걸리", "전통주마트");
                    case "와인/양주": return localName() + " " + pickRandom("세계주류", "주류전문점");
                    case "일반 소주/맥주":
                    case "일반 담배": return pickRandom("CU", "GS25", "이마트") + " " + region + area + "점";
                    case "궐련형 전자담배": return localName() + " 전자담배 전문점";
                    default: return localName() + " 주류/담배판매점";
                }

            case "clothingFootwear":
                switch (category) {
                    case "중저가 캐주얼 브랜드 의류":
                        return pickRandom("유니클로", "H&M", "지오다노") + " " + region + area + "점";
                    case "저가 의류 잡화":
                        return pickRandom(localName() + " " + "의류", "", "H&M");
                    case "패션 브랜드 기본 티셔츠":
                        return pickRandom("무신사 스토어", "스타일난다", "H&M") + " " + region + area + "점";
                    case "신발 구매":
                    case "데일리 슈즈":
                    case "스포츠 브랜드 운동화":
                        return pickRandom("ABC마트", "풋락커", localName() + "신발", "신세계백화점") + " " + region + area + "점";

                    case "중고 의류":
                        return localName() + " " + pickRandom("헌옷가게", "빈티지샵");
                    case "의류 수선":
                        return localName() + " " + pickRandom("수선", "리폼샵", "의류수선 전문점");
                    case "보세 의류":
                        return localName() + " " + pickRandom("보세의류샵", "의류점", "스트리트샵");
                    case "SPA 브랜드 외투":
                        return pickRandom("자라", "H&M", "유니클로") + " " + region + area + "점";
                    default:
                        return "무신사 스토어" + " " + region + area + "점";
                }

            case "housingUtilitiesFuel":
                switch (category) {
                    case "월세": return localName() + " " + localName() + " 아파트";
                    case "수도요금":
                    case "수도 요금": return UTIL_BY_REGION.get(region).get("수도");
                    case "전기 요금":
                    case "전기요금": return "한국전력공사";
                    case "도시가스요금":
                    case "도시가스 요금": return UTIL_BY_REGION.get(region).get("가스");
                    default: return localName() +  " 공과금";
                }

            case "householdGoodsServices":
                switch (category) {
                    case "일반 가전제품": return pickRandom("하이마트", "전자랜드", "롯데하이마트") + " " + region + area + "점";
                    case "기본적인 가구": return pickRandom("이케아", "한샘", "까사미아") + " " + region + area + "점";
                    case "가사 도우미": return pickRandom("청소나라", "우리집 청소", "홈클린");
                    case "다이소 소모품": return "다이소 "  + " " + region + area + "점";
                    case "침구류 교체": return pickRandom("이케아", "한샘", "잠비")  + " " + region + area + "점";
                    case "청소/세탁 용품":
                    case "직접 청소 용품":
                    case "저렴한 식기류":
                    case "저가 생활용품": return pickRandom("다이소", "롯데마트", "이마트", "홈플러스") + " " + region + area + "점";
                    case "대용량 생활용품 구매": return pickRandom("코스트코", "이마트 트레이더스") + " " + region + area + "점";
                    default: return localName() + " 생활용품점";
                }

            case "health":
                switch (category) {
                    case "동네 피트니스 센터": return localName() + " " + pickRandom("휘트니스", "헬스클럽", "짐");
                    case "일반 진료비":
                    case "일반 의원 진료": return localName() + " " + pickRandom("동네의원", "건강의원", "정형외과");
                    case "저가 상비약": return localName() + " " + pickRandom("약국", "동네약국");
                    case "일반 건강검진": return localName() + " " + pickRandom("검진센터", "종합병원");
                    case "영양제": return localName() + " " + pickRandom("약국", "건강보조식품");
                    case "약국 약값":
                    case "동네 약국 구매": return localName() + " " + pickRandom("약국", "약국체인");
                    case "종합병원 외래 진료": return pickRandom("서울대병원", "삼성서울병원", "세브란스병원", "충남대학병원");
                    case "기본 의료용품": return pickRandom("올리브영", "다이소", "약국");
                    default: return localName() + " 건강센터";
                }

            case "transportation":
                switch (category) {
                    case "공유 자전거/킥보드": return pickRandom("따릉이", "씽씽", "라임");
                    case "버스":
                    case "지하철": return "대중교통";
                    case "기차": return "코레일";
                    case "택시": return localName() + " 운수";
                    default: return localName() + " 교통수단";
                }

            case "communication":
                switch (category) {
                    case "인터넷 사용료": return INTERNET_BRANDS.get(RND.nextInt(INTERNET_BRANDS.size()));
                    case "통신 요금":
                    case "모바일 기기 구매": return TELECOM_BRANDS.get(RND.nextInt(TELECOM_BRANDS.size()));
                    default: return localName() + " 통신";
                }

            case "recreationCulture":
                switch (category) {
                    case "영화관람":
                    case "영화 관람": return pickRandom("CGV", "롯데시네마", "메가박스")  + region + area + "점";
                    case "테마파크 입장권": return pickRandom("에버랜드", "롯데월드", "서울랜드");
                    case "대중 공연": return localName() + " " + localName() + " 공연장";
                    case "서점 도서 구매": return pickRandom("교보문고", "영풍문고", "반디앤루니스") + region + area + "점";
                    case "공원 입장료": return localName() + " " + localName() + "공원";
                    case "전시/행사": return localName() + " " + "전시관";
                    case "장난감": return localName() + " " + pickRandom("토이월드", "장난감나라", "장난감세상");
                    default: return localName() + " 문화시설";
                }

            case "education":
                switch (category) {
                    case "인터넷 강의": return pickRandom("메가스터디 온라인캠퍼스", "이투스 온라인", "대성마이맥");
                    case "학원 수강":
                    case "학원": return localName() + " " + "학원";
                    case "학습지":
                    case "도서 구매":
                    case "문구류 구매": return pickRandom("교보문고", "영풍문고", localName() + " " + "문고");
                    default: return localName() + " 교육센터";
                }

            case "foodAccommodation":
                switch (category) {
                    case "찜질방":
                    case "사우나": return localName() + " 사우나";
                    case "프랜차이즈 식당 외식": return pickRandom("스타벅스", "맥도날드", "버거킹", "롯데리아")  + region + area + "점";
                    case "분식/국밥 등 저가 외식": return pickRandom("김밥천국", "천리김밥", localName() + " 국밥집");
                    case "배달 음식": return pickRandom("배달의민족", "요기요", "배달통");
                    case "카페 이용": return pickRandom("투썸플레이스", "이디야", "스타벅스") + region + area + "점";
                    case "모텔": return localName() + " " + localName() + "모텔";
                    case "호텔": return localName() + " " + localName() + "호텔";
                    default: return localName() + " 숙박";
                }

            case "otherGoodsServices":
                switch (category) {
                    case "화장품": return pickRandom("올리브영", "롭스", "아리따움") + region + area + "점";
                    case "일반 미용실":
                    case "미용실": return localName() + " " + pickRandom("헤어살롱", "뷰티살롱", "헤어샵");
                    case "생활 수선 서비스": return localName() + " " + pickRandom("수선집", "공방", "수선센터");
                    case "은행 수수료":
                        String bank = ensureUserBank(userId);
                        return bank + " " + area + "점";
                    case "잡화": return area + " " + pickRandom("다이소", "롯데마트", "이마트") + region + area + "점";
                    case "향수": return area + " " + pickRandom("올리브영", "신세계백화점", "롯데백화점", "NC백화점") + region + area + "점";
                    case "개인 위생용품": return area + " " + pickRandom("다이소", "올리브영", "홈플러스") + region + area + "점";
                    default: return area + " 기타매장";
                }

            default:
                return localName() + " " + category;
        }
    }

    private String pickRandom(String... options) {
        return options[RND.nextInt(options.length)];
    }

    private String localName() {
        String[] locals = {
                "가온", "푸른", "솔", "하늘", "빛", "다온", "새롬", "바다", "나래", "아라",

                "가람", "이슬", "마루", "뫼", "윤슬", "여울", "안개", "노을", "구름", "미리내",

                "꽃", "잎새", "열매", "라온", "새싹", "소나무", "난초", "버들", "뿌리", "으뜸",

                "슬기", "보람", "한결", "힘찬", "고운", "다운", "별", "으뜸", "사랑", "맑음",

                "겨루", "도담", "시내", "우람", "지음", "파란", "해솔", "흐름", "너울", "소미"
        };
        return locals[RND.nextInt(locals.length)];
    }

    private String ensureUserRegion(Long userId, String region) {
        String k = "user:" + userId + ":region";
        String r = redisTemplate.opsForValue().get(k);
        if (r == null) {
            redisTemplate.opsForValue().set(k, region);
        }
        return r;
    }

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

    private String ensureUserBank(Long userId) {
        String key = "user:" + userId + ":bank";
        String bank = redisTemplate.opsForValue().get(key);
        if (bank == null) {
            bank = BANKS.get(Math.abs(userId.hashCode()) % BANKS.size());
            redisTemplate.opsForValue().set(key, bank);
        }
        return bank;
    }
}
