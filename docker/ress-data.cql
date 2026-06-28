use ress;

insert into cas_properties (name, value) values ('hrcc.segments_by_imsi', '10');
insert into cas_properties (name, value) values ('metric.definition.all', '{"schema":"yes"}');

insert into hrcc_msisdn_imsi (msisdn, imsi) values ('msisdn-1', 'imsi1-1');

insert into hrcc_msisdn_imsi (msisdn, imsi) values ('msisdn-2', 'imsi2-1');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('msisdn-2', 'imsi2-2');

insert into hrcc_msisdn_imsi (msisdn, imsi) values ('msisdn-3', 'imsi3-1');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('msisdn-3', 'imsi3-2');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('msisdn-3', 'imsi3-3');

insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('m1', '1');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('m2', '2');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('m3', '3');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('m3', '4');

insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('3531', '0001');
insert into hrcc_msisdn_imsi ( msisdn, imsi) values ('555', '555');


insert into ress.hrcc_subscriber json '{"imsi": "2","age": null}';
insert into ress.hrcc_subscriber json '{"imsi": "1","age": null}';

insert into ress.hrcc_subscriber json '{"imsi": "imsi1-1" }';

insert into ress.hrcc_subscriber json '{"imsi": "imsi2-1"}';
insert into ress.hrcc_subscriber json '{"imsi": "imsi2-2"}';

insert into ress.hrcc_subscriber json '{"imsi": "imsi3-1"}';
insert into ress.hrcc_subscriber json '{"imsi": "imsi3-2"}';
insert into ress.hrcc_subscriber json '{"imsi": "imsi3-3"}';

insert into hrcc_historical_h_1 json '{"dt":"201608090400","sgm":"1","imsi":"3","timeid":"20160809040000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":4310}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090100","sgm":"3","imsi":"1","timeid":"20160809010000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":111}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090200","sgm":"1","imsi":"3","timeid":"20160809020000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":234}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090200","sgm":"7","imsi":"2","timeid":"20160809020000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":226}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090100","sgm":"7","imsi":"2","timeid":"20160809010000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":123}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090300","sgm":"3","imsi":"1","timeid":"20160809030000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":318}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090300","sgm":"7","imsi":"2","timeid":"20160809030000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":329}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090300","sgm":"1","imsi":"3","timeid":"20160809030000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":337}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1 json '{"dt":"201608090200","sgm":"3","imsi":"1","timeid":"20160809020000","metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":215}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';

insert into hrcc_historical_h_1i json '{"dt":"201608090400","sgm":"1","imsi":"3","timeid":20160809040000,"metric_ces_scores_distribution_for_video":"0x7c000000026d65747269634964002d00000069626d6161665f636f756e745f6f665f737563627965636e745f62795f766f6c74655f646972656374696f6e0004636f756e7465727300320000000330002a00000002627265616b646f776e00030000004d54000476616c7565000c0000001030000100000000000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":4310}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090100","sgm":"3","imsi":"1","timeid":20160809010000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":111}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090200","sgm":"1","imsi":"3","timeid":20160809020000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":234}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090200","sgm":"7","imsi":"2","timeid":20160809020000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":226}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090100","sgm":"7","imsi":"2","timeid":20160809010000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":123}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090300","sgm":"3","imsi":"1","timeid":20160809030000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":318}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090300","sgm":"7","imsi":"2","timeid":20160809030000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":329}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090300","sgm":"1","imsi":"3","timeid":20160809030000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":337}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
insert into hrcc_historical_h_1i json '{"dt":"201608090200","sgm":"3","imsi":"1","timeid":20160809020000,"metric_ces_scores_distribution_for_video":"0x13000000106100010000001062000200000000","metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":215}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';

insert into hrcc_historical_d_1 json '{"dt": "201107310000", "sgm": "7", "imsi": "234304100455762", "timeid": "20110731000000", "metric_ces_scores_distribution_for_video":null,"metric_count_of_pdp_create_attempts":"{\"m2\":222}","metric_count_of_successful_pdp_create":"{\"m1\":215}","metric_mb_of_data_for_top_10_applicaton_used":null,"metric_number_of_mo_calls_attempts_for_top_12_cell_sites":null}';
