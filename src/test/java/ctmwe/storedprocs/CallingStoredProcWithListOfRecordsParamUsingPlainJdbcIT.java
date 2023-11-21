package ctmwe.storedprocs;

import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
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
class CallingStoredProcWithListOfRecordsParamUsingPlainJdbcIT {
    private static OracleContainer tc;

    @Autowired
    protected TransactionTemplate transactionTemplate;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

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

        this.transactionTemplate.executeWithoutResult(status->{
            // Create an instance of your custom stored procedure
            MyStoredProcedure myStoredProcedure = new MyStoredProcedure(jdbcTemplate);

            // Create an instance of your custom Oracle type
            try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource(), "required datasource").getConnection()) {
                Struct customTypeParamA = createStructCustomTypeObject(connection, "val1_from_java__"+ randomInt(), "val2_from_java__"+ randomInt());
                Struct customTypeParamB = createStructCustomTypeObject(connection, "val3_from_java__"+ randomInt(), "val4_from_java__"+ randomInt());

                Array array =
                        createArrayCustomTypeObject(connection, new Object[]{customTypeParamA, customTypeParamB});

                myStoredProcedure.execute(array);
                System.out.println("done");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, String.format("%s.%s", "aaa", "sample_table")), is(2));
        });
    }

    private static Struct createStructCustomTypeObject(Connection connection, String value1, String value2) {
        try {
            // Define the type name (case-sensitive) for CustTestRecord
            String typeName = "AAA.CUSTOM_RECORD";     //for oracle, it must be all caps!!!

            return connection.createStruct(typeName, new Object[]{value1, value2});
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Array createArrayCustomTypeObject(Connection connection, Object[] elements) throws SQLException {
        OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
        ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("AAA.CUSTOM_RECORD_LIST", oracleConnection);
        return new ARRAY(descriptor, oracleConnection, elements);
    }

    public static class MyStoredProcedure extends StoredProcedure {

        public MyStoredProcedure(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, "PROCESS_CUSTOM_RECORD_LIST"); //has to be uppercase!

            // Declare the parameters and their types
            declareParameter(new SqlParameter("whatever, irrelevant", Types.ARRAY));

            // Compile the stored procedure.
            compile();
        }
    }

    protected static int randomInt() {
        return new Random().nextInt(0, Integer.MAX_VALUE);
    }
}
