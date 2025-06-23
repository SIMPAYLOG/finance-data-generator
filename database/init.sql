CREATE TYPE gender AS ENUM ('M', 'F');
CREATE TYPE income_level_value AS ENUM ('L', 'M', 'H');
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
    behavior_type                     VARCHAR(50),
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
    id           BIGSERIAL PRIMARY KEY,
    profile_id   BIGINT UNIQUE NOT NULL REFERENCES user_behavior_profiles (id) ON DELETE CASCADE,
    age          SMALLINT,
    gender       gender,
    balance      BIGINT,
    debt         BIGINT,
    income_level income_level_value,
    job          VARCHAR(100)
);

CREATE TABLE income_level
(
    id                                BIGSERIAL PRIMARY KEY,
    income_range                      JSONB,
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
    non_consumption_expenditure       DECIMAL(5, 2),
    surplus_rate_pct                  DECIMAL(5, 2),
    avg_propensity_to_consume_pct     DECIMAL(5, 2)
);

CREATE TABLE occupational_wages
(
    id                                BIGSERIAL PRIMARY KEY,
    occupation                        varchar(20),
    monthly_wage                      Integer
)