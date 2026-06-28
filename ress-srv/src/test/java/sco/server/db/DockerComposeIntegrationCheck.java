package sco.server.db;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static java.lang.String.format;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static sco.common.properties.Dt.getDt;
import static sco.common.properties.Sgm.getSgm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import sco.common.db.BlobberRegistry;
import sco.common.properties.Dt;
import sco.common.properties.Sgm;
import sco.server.db.DbHistoricalFinder;
import sco.server.db.DbImsiResolver;
import sco.server.db.DbSession;
import sco.server.db.DbSubProfileShemaGenerator;
import sco.server.db.DbSubProfiler;
import sco.server.db.HistoricalSchemaGenerator;
import sco.server.db.SubProfiler;
import sco.server.db.Switcher;
import sco.server.provider.BadParameters;

import test.spring.non.scannable.CassandraContextCfg;

/**
 * Integration test that runs against docker-compose stack.
 * Reuses the same scenarios as DockerCassandraTest but connects
 * to Cassandra on localhost:9042 (docker-compose) instead of Testcontainers.
 *
 * Prerequisites:
 *   docker-compose up -d   (Cassandra must be running and init scripts completed)
 */
public class DockerComposeIntegrationCheck {
	private static AnnotationConfigWebApplicationContext mApp;
	private static Cluster mCluster;
	private static Session mSession;

	@BeforeClass
	public static void startup() throws Exception {
		DbHistoricalFinder.VALIDATE_NUMERIC_IMSI_MSISDN = false;

		try {
			Sgm.setBase(10);

			Dt dt = Dt.singleton();
			dt.mTableToTimePeriodMap = new HashMap<String, String>();
			dt.mTableToTimePeriodMap.put("hrcc_historical_h_1", "H");
			dt.mTableToTimePeriodMap.put("hrcc_historical_h_1i", "H");
			dt.mTableToTimePeriodMap.put("hrcc_historical_d_1", "D");

			DbSession.setDynamicPort(9042);
			mApp = new AnnotationConfigWebApplicationContext();
			mApp.setConfigLocations(CassandraContextCfg.class.getName());
			mApp.refresh();

			DbSession db = mApp.getBean(DbSession.class);
			mSession = db.getObject();
			assertNotNull("Cassandra session must not be null", mSession);

			out.println(mSession.getCluster().getMetadata().getKeyspaces());
			mCluster = mSession.getCluster();

			KeyspaceMetadata ressKeyspace = mCluster.getMetadata().getKeyspace("ress");
			assertNotNull("Keyspace 'ress' must exist (run docker-compose init first)", ressKeyspace);

			assertNotNull(ressKeyspace.getTable("hrcc_subscriber"));
			assertNotNull(ressKeyspace.getTable("hrcc_msisdn_imsi"));
			assertNotNull(ressKeyspace.getTable("hrcc_historical_h_1"));
			assertNotNull(ressKeyspace.getTable("hrcc_historical_h_1i"));
			assertNotNull(ressKeyspace.getTable("hrcc_historical_d_1"));
			assertNotNull(ressKeyspace.getTable("cas_properties"));

		} catch (Exception e) {
			fail("Cassandra init failed. Is docker-compose running?\n" + e.getMessage());
		}
	}

	@Test
	public void testConnection() throws Exception {
		assertNotNull(mSession);
		KeyspaceMetadata ks = mCluster.getMetadata().getKeyspace("ress");
		assertNotNull(ks);
		out.println("Connected to keyspace: ress");
		out.println("Tables: " + ks.getTables().size());
		assertTrue("Should have at least 5 tables", ks.getTables().size() >= 5);
	}

	@Test
	public void testIntVsString() throws Exception {
		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);
		assertNotNull(finder.getImsiResolver());
		Session session = finder.getSession();

		{
			ColumnMetadata col = session.getCluster().getMetadata()
					.getKeyspace("ress").getTable("hrcc_subscriber").getColumn("imsi");
			assertNotNull(col);
		}

