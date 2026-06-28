package com.tnf.cas.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sco.server.db.DbSubProfileShemaGenerator;
import sco.server.db.ProfileSchemaGenerator;

public class DummySubProfileGenerator extends DbSubProfileShemaGenerator
        implements ProfileSchemaGenerator {

    String mSchema = "{'$schema':'http://json-schema.org/draft-04/schema#','type':'array','items':{'type':'object','properties':{'imsi':{'type':'string'},'address':{'type':'string'},'age':{'type':'string'},'current_plan':{'type':'string'},'dummy':{'type':'string'}},'required':['imsi','address','age','current_plan','dummy']}}";

    public JsonNode generateSchema() {
        ObjectMapper om = new ObjectMapper();
        JsonNode node = om.createObjectNode();
        try {
            node = om.readTree(mSchema.replace('\'', '"'));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return node;
    }
}
