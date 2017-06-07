// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.service.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.service.rest.dto.PropertiesDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.RestAssured;

public class JdbcDatasetTestIT {

    public static final String USER = "root";

    public static final String PASS = "Fghjrcbvfwbz";

    public static final String URL = "jdbc:mysql://192.168.99.100:3306/";

    public static final String DB_NAME = "test_db";

    public static final String TABLE_NAME = "test_table";

    public static final String CREATE_DB = "CREATE DATABASE " + DB_NAME;

    public static final String DROP_DB = "DROP DATABASE " + DB_NAME;

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(id INTEGER NOT NULL, "
            + "name VARCHAR(30) NOT NULL, " + "salary FLOAT(3) NOT NULL, " + "flag BOOLEAN NOT NULL, "
            + "created_timestamp TIMESTAMP NOT NULL, " + "PRIMARY KEY(id))";

    public static final String INSERT = "INSERT INTO " + TABLE_NAME + " VALUES(?,?,?,?,?)";

    public static final String JDBC_DATASTORE_PROPERTIES = "{\"dbTypes\":\"MYSQL\",\"jdbcUrl\":\"jdbc:mysql://192.168.99.100:3306/test_db\",\"userId\":\"root\",\"password\":\"Fghjrcbvfwbz\",\"@definitionName\":\"JDBCDatastore\"}";

    public static final String JDBC_DATASET_PROPERTIES = "{\"sourceType\":\"QUERY\",\"datastore\":\"JDBCDatastore\",\"@definitionName\":\"JDBCDataset\",\"main\":{\"schema\":{\"type\":\"record\",\"name\":\"EmptyRecord\",\"fields\":[]}},\"sql\":\"select * from test_table\"}";

    public static final String JDBC_DATASET_PROPERTIES_WITH_SCHEMA = "{\"sourceType\":\"QUERY\",\"datastore\":\"JDBCDatastore\",\"@definitionName\":\"JDBCDataset\",\"main\":{\"schema\":{\"type\":\"record\",\"name\":\"DYNAMIC\",\"fields\":[{\"name\":\"id\",\"type\":\"string\",\"talend.field.precision\":11,\"talend.field.dbType\":4,\"talend.field.dbColumnName\":\"id\"},{\"name\":\"name\",\"type\":\"string\",\"talend.field.length\":30,\"talend.field.dbType\":12,\"talend.field.dbColumnName\":\"name\"},{\"name\":\"salary\",\"type\":\"string\",\"talend.field.dbType\":7,\"talend.field.dbColumnName\":\"salary\"},{\"name\":\"flag\",\"type\":\"string\",\"talend.field.dbType\":-7,\"talend.field.dbColumnName\":\"flag\"},{\"name\":\"created_timestamp\",\"type\":\"string\",\"talend.field.pattern\":\"yyyy-MM-dd HH:mm:ss.SSS\",\"talend.field.dbType\":93,\"talend.field.dbColumnName\":\"created_timestamp\"}]}},\"sql\":\"select * from test_table\"}";

    /**
     * Converts json string to {@link ObjectNode} instances
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://192.168.99.100";
        RestAssured.port = 8989;
        RestAssured.basePath = "/tcomp";
    }

    @Test
    public void setupDB() throws SQLException {
        // create connection
        try (Connection connection = DriverManager.getConnection(URL, USER, PASS)) {
            try (Statement statement = connection.createStatement()) {
                // create db
                statement.executeUpdate(CREATE_DB);
                // use created db
                connection.setCatalog(DB_NAME);
                // create table
                statement.executeUpdate(CREATE_TABLE);
            }
            // fill data
            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setInt(1, 1);
                statement.setString(2, "first");
                statement.setFloat(3, 1.23f);
                statement.setBoolean(4, true);
                statement.setTimestamp(5, new Timestamp(new Date().getTime()));
                statement.execute();

                statement.setInt(1, 2);
                statement.setString(2, "second");
                statement.setFloat(3, 4.56f);
                statement.setBoolean(4, false);
                statement.setTimestamp(5, new Timestamp(new Date().getTime()));
                statement.execute();
            }
        }
    }

    /**
     * Checks whether JDBCDatastore properties contains required db types:
     */
    @Test
    public void testGetPropertiesJDBCDatastore() {
        // get JDBCDatastore properties
        when() //
                .get("/properties/JDBCDatastore") //
                .then() //
                .body("jsonSchema.properties.dbTypes.enum", contains("MYSQL", "DERBY")); //

    }
    
    /**
     * Checks schema returned for JDBC connection
     * 
     * @throws IOException
     */
    @Test
    public void testGetDatasetSchema() throws IOException {
        // get JDBCDataset schema
        given().accept("application/json;charset=UTF-8") //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(getJdbcDatasetProperties()) //
                .contentType("application/json;charset=UTF-8") //
                .when() //
                .post("/runtimes/{datasetDefinitionName}/schema", "JDBCDataset") //
                .then() //
                .body("fields.name", contains("id", "name", "salary", "flag", "created_timestamp")); //
    }
    
    /**
     * Checks schema returned for JDBC connection
     * 
     * @throws IOException
     */
    @Test
    public void testGetDatasetData() throws IOException {
        // get JDBCDataset schema
        given().accept("application/json;charset=UTF-8") //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(getJdbcDatasetPropertiesWithSchema()) //
                .contentType("application/json;charset=UTF-8") //
                .when() //
                .post("/runtimes/{datasetDefinitionName}/data", "JDBCDataset") //
                .then() //
                .content(containsString("{\"id\":\"1\",\"name\":\"first\",\"salary\":\"1.23\",\"flag\":\"1\",\"created_timestamp\":"))
                .content(containsString("{\"id\":\"2\",\"name\":\"second\",\"salary\":\"4.56\",\"flag\":\"0\",\"created_timestamp\":"));
    }

    /**
     * Cleans database. Will be removed after using container
     * 
     * @throws SQLException
     */
    @Test
    public void teardown() throws SQLException {
        // create connections
        try (Connection connection = DriverManager.getConnection(URL, USER, PASS)) {
            try (Statement statement = connection.createStatement()) {
                // remove db
                statement.executeUpdate(DROP_DB);
            }
        }
    }

    private PropertiesDto getJdbcDatastoreProperties() throws java.io.IOException {
        PropertiesDto propertiesDto = new PropertiesDto();
        ObjectNode properties = objectMapper.readerFor(ObjectNode.class).readValue(JDBC_DATASTORE_PROPERTIES);
        propertiesDto.setProperties(properties);
        return propertiesDto;
    }

    /**
     * Builds JDBC Dataset properties with empty schema
     *
     * @return
     * @throws java.io.IOException
     */
    private PropertiesDto getJdbcDatasetProperties() throws java.io.IOException {
        PropertiesDto propertiesDto = new PropertiesDto();
        ObjectNode jdbcDatasetProperties = objectMapper.readerFor(ObjectNode.class).readValue(JDBC_DATASET_PROPERTIES);
        ObjectNode jdbcDatastoreProperties = objectMapper.readerFor(ObjectNode.class).readValue(JDBC_DATASTORE_PROPERTIES);
        propertiesDto.setProperties(jdbcDatasetProperties);
        propertiesDto.setDependencies(Collections.singletonList(jdbcDatastoreProperties));
        return propertiesDto;
    }

    private PropertiesDto getJdbcDatasetPropertiesWithSchema() throws java.io.IOException {
        PropertiesDto propertiesDto = new PropertiesDto();
        ObjectNode jdbcDatasetProperties = objectMapper.readerFor(ObjectNode.class).readValue(JDBC_DATASET_PROPERTIES_WITH_SCHEMA);
        ObjectNode jdbcDatastoreProperties = objectMapper.readerFor(ObjectNode.class).readValue(JDBC_DATASTORE_PROPERTIES);
        propertiesDto.setProperties(jdbcDatasetProperties);
        propertiesDto.setDependencies(Collections.singletonList(jdbcDatastoreProperties));
        return propertiesDto;
    }

}
