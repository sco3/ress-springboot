package com.tnf.cas.db;

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

import javax.ws.rs.NotFoundException;
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
import com.tnf.cas.provider.BadParameters;

import sco.common.db.BlobberRegistry;
import sco.common.properties.Dt;
import sco.common.properties.Sgm;

import org.testcontainers.containers.CassandraContainer;


import test.spring.non.scannable.TestCassandraContextCfg;

public class TestDockerCassandra {
	private static AnnotationConfigWebApplicationContext mApp;
	private static Cluster mCluster;
	private static int mPort9042;
	private static Session mSession;
	private static CassandraContainer<?> mCassandra;

	@BeforeClass
	public static void startup() throws Exception {
		DbHistoricalFinder.VALIDATE_NUMERIC_IMSI_MSISDN = false;

		try {
			mCassandra = new CassandraContainer<>("cassandra:3.0.9");
			mCassandra.start();
		} catch (IllegalStateException e) {
			Assume.assumeTrue(false);
			return;
		}

		try {
			Sgm.setBase(10);

			Dt dt = Dt.singleton();
			dt.mTableToTimePeriodMap = new HashMap<String, String>();
			dt.mTableToTimePeriodMap.put("hrcc_historical_h_1", "H");
			dt.mTableToTimePeriodMap.put("hrcc_historical_h_1i", "H");
			dt.mTableToTimePeriodMap.put("hrcc_historical_d_1", "D");

			mPort9042 = mCassandra.getMappedPort(9042);
			out.println("Port: " + mPort9042);

			DbSession.setDynamicPort(mPort9042);
			mApp = new AnnotationConfigWebApplicationContext();
			mApp.setConfigLocations(//
					TestCassandraContextCfg.class.getName() //
			);
			mApp.refresh();
			DbSession db = mApp.getBean(DbSession.class);
			mSession = db.getObject();
			out.println(mSession.getCluster().getMetadata().getKeyspaces());
			mCluster = mSession.getCluster();

			new CqlLexer("cc.cql").process(mSession);
			assertNotNull(mCluster.getMetadata().getKeyspace("cc"));

			new CqlLexer("tnf.cql").process(mSession);
			assertNotNull(mCluster.getMetadata().getKeyspace("tnf"));

			new CqlLexer("tnf-data.cql").process(mSession);

			assertNotNull(//
					mCluster.getMetadata()//
							.getKeyspace("tnf")//
							.getTable("hrcc_historical_d_1")//
			);
			assertNotNull(//
					mCluster.getMetadata()//
							.getKeyspace("tnf")//
							.getTable("hrcc_msisdn_imsi")//
			);

		} catch (Exception e) {
			fail("Cassandra init failed." + e.getMessage());
		}
	}

	@Test
	public void testIntVsString() throws Exception {

		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);
		assertNotNull(finder.getImsiResolver());
		Session session = finder.getSession();
		{
			ColumnMetadata col = session.getCluster().getMetadata().getKeyspace("cc").getTable("cci").getColumn("IMSI");
			assertNotNull(col);
		}
		{
			ColumnMetadata col = session.getCluster().getMetadata().getKeyspace("cc").getTable("cci").getColumn("imsi");
			assertNotNull(col);
		}
		ResultSet rs = session.execute("select * from cc.cci limit 1");
		for (Row row : rs) {
			DataType imsiType = rs.getColumnDefinitions().getType("imsi");
			if (imsiType.equals(DataType.bigint())) {
				{
					long imsi = row.getLong("imsi");
					System.out.println(imsi);
					assertEquals(2, imsi);
				}
				{
					long imsi = row.getLong("IMSI");
					System.out.println(imsi);
					assertEquals(2, imsi);
				}
			}
		}

		ArrayList<Object> ints = new ArrayList<Object>();
		ints.add(new Long(1));
		ints.add(new Long(2));
		ints.add(new Long(3));

		Statement select = QueryBuilder//
				.select()//
				.from("cc", "cci") //
				.where(in("timeid", ints))//
				.and(in("imsi", ints));

