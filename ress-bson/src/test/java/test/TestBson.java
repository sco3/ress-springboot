package test;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;
import sco.common.db.Blobber;
import sco.common.db.BlobberRegistry;

public class TestBson {

	@Test
	public void test() throws Exception {
		String hex = "7c000000026d65747269634964002d00000069626d6161665f636f756e745f6f665f737563627965636e745f62795f766f6c74655f646972656374696f6e0004636f756e7465727300320000000330002a00000002627265616b646f776e00030000004d54000476616c7565000c0000001030000100000000000000";

		byte[] buf = HexFormat.of().parseHex(hex);
		out.println(new String(buf));
		BsonFactory f = new BsonFactory();
		ObjectMapper om = new ObjectMapper(f);
		om.registerModule(new BsonModule());
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};

		HashMap<String, Object> o = om.readValue(buf, typeRef);
		out.println("Got " + o);

		JsonNode node = om.readTree(buf);
		out.println(node.toString());

	}

	@Test
	public void test2() throws Exception {
		String hex = "14020000126631007b55070000000000106632003387000010663300c89f000010663400d6bc0000026635000b00000044756e676c69736f6e2e0010663600be07010010663700b2370100106638005e7001001066390058b30100026631300015000000666f726d65726c7920737570706f73656420746f0010663131000b600200106631320099ce02001066313300405103001066313400a9eb0300026631350001000000001066313600cf7905001066313700ae7806001066313800e5a507001066313900e109090002663230000d000000524544454c49424552415445001066323100c79f0c001066323200a80d0000106632330024100000106632340013130000026632350001000000001066323600a41a000010663237007c1f00001066323800352500001066323900f92b00000266333000010000000010663331006b3d00001066333200964800001066333300c8550000106633340061650000026633350001000000001066333600988d0000106633370057a700001066333800c4c500001066333900b9e90000026634300001000000001066343100714601001066343200cb8101001066343300f0c701001066343400d61a020002663435000b000000322e20284d6563682e2900106634360096f0020010663437006c7903001066343800221b0400106634390040da04000266353000150000004465666e3a2041207368616674206f72206578630000";
		byte[] buf = HexFormat.of().parseHex(hex);
		BsonFactory f = new BsonFactory();
		ObjectMapper om = new ObjectMapper(f);
		om.registerModule(new BsonModule());
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};

		HashMap<String, Object> o = om.readValue(buf, typeRef);
		out.println("Got " + o);

		JsonNode node = om.readTree(buf);
		out.println(node.toString());

	}

	@Test
	public void testService() {
		ServiceLoader<Blobber> loader = ServiceLoader.load(Blobber.class);
		for (Iterator<Blobber> iter = loader.iterator(); iter.hasNext();) {
			Blobber impl = iter.next();
			out.println(impl);
		}

		BlobberRegistry reg = BlobberRegistry.getBlobber();
		byte[] b = reg.save("{\"a\":1,\"b\":2}");
		out.println(HexFormat.of().formatHex(b));
		Assert.assertNotNull(b);
		String s = reg.restore(b);
		out.println("restore: " + s);
	}
}
