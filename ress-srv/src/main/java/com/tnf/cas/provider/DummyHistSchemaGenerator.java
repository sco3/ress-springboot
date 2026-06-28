package com.tnf.cas.provider;

import com.tnf.cas.db.HistoricalSchemaGenerator;

public class DummyHistSchemaGenerator implements HistoricalSchemaGenerator {

    String mSchema = "{'$schema':'http://json-schema.org/draft-04/schema#','type':'array','items':{'type':'object','properties':{'imsi':{'type':'string'},'msisdn':{'type':'string'}},'required':['imsi','msisdn']}}";

    public String getSchema() {
        return mSchema.replace('\'', '"');
    }

}
