CREATE TABLE user_behavior_profiles
(
    id                                BIGSERIAL PRIMARY KEY,
    preference_id                     Integer,
    wage_type                         varchar(50),
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
    occupation_name varchar(50),
    job_number        SMALLINT
);