		ResultSet rs = session.execute("select * from ress.hrcc_subscriber limit 1");
		int cnt = 0;
		for (Row row : rs) {
			out.println("imsi=" + row.getString("imsi"));
			cnt++;
		}
		assertTrue("Should have at least 1 subscriber", cnt >= 1);
	}

	@Test
	public void testImsiResolver() {
		DbImsiResolver resolver = mApp.getBean(DbImsiResolver.class);
		assertNotNull(resolver);

		{
			Set<String> ms = resolver.find(Sets.newHashSet("1")).getImsis();
			System.out.println(ms);
			assertNotNull(ms);
			assertEquals(0, ms.size());
		}
		{
			Set<String> ms = resolver.find(Sets.newHashSet("msisdn-1")).getImsis();
			System.out.println(ms);
			assertNotNull(ms);
			assertEquals(1, ms.size());
			assertTrue(ms.contains("imsi1-1"));
		}
		{
			Set<String> ms = resolver.find(Sets.newHashSet("msisdn-2")).getImsis();
			System.out.println(ms);
			assertNotNull(ms);
			assertEquals(2, ms.size());
			assertTrue(ms.contains("imsi2-1"));
			assertTrue(ms.contains("imsi2-2"));
		}
		{
			Set<String> ms = resolver.find(Sets.newHashSet("msisdn-2", "msisdn-3")).getImsis();
			System.out.println(ms);
			assertNotNull(ms);
			assertEquals(5, ms.size());
			assertTrue(ms.contains("imsi2-1"));
			assertTrue(ms.contains("imsi2-2"));
			assertTrue(ms.contains("imsi3-1"));
			assertTrue(ms.contains("imsi3-2"));
			assertTrue(ms.contains("imsi3-2"));
		}
	}

	private void processProfiler(SubProfiler profiler) throws Exception {
		{
			String profiles = profiler.getProfilesForImsis(Sets.newHashSet("1"));
			System.out.println(profiles);
			ObjectMapper om = new ObjectMapper();
			JsonNode o = om.readTree(profiles);
			System.out.println(o);
			assertEquals(1, o.size());
			assertEquals("1", o.get(0).get("imsi").asText());
		}
		{
			String profiles = profiler.getProfilesForImsis(Sets.newHashSet("1", "2"));
			System.out.println(profiles);
			ObjectMapper om = new ObjectMapper();
			JsonNode o = om.readTree(profiles);
			System.out.println(o);
			assertEquals(2, o.size());
			HashSet<String> set = new HashSet<String>();
			for (Iterator<JsonNode> i = o.elements(); i.hasNext();) {
				JsonNode node = i.next();
				set.add(node.get("imsi").asText());
			}
			assertTrue(set.contains("1"));
			assertTrue(set.contains("2"));
		}
		{
			String profiles = profiler.getProfiles(null, Sets.newHashSet("msisdn1", "msisdn3"));
			System.out.println(profiles);
			ObjectMapper om = new ObjectMapper();
			JsonNode o = om.readTree(profiles);
			System.out.println(o);
			assertEquals(0, o.size());
		}
	}

	@Test
	public void testProfiler() throws Exception {
		SubProfiler profiler = mApp.getBean(DbSubProfiler.class);
		processProfiler(profiler);
	}

	@Test
	public void testSchemaGenerator() {
		DbSubProfileShemaGenerator gen = mApp.getBean(DbSubProfileShemaGenerator.class);
		gen.setKeySpace("ress");

		{
			JsonNode o = gen.generateSchema();
			System.out.println(o);
			assertNotNull(o);
			JsonNode props = o.path("items").path("properties");
			assertEquals(38, props.size());

			JsonNode field = props.get("address_2");
			String type = field.get("type").textValue();
			assertEquals("string", type);

			JsonNode reqs = o.path("items").path("required");
			assertEquals(38, reqs.size());
		}

		{
			KeyspaceMetadata keyspace = mCluster.getMetadata().getKeyspace("ress");
			assertNotNull(keyspace);
		}
	}

	@Test
	public void testSearchDaily() throws Exception {
		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);

		String result = finder.find(
				Sets.newHashSet("234304100455762"),
				Sets.newHashSet(),
				"d", "20110731000000", "20110732000000"
		);

		ObjectMapper om = new ObjectMapper();
		ArrayNode o = (ArrayNode) om.readTree(result);
		ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
		System.out.println(writer.writeValueAsString(o));
		assertEquals(1, o.size());
	}

	@Test
	public void testSearch() throws Exception {
		assertEquals("3", getSgm("1"));

		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);
		assertNotNull(finder.getImsiResolver());
		Session session = finder.getSession();
		assertNotNull(session);

		{
			Select select = QueryBuilder.select().json().from("hrcc_historical_h_1");
			ResultSet rs = session.execute(select);
			List<JsonNode> data = new ArrayList<JsonNode>();
			ObjectMapper om = new ObjectMapper();
			for (Row row : rs) {
				String jsonStr = row.getString(0);
				System.out.println(jsonStr);
				JsonNode o = om.readTree(jsonStr);

				String dt = o.get("dt").asText();
				String oldSgm = o.get("sgm").asText();
				String imsi = o.get("imsi").asText();
				long timeId = o.get("timeid").asLong();

				String newSgm = getSgm(o.get("imsi").asText());
				((ObjectNode) o).put("sgm", newSgm);
				data.add(o);

				Statement del = QueryBuilder.delete().from("hrcc_historical_h_1")
						.where(eq("dt", dt))
						.and(eq("sgm", oldSgm))
						.and(eq("imsi", imsi))
						.and(eq("timeid", timeId));
				System.out.println(del);
				session.execute(del);
			}
			System.out.println("====");
			for (JsonNode row : data) {
				String jsonStr = om.writeValueAsString(row);
				String cql = format("insert into hrcc_historical_h_1 json '%s'", jsonStr);
				System.out.println(cql + ";");
				session.execute(cql);
			}
			System.out.println("====");
		}
		{
			Select select = QueryBuilder.select().json().from("hrcc_historical_h_1");
			ResultSet rs = session.execute(select);

			int cnt = 0;
			for (Row row : rs) {
				for (Definition def : rs.getColumnDefinitions().asList()) {
					System.out.print(row.getString(def.getName()) + " ");
				}
				System.out.println();
				cnt++;
			}
			assertEquals(10, cnt);
		}

		{
			String result = finder.find(
					Sets.newHashSet("1"),
					Sets.newHashSet("m2", "m3"),
					"h", "20160809010000", "20160809030000"
			);
			ObjectMapper om = new ObjectMapper();
			ArrayNode o = (ArrayNode) om.readTree(result);
			ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
			System.out.println(writer.writeValueAsString(o));
			assertEquals(5, o.size());
		}

		{
			Switcher sw = mApp.getBean(Switcher.class);
			String old = sw.getHourlyTable();
			sw.setHourlyTable("hrcc_historical_h_1i");
			String result = finder.find(
					Sets.newHashSet("1"),
					Sets.newHashSet("m2", "m3"),
					"h", "20160809010000", "20160809030000"
			);
			ObjectMapper om = new ObjectMapper();
			ArrayNode o = (ArrayNode) om.readTree(result);
			ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
			System.out.println(writer.writeValueAsString(o));
			assertEquals(5, o.size());
			sw.setHourlyTable(old);
			for (int i = 0; i < o.size(); i++) {
				JsonNode m = o.get(i).get("metrics");
				if (m instanceof ArrayNode) {
					ArrayNode am = (ArrayNode) m;
					boolean found = false;
					for (int j = 0; j < am.size(); j++) {
						JsonNode mt = am.get(j);
						System.out.println(mt);
						if (mt.toString().equals("{\"a\":1,\"b\":2}")) {
							found = true;
							break;
						}
					}
					if (!found) {
						fail("bson was not found");
					}
				} else {
					fail("Should be array node");
				}
			}
		}

		{
			String cols = "imsi,timeid,metric_ces_scores_distribution_for_video,metric_count_of_pdp_create_attempts";
			finder.setColumnListCommasSeparated(cols);
			String result = finder.find(
					Sets.newHashSet("1"),
					Sets.newHashSet("m2", "m3"),
					"h", "20160809010000", "20160809030000"
			);
			ObjectMapper om = new ObjectMapper();
			ArrayNode o = (ArrayNode) om.readTree(result);
			ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
			System.out.println(writer.writeValueAsString(o));
			assertEquals(5, o.size());
			for (int i = 0; i < o.size(); i++) {
				JsonNode item = o.get(i);
				Assert.assertNotNull(item.get("imsi"));
				Assert.assertNotNull(item.get("time"));
				Assert.assertNotNull(item.get("metrics"));
			}
		}
	}

	@Test
	public void testNumbers() throws Exception {
		String msg = "";
		int status = 0;
		boolean old = DbHistoricalFinder.VALIDATE_NUMERIC_IMSI_MSISDN;
		DbHistoricalFinder.VALIDATE_NUMERIC_IMSI_MSISDN = true;
		try {
			DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
			Set<String> imsis = new HashSet<String>();
			imsis.add("0001");
			Set<String> msisdns = new HashSet<String>();
			msisdns.add("3531");
			Object r = finder.find(imsis, msisdns, "h", "20171201", "20171202");
			System.out.println(r);
			assertEquals("", msg);
			imsis.add("i-1");
			msisdns.add("m-1");
			r = finder.find(imsis, msisdns, "h", "20171201", "20171202");
		} catch (BadParameters e) {
			msg += e.getMessage();
			System.out.println(e.getMessage());
			status = e.getResponse().getStatus();
		} finally {
			DbHistoricalFinder.VALIDATE_NUMERIC_IMSI_MSISDN = old;
		}
		System.out.println(msg);
		assertEquals("Wrong value for Parameter imsi, Wrong value for Parameter msisdn", msg);
		assertEquals(400, status);
	}

	@Test
	public void testSearchAsync() throws Exception {
		assertEquals("3", getSgm("1"));

		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);
		assertNotNull(finder.getImsiResolver());
		Session session = finder.getSession();

		{
			String[] imsis = { "1", "2", "3" };
			Long[] timeids = { 20160809010000L, 20160809020000L };

			String sql = (" select * from hrcc_historical_h_1 "
					+ " where "
					+ " dt = ?"
					+ " and sgm = ?"
					+ " and timeid = ?"
					+ " and imsi = ? "
			);
			List<ResultSetFuture> futures = new LinkedList<ResultSetFuture>();
			for (Long timeid : timeids) {
				for (String imsi : imsis) {
					ResultSetFuture f = session.executeAsync(
							sql, getDt(timeid.toString(), "hrcc_historical_h_1"), getSgm(imsi), timeid, imsi
					);
					futures.add(f);
				}
			}
			ListenableFuture<List<ResultSet>> rss = Futures.successfulAsList(futures);
			int cnt = 0;
			for (ResultSet rs : rss.get()) {
				for (Row row : rs) {
					for (Definition def : rs.getColumnDefinitions().asList()) {
						if (def.getType().equals(DataType.text())) {
							System.out.print(row.getString(def.getName()) + " ");
						} else if (def.getType().equals(DataType.bigint())) {
							System.out.print(row.getLong(def.getName()) + " ");
						}
					}
					System.out.println();
					cnt++;
				}
			}
			assertEquals(5, cnt);
		}
	}

	@Test
	public void testHistSchemaGenerator() throws Exception {
		HistoricalSchemaGenerator gen = mApp.getBean(HistoricalSchemaGenerator.class);
		String str = gen.getSchema();
		System.out.println(str);
		assertEquals("{\"schema\":\"yes\"}", str);
	}

	@Test
	public void testBson() {
		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);
		assertNotNull(finder.getImsiResolver());
		Session session = finder.getSession();

		// Check that hrcc_historical_h_1i has BSON data
		ResultSet rs = session.execute(
				"SELECT metric_ces_scores_distribution_for_video FROM ress.hrcc_historical_h_1i LIMIT 1"
		);
		for (Row row : rs) {
			ByteBuffer bytes = row.getBytes(0);
			if (bytes != null) {
				byte[] buf = new byte[bytes.remaining()];
				bytes.get(buf);

				String s = DatatypeConverter.printHexBinary(buf);
				System.out.println(s);
				assertNotNull(s);
				assertTrue("BSON hex should not be empty", s.length() > 0);

				s = BlobberRegistry.getBlobber().restore(buf);
				System.out.println(s);
				assertNotNull(s);
			}
		}
	}

	@AfterClass
	public static void teardown() {
		if (mApp != null) {
			mApp.close();
		}
	}
}
