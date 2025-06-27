CREATE TABLE user_behavior_profiles
(
    id                                BIGSERIAL PRIMARY KEY,
    preference_id                     Integer,
    spending_probability              DECIMAL(2, 1),
    transaction_frequency_pattern     varchar(50),
    income_day_of_month               SMALLINT,
    auto_transfer_ratio               DECIMAL(2, 1),
    average_saving_amount_ratio       DECIMAL(2, 1),
    average_spending_amount_range     JSONB,
    active_hours                      JSONB,
    income_value                      DECIMAL(15, 2),
    asset_value                       DECIMAL(15, 2),
    groceries_non_alcoholic_beverages DECIMAL(5, 2),
    alcoholic_beverages_tobacco       DECIMAL(5, 2),
    clothing_footwear                 DECIMAL(5, 2),
    housing_utilities_fuel            DECIMAL(5, 2),
    household_goods_services          DECIMAL(5, 2),
    health                            DECIMAL(5, 2),
    transportation                    DECIMAL(5, 2),
    communication                     DECIMAL(5, 2),
    recreation_culture                DECIMAL(5, 2),
    education                         DECIMAL(5, 2),
    food_accommodation                DECIMAL(5, 2),
    other_goods_services              DECIMAL(5, 2),
    non_consumption_expenditure       DECIMAL(5, 2),
    surplus_rate_pct                  DECIMAL(5, 2),
    avg_propensity_to_consume_pct     DECIMAL(5, 2)
);

CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    profile_id        BIGINT UNIQUE NOT NULL REFERENCES user_behavior_profiles (id) ON DELETE CASCADE,
    age               SMALLINT,
    gender            char(1),
    balance           BIGINT,
    debt              BIGINT,
    occupation_code   SMALLINT,
    job_number        SMALLINT
);

CREATE TABLE occupational_wages
(
    id                                BIGSERIAL PRIMARY KEY,
    occupation                        varchar(100),
    monthly_wage                      Integer
);

-- 데이터 insert 부분
INSERT INTO occupational_wages (occupation, monthly_wage)
VALUES
    ('농업, 임업 및 어업', 2878),
    ('광업', 4117),
    ('제조업', 3889),
    ('전기, 가스, 증기 및 공기 조절 공급업', 5248),
    ('수도, 하수 및 폐기물 처리, 원료 재생업', 3899),
    ('건설업', 3182),
    ('도매 및 소매업', 3322),
    ('운수 및 창고업', 3532),
    ('숙박 및 음식점업', 1830),
    ('정보통신업', 4501),
    ('금융 및 보험업', 5344),
    ('부동산업', 2788),
    ('전문, 과학 및 기술 서비스업', 4489),
    ('사업시설 관리, 사업 지원 및 임대 서비스업', 3111),
    ('교육 서비스업', 3106),
    ('보건업 및 사회복지 서비스업', 2632),
    ('예술, 스포츠 및 여가 관련 서비스업', 2575),
    ('협회 및 단체, 수리 및 기타 개인 서비스업', 2547);