package com.yas.sampledata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.yas.sampledata.viewmodel.SampleDataVm;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class SampleDataServiceTest {

    @Test
    void createSampleData_returnsSuccessMessage() {
        DataSource dataSource = createDataSource("jdbc:h2:mem:sampledata_service;DB_CLOSE_DELAY=-1");
        SampleDataService service = new SampleDataService(dataSource, dataSource);

        SampleDataVm result = service.createSampleData();

        assertEquals("Insert Sample Data successfully!", result.message());
    }

    private DataSource createDataSource(String url) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
