package com.simpaylog.generatorsimulator.service;

import com.simpaylog.generatorcore.enums.WageType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class WageCounterpartyNameGenerator {

    private static final Map<Integer, String> OCC_CAT = Map.ofEntries(
            Map.entry(1, "AGRI"),            // 농업, 임업 및 어업
            Map.entry(2, "MINING"),          // 광업
            Map.entry(3, "MANUFACTURING"),   // 제조업
            Map.entry(4, "ELECTRICITY"),     // 전기/가스/증기/공조
            Map.entry(5, "WASTE"),           // 수도/하수/폐기물/원료재생
            Map.entry(6, "CONSTRUCTION"),    // 건설업
            Map.entry(7, "RETAIL"),          // 도매 및 소매업
            Map.entry(8, "TRANSPORT"),       // 운수 및 창고업
            Map.entry(9, "FOOD"),            // 숙박 및 음식점업
            Map.entry(10, "IT"),              // 정보통신업
            Map.entry(11, "FINANCE"),         // 금융 및 보험업
            Map.entry(12, "REAL_ESTATE"),     // 부동산업
            Map.entry(13, "PROFESSIONAL"),    // 전문/과학/기술 서비스
            Map.entry(14, "SUPPORT_SERVICE"), // 사업시설관리/지원/임대
            Map.entry(15, "EDU"),             // 교육 서비스업
            Map.entry(16, "HEALTH"),          // 보건/사회복지
            Map.entry(17, "ARTS"),            // 예술/스포츠/여가
            Map.entry(18, "PERSONAL_SERVICE") // 협회/단체/수리/기타 개인
    );

    // 2) 카테고리별 고용주 풀(간단 샘플) — 필요시 자유롭게 보강
    // 규모 버킷: SOHO, SMB, MID, LARGE
    private static final Map<String, Map<String, List<String>>> EMPLOYER_POOL =
            Map.ofEntries(
                    Map.entry("AGRI", Map.of(
                            "SOHO", List.of("푸른들영농조합", "한솔과수원", "그린팜"),
                            "SMB", List.of("평야농업회사법인", "강남영농㈜"),
                            "MID", List.of("대한아그로㈜"),
                            "LARGE", List.of("코리아애그리㈜")
                    )),
                    Map.entry("MINING", Map.of(
                            "SOHO", List.of("동림채굴사무소", "백운광맥"),
                            "SMB", List.of("청암광업㈜", "오닉스자원개발"),
                            "MID", List.of("한성마이닝㈜"),
                            "LARGE", List.of("코리아자원개발㈜")
                    )),
                    Map.entry("MANUFACTURING", Map.of(
                            "SOHO", List.of("새봄정밀", "다온금속"),
                            "SMB", List.of("한빛제조㈜", "진우기계㈜"),
                            "MID", List.of("넥스트머티리얼즈㈜"),
                            "LARGE", List.of("코스모스산업㈜")
                    )),
                    Map.entry("ELECTRICITY", Map.of(
                            "SOHO", List.of("해밀전기공사", "청호설비"),
                            "SMB", List.of("태성에너지서비스"),
                            "MID", List.of("한전테크솔루션㈜"),
                            "LARGE", List.of("코리아에너지그룹㈜")
                    )),
                    Map.entry("WASTE", Map.of(
                            "SOHO", List.of("맑은물설비", "청정환경센터"),
                            "SMB", List.of("에코리사이클㈜"),
                            "MID", List.of("클린시티㈜"),
                            "LARGE", List.of("국민환경자원㈜")
                    )),
                    Map.entry("CONSTRUCTION", Map.of(
                            "SOHO", List.of("정우건설팀", "세영철거팀", "성진토목반"),
                            "SMB", List.of("한솔토건㈜", "두레건설㈜"),
                            "MID", List.of("동명ENG㈜", "대림개발㈜"),
                            "LARGE", List.of("코리아건설㈜", "한빛건설㈜")
                    )),
                    Map.entry("RETAIL", Map.of(
                            "SOHO", List.of("한길문구", "바른서점", "별빛잡화점"),
                            "SMB", List.of("다온유통㈜", "하나상사"),
                            "MID", List.of("코리아리테일㈜"),
                            "LARGE", List.of("월드마트㈜")
                    )),
                    Map.entry("TRANSPORT", Map.of(
                            "SOHO", List.of("은하물류", "한빛퀵서비스"),
                            "SMB", List.of("도시물류㈜"),
                            "MID", List.of("코리아로지스㈜"),
                            "LARGE", List.of("글로벌항만물류그룹㈜")
                    )),
                    Map.entry("FOOD", Map.of(
                            "SOHO", List.of("카페서담", "동네분식", "밀크티하우스"),
                            "SMB", List.of("미소레스토랑", "한남브런치랩"),
                            "MID", List.of("호텔루체", "포레스트호텔"),
                            "LARGE", List.of("오션스테이 리조트", "코스모호텔")
                    )),
                    Map.entry("IT", Map.of(
                            "SOHO", List.of("봄날개발연구소", "개미컴퓨터", "코드앤픽셀"),
                            "SMB", List.of("한빛소프트㈜", "넥스트링크㈜", "블루바이트㈜"),
                            "MID", List.of("뉴웨이브테크㈜", "에이톤시스템즈㈜"),
                            "LARGE", List.of("메가아이티㈜", "코스모스소프트㈜")
                    )),
                    Map.entry("FINANCE", Map.of(
                            "SOHO", List.of("삼거리보험사무소", "한결세무컨설팅"),
                            "SMB", List.of("미래자산관리㈜"),
                            "MID", List.of("코리아캐피탈㈜"),
                            "LARGE", List.of("대한금융그룹㈜")
                    )),
                    Map.entry("REAL_ESTATE", Map.of(
                            "SOHO", List.of("정성공인중개사사무소", "코지부동산"),
                            "SMB", List.of("한빛자산개발㈜"),
                            "MID", List.of("센트럴리빙㈜"),
                            "LARGE", List.of("코리아리츠홀딩스㈜")
                    )),
                    Map.entry("PROFESSIONAL", Map.of(
                            "SOHO", List.of("로포스법률사무소", "에이치컨설팅"),
                            "SMB", List.of("프라임기술연구㈜"),
                            "MID", List.of("한성엔지니어링㈜"),
                            "LARGE", List.of("코리아리서치그룹㈜")
                    )),
                    Map.entry("SUPPORT_SERVICE", Map.of(
                            "SOHO", List.of("한빛파견센터", "스마트알바"),
                            "SMB", List.of("오더스태프㈜"),
                            "MID", List.of("에이스비즈서포트㈜"),
                            "LARGE", List.of("코리아비즈지원그룹㈜")
                    )),
                    Map.entry("EDU", Map.of(
                            "SOHO", List.of("새봄학원", "한빛과외센터", "정문에듀"),
                            "SMB", List.of("에듀클래스㈜", "러닝팩토리㈜"),
                            "MID", List.of("코리아에듀텍㈜"),
                            "LARGE", List.of("대한교육홀딩스㈜")
                    )),
                    Map.entry("HEALTH", Map.of(
                            "SOHO", List.of("사랑의원", "편안한치과", "푸른재활센터"),
                            "SMB", List.of("민트의료재단", "메디웰㈜"),
                            "MID", List.of("케어앤헬스㈜"),
                            "LARGE", List.of("코스모메디컬센터㈜")
                    )),
                    Map.entry("ARTS", Map.of(
                            "SOHO", List.of("스타라이트스튜디오", "브라보엔터"),
                            "SMB", List.of("컬처팩토리㈜"),
                            "MID", List.of("코리아컬처웍스㈜"),
                            "LARGE", List.of("글로벌엔터테인먼트그룹㈜")
                    )),
                    Map.entry("PERSONAL_SERVICE", Map.of(
                            "SOHO", List.of("스마일수리", "한빛클린", "행복케어센터"),
                            "SMB", List.of("에코케어㈜"),
                            "MID", List.of("코지서비스㈜"),
                            "LARGE", List.of("코리아라이프서비스그룹㈜")
                    ))
            );

    // 프리랜서/긱워크 느낌의 "플랫폼/마켓/스튜디오" 가명 풀 (RANDOM에서 가끔 사용)
    private static final Map<String, List<String>> PLATFORM_POOL = Map.of(
            "IT", List.of("코드워크스", "데이터마켓", "애자일랩스"),
            "EDU", List.of("튜터링마켓", "클라스파크", "에듀허브"),
            "PROFESSIONAL", List.of("컨설트라운지", "리서치마켓"),
            "ARTS", List.of("픽셀스튜디오", "브랜딩팩토리"),
            "TRANSPORT", List.of("라스트마일랩", "도시물류플랫폼")
    );

    // 일용/파트타임에서 가끔 등장할 에이전시/인력지원 명칭
    private static final Map<String, List<String>> AGENCY_POOL = Map.of(
            "CONSTRUCTION", List.of("한빛HR㈜", "현장파트너스", "코리아잡서포트"),
            "RETAIL", List.of("오더스태프", "리테일인력지원센터"),
            "FOOD", List.of("키친스태프㈜", "푸드잡파트너스"),
            "AGRI", List.of("농촌인력지원단", "그린잡㈜")
    );

    // 규모 버킷 & 주기별 가중치 (SOHO, SMB, MID, LARGE)
    private static final String[] SIZE_BUCKET = {"SOHO", "SMB", "MID", "LARGE"};
    private static final Map<WageType, double[]> SIZE_WEIGHTS = Map.of(
            WageType.REGULAR, new double[]{0.05, 0.25, 0.45, 0.25},
            WageType.BI_WEEKLY, new double[]{0.15, 0.45, 0.30, 0.10},
            WageType.WEEKLY, new double[]{0.25, 0.45, 0.25, 0.05},
            WageType.DAILY, new double[]{0.50, 0.35, 0.12, 0.03},
            WageType.RANDOM, new double[]{0.25, 0.35, 0.30, 0.10}
    );

    /**
     * 퍼블릭 API — 최소 매개변수만 사용
     */
    public static String pickCounterparty(
            WageType wageType,
            int occupationCode,
            String jobTitle,
            long userId
    ) {
        String cat = OCC_CAT.getOrDefault(occupationCode, "RETAIL");

        // 날짜가 없으므로 "안정적 재현"을 위해 사용자·직무·주기 기반 시드 사용
        long seed = Objects.hash(userId, cat, jobTitle == null ? "" : jobTitle, wageType.name());
        Random r = new Random(seed);

        // 주기별 규모 가중치로 버킷 선택
        String size = weightedPick(SIZE_BUCKET, SIZE_WEIGHTS.getOrDefault(wageType, SIZE_WEIGHTS.get(WageType.RANDOM)), r);

        // 주기별 스타일: DAILY/WEEKLY는 에이전시가 섞일 확률, RANDOM은 플랫폼이 섞일 확률을 부여
        switch (wageType) {
            case DAILY:
                if (maybeAgency(cat, r)) {
                    String agency = pick(AGENCY_POOL.get(cat), r);
                    if (agency != null) return agency;
                }
                return pickEmployerOrGrammar(cat, size, r);

            case WEEKLY:
            case BI_WEEKLY:
                if (r.nextDouble() < 0.25) { // 가끔 에이전시
                    String agency = pick(AGENCY_POOL.get(cat), r);
                    if (agency != null) return agency;
                }
                return pickEmployerOrGrammar(cat, size, r);

            case RANDOM:
                if (r.nextDouble() < 0.35) { // 프리랜서/긱 느낌
                    String platform = pick(PLATFORM_POOL.getOrDefault(cat, PLATFORM_POOL.get("IT")), r);
                    if (platform != null) return platform;
                }
                return pickEmployerOrGrammar(cat, size, r);

            case REGULAR:
            default:
                return pickEmployerOrGrammar(cat, size, r);
        }
    }

    // ===== 내부 유틸 =====

    private static boolean maybeAgency(String cat, Random r) {
        // DAILY는 에이전시 비중 높음
        if (AGENCY_POOL.containsKey(cat)) return r.nextDouble() < 0.60;
        return false;
    }

    private static String pickEmployerOrGrammar(String cat, String size, Random r) {
        Map<String, List<String>> bySize = EMPLOYER_POOL.getOrDefault(cat, EMPLOYER_POOL.get("RETAIL"));
        List<String> names = bySize.getOrDefault(size, List.of());
        if (!names.isEmpty()) {
            return names.get(r.nextInt(names.size()));
        }
        return grammar(cat, size, r); // 풀에 없으면 합성
    }

    private static String pick(List<String> pool, Random r) {
        return (pool == null || pool.isEmpty()) ? null : pool.get(r.nextInt(pool.size()));
    }

    private static String weightedPick(String[] items, double[] weights, Random r) {
        double sum = 0;
        for (double w : weights) sum += w;
        double roll = r.nextDouble() * sum, acc = 0;
        for (int i = 0; i < items.length; i++) {
            acc += weights[i];
            if (roll <= acc) return items[i];
        }
        return items[items.length - 1];
    }

    // 지역/날짜 없이도 그럴듯한 상호 합성 (카테고리·규모 기반)
    private static String grammar(String cat, String size, Random r) {
        String[] prefix = new String[]{"한빛", "새봄", "코리아", "월드", "메가", "포레스트"};
        String[] mid = switch (cat) {
            case "IT" -> new String[]{"소프트", "테크", "시스템즈", "데이터"};
            case "CONSTRUCTION" -> new String[]{"건설", "토건", "ENG", "개발"};
            case "FOOD" -> new String[]{"레스토랑", "브런치", "카페", "키친", "호텔"};
            case "EDU" -> new String[]{"에듀", "러닝", "아카데미", "튜터링"};
            case "HEALTH" -> new String[]{"의료", "메디", "재활", "헬스케어", "복지"};
            case "AGRI" -> new String[]{"영농", "과수", "팜", "아그로"};
            case "FINANCE" -> new String[]{"금융", "자산", "캐피탈", "인베스트"};
            case "RETAIL" -> new String[]{"리테일", "유통", "상사", "트레이드"};
            case "TRANSPORT" -> new String[]{"로지스", "물류", "항만", "퀵서비스"};
            case "MANUFACTURING" -> new String[]{"정밀", "머티리얼", "머신", "플랜트"};
            case "MINING" -> new String[]{"광업", "자원", "마이닝"};
            case "ELECTRICITY" -> new String[]{"에너지", "전기", "설비"};
            case "WASTE" -> new String[]{"환경", "클린", "리사이클"};
            case "REAL_ESTATE" -> new String[]{"리츠", "리빙", "자산관리"};
            case "PROFESSIONAL" -> new String[]{"컨설팅", "법률", "리서치", "엔지니어링"};
            case "SUPPORT_SERVICE" -> new String[]{"비즈서포트", "파견", "BPO", "오피스"};
            case "ARTS" -> new String[]{"컬처", "엔터", "스튜디오"};
            case "PERSONAL_SERVICE" -> new String[]{"서비스", "케어", "홈케어", "클린"};
            default -> new String[]{"홀딩스", "파트너스", "에셋", "컴퍼니"};
        };
        String[] suffix = switch (size) {
            case "SOHO" -> new String[]{"", " 연구소", " 사무소", " 센터"};
            case "SMB" -> new String[]{"", " 상사", " 유통"};
            case "MID" -> new String[]{"㈜"};
            default -> new String[]{"㈜", " 그룹㈜"};
        };
        return prefix[r.nextInt(prefix.length)] + mid[r.nextInt(mid.length)] + suffix[r.nextInt(suffix.length)];
    }
}

