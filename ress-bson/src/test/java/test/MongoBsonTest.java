package test;

import static java.lang.System.out;
import static java.util.HexFormat.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.bson.RawBsonDocument;
import org.junit.Test;

import sco.common.db.Blobber;
import sco.common.db.BlobberRegistry;

public class MongoBsonTest {

	@Test
	public void test() {
		String hex = "7c000000026d65747269634964002d00000069626d6161665f636f756e745f6f665f737563627965636e745f62795f766f6c74655f646972656374696f6e0004636f756e7465727300320000000330002a00000002627265616b646f776e00030000004d54000476616c7565000c0000001030000100000000000000";
		byte[] buf = of().parseHex(hex);
		out.println(new String(buf));
		RawBsonDocument d = new RawBsonDocument(buf);
		String json = d.toJson();
		out.println(json);
		assertTrue(json.contains("\"metricId\""));
		BlobberRegistry r = BlobberRegistry.getBlobber();
		Iterator<Blobber> iter = r.loader.iterator();
		while (iter.hasNext()) {
			Blobber b = iter.next();
			out.println(b.getClass().getSimpleName());
		}
		byte[] b2 = r.save(json);
		String s2 = of().formatHex(b2);
		assertEquals(hex.toLowerCase(), s2.toLowerCase());

	}

}
