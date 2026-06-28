package com.tnf.bis.common.sqlprocessor;

import java.nio.file.Path;

public interface ISqlProcessor {

    int update(String sql, String executionEngine, String extraConnectionProperties);

    int query(String sql, Path outputFile, String executionEngine, String extraConnectionProperties);
}
