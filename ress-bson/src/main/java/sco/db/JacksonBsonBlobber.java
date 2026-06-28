package sco.db;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;
import sco.common.db.Blobber;

public class JacksonBsonBlobber implements Blobber {
    static Logger mTrace = LoggerFactory
            .getLogger(JacksonBsonBlobber.class.getSimpleName());
    ObjectMapper mOm = new ObjectMapper();
    ObjectMapper mBm = new ObjectMapper(new BsonFactory());

    public JacksonBsonBlobber() {

        mBm.registerModule(new BsonModule());
    }

    @Override
    public byte[] save(String s) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            JsonNode o = mOm.readTree(s);
            mBm.writeValue(bout, o);
        } catch (Exception e) {
            mTrace.error("Cannot save: ", e);
        }
        return bout.toByteArray();
    }

    @Override
    public String restore(byte[] buf) {
        String result = null;
        try {
            JsonNode o = mBm.readTree(buf);
            result = mOm.writeValueAsString(o);
        } catch (Exception e) {
            mTrace.error("Cannot restore: {}", e);
        }
        return result;
    }
}
