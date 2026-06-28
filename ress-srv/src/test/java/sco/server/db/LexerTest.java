package sco.server.db;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LexerTest {

    @Test
    public void test() {
        List<String> lines = Arrays.asList(//
                "CREATE KEYSPACE if not exists tnf WITH replication =" //
                        + " {'class': 'SimpleStrategy', 'replication_factor': '1'} " //
                        + " AND durable_writes = true;", //
                "create table asdf ( ", //
                "a int ,", //
                "b text", //
                ") WITH bloom_filter_fp_chance = 0.01\n"
                        + "    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}\n"
                        + "    AND comment = ''\n" + "    AND compaction = {'class': " //
                        + "'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'," //
                        + " 'max_threshold': '32', 'min_threshold': '4'}\n"
                        + "    AND compression = {'chunk_length_in_kb': '64', " //
                        + " 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}\n"
                        + "    AND crc_check_chance = 1.0\n"
                        + "    AND dclocal_read_repair_chance = 0.1\n"
                        + "    AND default_time_to_live = 0\n"
                        + "    AND gc_grace_seconds = 864000\n"
                        + "    AND max_index_interval = 2048\n"
                        + "    AND memtable_flush_period_in_ms = 0\n"
                        + "    AND min_index_interval = 128\n"
                        + "    AND read_repair_chance = 0.0\n"
                        + "    AND speculative_retry = '99PERCENTILE';\n");
        CqlLexer lexer = new CqlLexer(lines);
        List<String> stmts = lexer.getStatements();
        for (String stmt : stmts) {
            System.out.println(stmt);
        }
        assertEquals(2, stmts.size());
    }

}
