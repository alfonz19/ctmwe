package ctmwe.storedprocs;

import ctmwe.repositories.jpa.main.CustomRecordDTO;
import ctmwe.repositories.jpa.main.CustomRecordRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.MountableFile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableConfigurationProperties
@Slf4j
class CallingStoredProcWithCustomTypeUsingHibernateIT {

    private static OracleContainer tc;

    @Autowired
    protected TransactionTemplate transactionTemplate;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomRecordRepository custTest2Repository;

    @BeforeAll
    static void beforeAll() {
        tc = createTC().withReuse(true);
        tc.start();
        System.out.println(String.format("Using jdbc: %s", tc.getJdbcUrl().toString()));
    }

    @DynamicPropertySource
    protected static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        Supplier<Object> userName = () -> "aaa";
        Supplier<Object> password = () -> "aaa";
        Supplier<Object> getJdbcUrl = tc::getJdbcUrl;
        Supplier<Object> driverClassName = () -> "oracle.jdbc.OracleDriver";

        registry.add("spring.datasource.url", getJdbcUrl);
        registry.add("spring.datasource.username", userName);
        registry.add("spring.datasource.password", password);
        registry.add("spring.datasource.driver-class-name", driverClassName);
    }

    private static OracleContainer createTC() {
        return new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
                .withUsername("aaa")
                .withPassword("aaa")
                .withCopyFileToContainer(MountableFile.forClasspathResource(
                                "testcontainers/oracle/init.sql", 0777),
                        "/container-entrypoint-initdb.d/init001.sql")
                .withStartupTimeout(Duration.ofMinutes(1));
    }

    @Test
    void test() {
        jdbcTemplate.update(String.format("truncate table %s.%s", "aaa", "sample_table"));
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, String.format("%s.%s", "aaa", "sample_table")), is(0));

        transactionTemplate.executeWithoutResult(status->{
            custTest2Repository.callWithSingleCustomType(new CustomRecordDTO("aaa", "bbb"));
        });

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, String.format("%s.%s", "aaa", "sample_table")), is(1));
    }

    protected static int randomInt() {
        return new Random().nextInt(0, Integer.MAX_VALUE);
    }
}