		System.out.println(select);
		rs = session.execute(select);
		for (Row row : rs) {
			System.out.println(row);
		}

	}

	@Test
	public void test() throws Exception {
		Session sess = mSession;
		{
			sess.execute("use cc");
			ResultSet rs = sess.execute("select * from cc");

			Consumer<Row> c = new Consumer<Row>() {
				@Override
				public void accept(Row t) {
					ColumnDefinitions cols = rs.getColumnDefinitions();
					for (int i = 0; i < cols.size(); i++) {
						out.print(" " + cols.getName(i));
						out.print(" " + cols.getType(i));
						out.println(" " + t.getObject(i));
					}
				}
			};

			rs.forEach(c);
		}
		{
			ResultSet rows = sess.execute("select json * from tnf.hrcc_subscriber");
			for (Row row : rows) {
				out.println(row.getString(0));
			}
		}
		sess.execute("use tnf");
	}

	@Test
	public void testImsiResolver() {
		DbImsiResolver resolver = mApp.getBean(DbImsiResolver.class);
		{
			Set<String> ms = resolver.find(Sets.newHashSet("1")).getImsis();
			System.out.println(ms);
			assertNotNull(ms);
			assertEquals(0, ms.size());
		}
		{
			Set<String> ms = resolver.find(//
					Sets.newHashSet("msisdn-1")//
			).getImsis();
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
			Set<String> ms = resolver.find(//
					Sets.newHashSet("msisdn-2", "msisdn-3")//
			).getImsis();
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

			String profiles = profiler.getProfilesForImsis(//
					Sets.newHashSet("1")//
			);
			System.out.println(profiles);
			ObjectMapper om = new ObjectMapper();
			JsonNode o = om.readTree(profiles);
			System.out.println(o);
			assertEquals(1, o.size());
			assertEquals("1", o.get(0).get("imsi").asText());
		}
		{
			String profiles = profiler.getProfilesForImsis(//
					Sets.newHashSet("1", "2")//
			);
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
			String profiles = profiler.getProfiles(//
					null, Sets.newHashSet("msisdn1", "msisdn3")//
			);
			System.out.println(profiles);
			ObjectMapper om = new ObjectMapper();
			JsonNode o = om.readTree(profiles);
			System.out.println(o);
			assertEquals(0, o.size());
		}

	}

	@Test
	public void testProfiler() throws Exception {
		{
			SubProfiler profiler = mApp.getBean(DbSubProfiler.class);
			processProfiler(profiler);
		}
		{
			SubProfiler profiler = new DummySubProfiler();
			processProfiler(profiler);
		}
	}

	@Test
	public void testSchemaGenerator() {

		DbSubProfileShemaGenerator gen = mApp.getBean(//
				DbSubProfileShemaGenerator.class//
		);
		gen.setKeySpace("tnf");
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
			KeyspaceMetadata keyspace = mCluster.getMetadata().getKeyspace("cc");
			assertNotNull(keyspace);
		}
		{
			JsonNode o = gen.generateSchema("cc", "cc");
			System.out.println(o);
			assertNotNull(o);
			JsonNode props = o.path("items").path("properties");
			{
				KeyspaceMetadata keyspace = mCluster.getMetadata().getKeyspace("cc");
				assertNotNull(keyspace);
			}

			assertEquals(3, props.size());
			String exp = ("{" //
					+ "'imsi':{'type':'string'}," //
					+ "'age':{'type':'integer'}," //
					+ "'msisdn':{'type':'string'}"//
					+ "}" //
			);
			assertEquals(exp.replace('\'', '"'), props.toString());

			JsonNode reqs = o.path("items").path("required");
			assertEquals(3, reqs.size());

			exp = "['imsi','age','msisdn']";
			assertEquals(exp.replace('\'', '"'), reqs.toString());

		}
	}

	@Test
	public void testSearchDaily() throws Exception {
		DbHistoricalFinder finder = mApp.getBean(DbHistoricalFinder.class);
		assertNotNull(finder);

		String result = finder.find( //
				Sets.newHashSet("234304100455762"), //
				Sets.newHashSet(), //
				"d", "20110731000000", "20110732000000"//
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
			Select select = QueryBuilder//
					.select().json()//
					.from("hrcc_historical_h_1");
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

				Statement del = QueryBuilder//
						.delete()//
						.from("hrcc_historical_h_1") //
						.where(eq("dt", dt))//
						.and(eq("sgm", oldSgm))//
						.and(eq("imsi", imsi))//
						.and(eq("timeid", timeId));
				System.out.println(del);
				session.execute(del);
			}
			System.out.println("====");
			for (JsonNode row : data) {
				String jsonStr = om.writeValueAsString(row);
				String cql = format(//
						"insert into hrcc_historical_h_1 json '%s'", //
						jsonStr//
				);
				System.out.println(cql + ";");
				session.execute(cql);
			}
			System.out.println("====");
		}
		{

			Select select = QueryBuilder//
					.select().json()//
					.from("hrcc_historical_h_1");

			ResultSet rs = session.execute(select);

			int cnt = 0;
			for (Row row : rs) {
				for (Definition def : rs.getColumnDefinitions().asList()) {
					System.out.print(row.getString(def.getName()) + " ");
				}
				System.out.println();
				cnt++;
			}
			assertEquals(9, cnt);
		}
		{
			Statement select = QueryBuilder//
					.select().json()//
					.from("hrcc_historical_h_1")//
					.where(in("imsi", //
							Arrays.asList("1", "2", "3"))) //
					.and(in("sgm", Arrays.asList(//
							getSgm("1"), getSgm("2"), getSgm("3"))))
					.and(in("timeid", Arrays.asList(//
							20160809010000L, 20160809020000L)))//
					.and(in("dt", Arrays.asList(//
							getDt("20160809010000", "hrcc_historical_h_1"),
							getDt("20160809020000", "hrcc_historical_h_1"))));

			ResultSet rs = session.execute(select);

			int cnt = 0;
			for (Row row : rs) {
				for (Definition def : rs.getColumnDefinitions().asList()) {
					System.out.print(row.getString(def.getName()) + " ");
				}
				System.out.println();
				cnt++;
			}
			assertEquals(5, cnt);
			DbHistoricalFinder f = new DbHistoricalFinder();
			select = QueryBuilder//
					.select()//
					.from("hrcc_historical_h_1")//
					.where(in("imsi", //
							Arrays.asList("1", "2", "3"))) //
					.and(in("sgm", Arrays.asList(//
							getSgm("1"), getSgm("2"), getSgm("3"))))
					.and(in("timeid", Arrays.asList(//
							20160809010000L, 20160809020000L)))//
					.and(in("dt", Arrays.asList(//
							getDt("20160809010000", "hrcc_historical_h_1"),
							getDt("20160809020000", "hrcc_historical_h_1"))));

			{
				StringBuilder s = new StringBuilder();
				rs = session.execute(select);
				f.processFlat(rs, s);
				System.out.println(s);

			}

		}

		{

			String result = finder.find( //
					Sets.newHashSet("1"), //
					Sets.newHashSet("m2", "m3"), //
					"h", "20160809010000", "20160809030000"//
			);
			ObjectMapper om = new ObjectMapper();
			ArrayNode o = (ArrayNode) om.readTree(result);
			ObjectWriter writer = om.writerWithDefaultPrettyPrinter();
			System.out.println(writer.writeValueAsString(o));
			assertEquals(5, o.size());
		}

		{ // test long imis and timeid
			Switcher sw = mApp.getBean(Switcher.class);
			String old = sw.getHourlyTable();
			sw.setHourlyTable("hrcc_historical_h_1i");
			String result = finder.find( //
					Sets.newHashSet("1"), //
					Sets.newHashSet("m2", "m3"), //
					"h", "20160809010000", "20160809030000"//
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
			String result = finder.find( //
					Sets.newHashSet("1"), //
					Sets.newHashSet("m2", "m3"), //
					"h", "20160809010000", "20160809030000"//
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
			Object r = null;
			try {
				r = finder.find(imsis, msisdns, "h", "20171201", "20171202");

			} catch (NotFoundException e) {
				System.out.println(e.getResponse().getStatus());
				System.out.println(e.getMessage());
			}
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

		{ // use async
			String[] imsis = { "1", "2", "3" };
			Long[] timeids = { 20160809010000L, 20160809020000L };

			String sql = ("" //
					+ " select * from hrcc_historical_h_1 " //
					+ " where " //
					+ " dt = ?" //
					+ " and sgm = ?" //
					+ " and timeid = ?" //
					+ " and imsi = ? " //
			);
			List<ResultSetFuture> futures = new LinkedList<ResultSetFuture>();
			for (Long timeid : timeids) {
				for (String imsi : imsis) {
					ResultSetFuture f = session.executeAsync(//
							sql, getDt(timeid.toString(), "hrcc_historical_h_1"), getSgm(imsi), timeid, imsi//
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

		ResultSet rs = session.execute("select bson from cc.bson where id = 1");
		for (Row row : rs) {

			ByteBuffer bytes = row.getBytes(0);
			byte[] buf = new byte[bytes.remaining()];
			bytes.get(buf);

			String s = DatatypeConverter.printHexBinary(buf);
			System.out.println(s);
			assertNotNull(s);
			assertEquals("13000000106100010000001062000200000000", s);
			s = BlobberRegistry.getBlobber().restore(buf);
			System.out.println(s);
			assertNotNull(s);
			assertEquals(//
					"{\"a\":1,\"b\":2}", //
					s.replaceAll(Pattern.quote(" "), "")//
			);
		}
	}

	@AfterClass
	public static void teardown() {
		if (mCassandra != null) {
			mCassandra.stop();
		}
	}
}
