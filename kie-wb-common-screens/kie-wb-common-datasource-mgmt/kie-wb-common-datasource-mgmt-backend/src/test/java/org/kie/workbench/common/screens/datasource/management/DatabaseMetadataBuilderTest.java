/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.internal.SystemEventListener;
import org.kie.workbench.common.screens.datasource.management.metadata.CatalogMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.DatabaseMetadata;
import org.kie.workbench.common.screens.datasource.management.util.DatabaseMetadataBuilder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class DatabaseMetadataBuilderTest {

    private static final String DATA_BASE_PRODUCT_NAME = "DATA_BASE_PRODUCT_NAME";

    @Mock
    private Connection conn;

    @Mock
    private java.sql.DatabaseMetaData sqlDatabaseMetaData;

    DatabaseMetadataBuilder builder;

    @Mock
    ResultSet rs;

    List<CatalogMetadata> catalogs = new ArrayList<>(  );

    @Before
    public void setup() {
        builder = new DatabaseMetadataBuilder( conn, DatabaseMetadata.TableType.ALL );

        catalogs.add( new CatalogMetadata( "catalog1" ) );
        catalogs.add( new CatalogMetadata( "catalog2" ) );
    }

    //@Test
    public void testBuild() {
        try {
            Class.forName( "org.postgresql.Driver" );
            //Connection conn = DriverManager.getConnection( "jdbc:postgresql://localhost:5432/livespark", "livespark", "livespark" );
            Connection conn = DriverManager.getConnection( "jdbc:postgresql://localhost:5432/livespark", "user1", "user1" );

            DatabaseMetadataBuilder builder = new DatabaseMetadataBuilder( conn, DatabaseMetadata.TableType.TABLE );
            DatabaseMetadata databaseMetadata = builder.build( );


            int i = 0;
        } catch ( Exception e ) {
            fail( e.getMessage() );
        }
    }

    @Test
    public void test() throws Exception {
        when ( conn.getMetaData() ).thenReturn( sqlDatabaseMetaData );
        when ( sqlDatabaseMetaData.getDatabaseProductName() ).thenReturn( DATA_BASE_PRODUCT_NAME );

        when ( sqlDatabaseMetaData.getCatalogs() ).thenReturn( rs );
        when( rs.next() ).thenReturn( true );

        DatabaseMetadata databaseMetadata = builder.build();


    }



}
