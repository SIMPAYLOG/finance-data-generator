CREATE TYPE gender AS ENUM ('M', 'F');
CREATE TYPE transaction_frequency_pattern AS ENUM ('daily', 'weekly', 'bi-weekly', 'random');
CREATE TABLE user_behavior_profiles
(
    id                                BIGSERIAL PRIMARY KEY,
    spending_probability              DECIMAL(2, 1),
    transaction_frequency_pattern     transaction_frequency_pattern,
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
    gender            gender,
    balance           BIGINT,
    debt              BIGINT,
    occupation_code   SMALLINT,
    job_number        SMALLINT
);

CREATE TABLE income_level
(
    id                                BIGSERIAL PRIMARY KEY,
    asset_range                       JSONB,
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
    consumption_expenditure           DECIMAL(5, 2),
    non_consumption_expenditure       DECIMAL(5, 2),
    surplus_rate_pct                  DECIMAL(5, 2),
    avg_propensity_to_consume_pct     DECIMAL(5, 2)
);

CREATE TABLE occupational_wages
(
    id                                BIGSERIAL PRIMARY KEY,
    occupation                        varchar(100),
    monthly_wage                      Integer
);

-- 데이터 insert 부분
INSERT INTO occupational_wages (occupation, monthly_wage) VALUES
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

INSERT INTO income_level (
    id,
    asset_range,
    groceries_non_alcoholic_beverages,
    alcoholic_beverages_tobacco,
    clothing_footwear,
    housing_utilities_fuel,
    household_goods_services,
    health,
    transportation,
    communication,
    recreation_culture,
    education,
    food_accommodation,
    other_goods_services,
    consumption_expenditure,
    non_consumption_expenditure,
    surplus_rate_pct,
    avg_propensity_to_consume_pct
) VALUES
      (1, '{ "min": 0, "max": 1829332 }', 22.50, 2.05, 3.41, 22.00, 3.64, 10.64, 6.45, 5.62, 3.74, 0.95, 12.42, 6.31, 88.03, 11.97, -101.30, 201.30),
      (2, '{ "min": 1829333, "max": 3048887 }', 21.87, 1.80, 3.80, 19.43, 3.90, 12.24, 7.08, 5.47, 3.88, 1.51, 12.05, 6.67, 86.15, 13.85, -7.50, 107.50),
      (3, '{ "min": 3048888, "max": 4268441 }', 18.77, 1.71, 4.15, 17.73, 3.83, 9.68, 8.38, 5.93, 4.49, 2.76, 14.23, 7.23, 82.1, 17.9, 12.40, 87.60),
      (4, '{ "min": 4268442, "max": 5467996 }', 17.03, 1.72, 4.47, 16.44, 3.81, 8.55, 9.60, 6.36, 4.69, 3.54, 16.21, 7.58, 79.86, 20.14, 22.10, 77.90),
      (5, '{ "min": 5487997, "max": 6097773 }', 15.90, 1.54, 4.44, 14.70, 4.11, 8.02, 10.37, 6.30, 5.65, 4.89, 16.30, 7.78, 77.90, 22.10, 23.70, 76.30),
      (6, '{ "min": 6097774, "max": 7927105 }', 15.54, 1.48, 4.74, 12.77, 4.44, 7.17, 11.25, 6.41, 5.54, 6.78, 15.93, 7.93, 75.27, 24.73, 24.20, 75.80),
      (7, '{ "min": 7927106, "max": 9146660 }', 14.88, 1.26, 4.74, 11.50, 4.15, 7.61, 11.31, 6.40, 5.61, 8.36, 16.10, 8.09, 74.83, 25.17, 27.60, 72.40),
      (8, '{ "min": 9146661, "max": 12195546 }', 14.30, 1.12, 4.89, 10.56, 4.36, 7.05, 12.13, 5.95, 5.73, 10.10, 15.96, 7.85, 72.74, 17.26, 30.30, 69.70),
      (9, '{ "min": 12195547, "max": 18293319 }', 13.91, 1.04, 5.08, 9.78, 4.30, 7.35, 11.73, 5.29, 7.02, 10.40, 15.86, 8.26, 36.30, 70.39, 29.61, 63.70),
      (10, '{ "min": 18293320, "max": 180000000 }', 11.66, 0.82, 5.23, 8.90, 4.56, 6.42, 14.73, 4.73, 8.32, 10.55, 15.06, 9.03, 64.16, 35.84, 47.00, 53.00);
