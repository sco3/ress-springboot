package com.tnf.cas.db;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author dz
 * 
 *         output sample:
 * 
 *         <pre>
 
 {
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "imsi": {
        "type": "string"
      },
      "address": {
        "type": "string"
      },
      "age": {
        "type": "integer"
      },
      "current_plan": {
        "type": "string"
      },
      "name": {
        "type": "string"
      }
    },
    "required": [
      "imsi",
      "address",
      "age",
      "current_plan",
      "name"
    ]
  }
 }
 *         </pre>
 *
 */
@Component
public class DbSubProfileShemaGenerator extends DbTableSchemaGenerator
        implements ProfileSchemaGenerator {

    public JsonNode generateSchema() {
        JsonNode result = generateSchema(getKeySpace(), "hrcc_subscriber");
        return result;
    }
}
