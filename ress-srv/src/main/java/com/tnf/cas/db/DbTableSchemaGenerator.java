package com.tnf.cas.db;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class DbTableSchemaGenerator implements SchemaGenerator {
    static final String TEMPLATE = ("" //
            + "{" //
            + "  '$schema': 'http://json-schema.org/draft-04/schema#',"//
            + "  'type': 'array',"//
            + "  'items': {"//
            + "    'type': 'object',"//
            + "    'properties':{ "//
            + "    },"//
            + "    'required': ["//
            + "    ]"//
            + "  }" //
            + "}"//
    );

    protected static Logger mTrace = LoggerFactory
            .getLogger(DbSubProfileShemaGenerator.class);
    @Autowired
    protected Session mSession;

    private String mKeySpace;
    protected HashMap<String, JsonNode> mCache = new HashMap<String, JsonNode>();
    /**
     * <pre>
    3.5.  JSON Schema primitive types
    
    JSON Schema defines seven primitive types for JSON values:
    
    array
        A JSON array. 
    boolean
        A JSON boolean. 
    integer
        A JSON number without a fraction or exponent part. 
    number
        Any JSON number. Number includes integer. 
    null
        The JSON null value. 
    object
        A JSON object. 
    string
        A JSON string.
     * </pre>
     */
    @SuppressWarnings("serial")
    static HashMap<String, String> mTypeMap = new HashMap<String, String>() {
        {
            put("ascii", "string");
            put("bigint", "integer");
            put("boolean", "boolean");
            put("counter", "integer");
            put("decimal", "number");
            put("double", "number");
            put("float", "number");
            put("inet", "string");
            put("int", "integer");
            put("text", "string");
            put("timestamp", "string");
            put("uuid", "string");
            put("timeuuid", "string");
            put("varchar", "string");
            put("varint", "integer");
        }
    };

    public DbTableSchemaGenerator() {
        super();
    }

    protected String getType(ColumnMetadata col) {
        String type = col.getType().toString().toLowerCase();
        String result = mTypeMap.get(type);
        if (result == null) {
            result = type;
        }
        return result;
    }

    public String getKeySpace() {
        return mKeySpace;
    }

    @Value("${cassandra.keyspace}")
    public void setKeySpace(String keySpace) {
        mKeySpace = keySpace;
    }

    @Override
    public JsonNode generateSchema(String schema, String table) {
        String key = schema + ":" + table;
        JsonNode node = mCache.get(key);
        if (node == null) {
            ObjectMapper om = new ObjectMapper();
            try {
                String t = TEMPLATE.replace('\'', '"');
                mTrace.debug(t);
                node = om.readTree(t);

                if (mSession != null) {
                    Metadata m = mSession.getCluster().getMetadata();
                    KeyspaceMetadata keyspace = m.getKeyspace(schema);
                    if (keyspace != null) {
                        TableMetadata tbl = keyspace.getTable(table);
                        List<ColumnMetadata> cols = tbl.getColumns();
                        JsonNode propsNode = node.path("items")
                                .path("properties");
                        JsonNode reqNode = node.path("items").path("required");
                        if (propsNode instanceof ObjectNode //
                                && reqNode instanceof ArrayNode//
                        ) {
                            ObjectNode propsObj = (ObjectNode) propsNode;
                            ArrayNode reqArray = (ArrayNode) reqNode;
                            for (ColumnMetadata col : cols) {
                                ObjectNode colNode = om.createObjectNode();
                                colNode.put("type", getType(col));
                                propsObj.set(col.getName(), colNode);
                                reqArray.add(col.getName());
                            }
                        }
                    }
                    mCache.put(key, node);
                }
            } catch (Exception e) {
                mTrace.error("{}", e);
            }
        }
        return node;
    }

}