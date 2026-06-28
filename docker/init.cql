CREATE KEYSPACE IF NOT EXISTS tnf WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;

CREATE TABLE IF NOT EXISTS tnf.hrcc_subscriber (
    imsi text PRIMARY KEY,
    account_number text,
    address_1 text,
    address_2 text,
    age int,
    brand text,
    car_owner text,
    churned_flag text,
    contract text,
    contract_type text,
    corporate_account_name text,
    corporate_group_name text,
    credit_rating_grade text,
    customer_type text,
    customer_value text,
    data_offering text,
    date_of_birth text,
    dob text,
    employment_status text,
    estimated_income text,
    ethnicity text,
    first_name text,
    fixmobilebundleflag text,
    gender text,
    handset text,
    highest_level_of_education text,
    last_updated_time text,
    marital_status text,
    monthly_arpu float,
    msisdn text,
    number_of_children text,
    plan text,
    plan_name text,
    sms_offering text,
    subscriber_name text,
    surname text,
    tenure text,
    voice_offering text
);

CREATE TABLE IF NOT EXISTS tnf.cas_properties (
    name text PRIMARY KEY,
    value text
);

CREATE TABLE IF NOT EXISTS tnf.hrcc_msisdn_imsi (
    msisdn text,
    imsi text,
    PRIMARY KEY (msisdn, imsi)
) WITH CLUSTERING ORDER BY (imsi ASC);

CREATE TABLE IF NOT EXISTS tnf.hrcc_historical_d_1 (
    dt text,
    sgm text,
    imsi text,
    timeid bigint,
    metric_ces_scores_distribution_for_video text,
    metric_count_of_pdp_create_attempts text,
    metric_count_of_successful_pdp_create text,
    metric_mb_of_data_for_top_10_applicaton_used text,
    metric_number_of_mo_calls_attempts_for_top_12_cell_sites text,
    PRIMARY KEY ((dt, sgm), imsi, timeid)
) WITH CLUSTERING ORDER BY (imsi ASC, timeid ASC);

CREATE TABLE IF NOT EXISTS tnf.hrcc_historical_h_1 (
    dt text,
    sgm text,
    imsi text,
    timeid bigint,
    metric_ces_scores_distribution_for_video text,
    metric_count_of_pdp_create_attempts text,
    metric_count_of_successful_pdp_create text,
    metric_mb_of_data_for_top_10_applicaton_used text,
    metric_number_of_mo_calls_attempts_for_top_12_cell_sites text,
    PRIMARY KEY ((dt, sgm), imsi, timeid)
) WITH CLUSTERING ORDER BY (imsi ASC, timeid ASC);

CREATE TABLE IF NOT EXISTS tnf.hrcc_historical_h_1i (
    dt text,
    sgm text,
    imsi text,
    timeid bigint,
    metric_ces_scores_distribution_for_video blob,
    metric_count_of_pdp_create_attempts text,
    metric_count_of_successful_pdp_create text,
    metric_mb_of_data_for_top_10_applicaton_used text,
    metric_number_of_mo_calls_attempts_for_top_12_cell_sites text,
    PRIMARY KEY ((dt, sgm), imsi, timeid)
) WITH CLUSTERING ORDER BY (imsi ASC, timeid ASC);

CREATE TABLE IF NOT EXISTS tnf.flyway_info (
    table_name text PRIMARY KEY,
    version text
);
