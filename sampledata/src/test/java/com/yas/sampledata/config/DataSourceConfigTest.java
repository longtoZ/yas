package com.yas.sampledata.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class DataSourceConfigTest {

    @Test
    void dataSources_andJdbcTemplates_areCreated() {
        DataSourceConfig config = new DataSourceConfig();
        ReflectionTestUtils.setField(config, "driverClassName", "org.h2.Driver");
        ReflectionTestUtils.setField(config, "username", "sa");
        ReflectionTestUtils.setField(config, "password", "");
        ReflectionTestUtils.setField(config, "productUrl", "jdbc:h2:mem:sampledata_product;DB_CLOSE_DELAY=-1");
        ReflectionTestUtils.setField(config, "mediaUrl", "jdbc:h2:mem:sampledata_media;DB_CLOSE_DELAY=-1");

        DataSource product = config.productDataSource();
        DataSource media = config.mediaDataSource();
        JdbcTemplate productJdbc = config.jdbcProduct(product);
        JdbcTemplate mediaJdbc = config.jdbcMedia(media);

        assertNotNull(product);
        assertNotNull(media);
        assertNotNull(productJdbc);
        assertNotNull(mediaJdbc);
    }
}
