package com.tnf.cas.db;

import org.bson.ByteBuf;
import org.bson.RawBsonDocument;

public class MongoBsonBlobber implements Blobber {
    public byte[] save(String s) {
        RawBsonDocument d = RawBsonDocument.parse(s);
        ByteBuf bb = d.getByteBuffer();
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    public String restore(byte[] b) {
        RawBsonDocument d = new RawBsonDocument(b);
        return d.toJson();
    }

}
