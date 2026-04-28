package com.yas.sampledata.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class SqlScriptExecutorTest {

    @Test
    void executeScriptsForSchema_runsSqlScripts() throws Exception {
        DataSource dataSource = createDataSource("jdbc:h2:mem:sampledata_utils;DB_CLOSE_DELAY=-1");
        SqlScriptExecutor executor = new SqlScriptExecutor();

        executor.executeScriptsForSchema(dataSource, "PUBLIC", "classpath*:db/product/*.sql");

        assertTrue(tableExists(dataSource, "SAMPLE_PRODUCT"));
    }

    private DataSource createDataSource(String url) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private boolean tableExists(DataSource dataSource, String tableName) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet tables = connection.getMetaData().getTables(null, "PUBLIC", tableName, null);
            return tables.next();
        }
    }
}
