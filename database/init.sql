CREATE TABLE user_behavior_profiles
(
    id                                BIGSERIAL PRIMARY KEY,
    preference_id                     Integer,
    spending_probability              DECIMAL(2, 1),
    transaction_frequency_pattern     varchar(50),
    income_day_of_month               SMALLINT,
    auto_transfer_day_of_month        SMALLINT,
    active_hours                      JSONB,
    income_value                      DECIMAL(15, 2),
    asset_value                       DECIMAL(15, 2)
);

CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    profile_id        BIGINT UNIQUE NOT NULL REFERENCES user_behavior_profiles (id) ON DELETE CASCADE,
    age               SMALLINT,
    gender            char(1),
    balance           BIGINT,
    occupation_code   SMALLINT,
    job_number        SMALLINT
);
