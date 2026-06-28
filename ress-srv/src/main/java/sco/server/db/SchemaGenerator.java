package sco.server.db;

import com.fasterxml.jackson.databind.JsonNode;

public interface SchemaGenerator {
    JsonNode generateSchema();

    JsonNode generateSchema(String schema, String table);
}
