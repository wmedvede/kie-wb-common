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

package org.kie.workbench.common.screens.datasource.management.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.kie.workbench.common.screens.datasource.management.metadata.CatalogMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.DatabaseMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.SchemaMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.TableMetadata;

public class DatabaseMetadataBuilder {

    private Connection conn;

    private DatabaseMetadata.TableType types[];

    public DatabaseMetadataBuilder( Connection conn, DatabaseMetadata.TableType... types ) {
        this.conn = conn;
        this.types = types;
    }

    public DatabaseMetadata build( ) throws Exception {

        List< CatalogMetadata > catalogs = new ArrayList<>( );
        List< SchemaMetadata > schemas = new ArrayList<>( );
        List< TableMetadata > tables = new ArrayList<>( );
        DatabaseMetadata result = new DatabaseMetadata( );

        try {
            java.sql.DatabaseMetaData sqlMetadata = conn.getMetaData( );

            result.setDatabaseProductName( sqlMetadata.getDatabaseProductName( ) );
            result.setDatabaseProductVersion( sqlMetadata.getDatabaseProductVersion( ) );
            result.setDriverName( sqlMetadata.getDriverName( ) );
            result.setDriverVersion( sqlMetadata.getDriverVersion( ) );
            result.setDriverMinorVersion( sqlMetadata.getDriverMinorVersion( ) );

            ResultSet rs = sqlMetadata.getCatalogs( );
            while ( rs.next( ) ) {
                catalogs.add( new CatalogMetadata( rs.getString( "TABLE_CAT" ) ) );
            }
            rs.close( );
            result.setCatalogs( catalogs );

            rs = sqlMetadata.getSchemas( );
            while ( rs.next( ) ) {
                schemas.add( new SchemaMetadata( rs.getString( "TABLE_CATALOG" ), rs.getString( "TABLE_SCHEM" ) ) );
            }
            rs.close( );
            result.setSchemas( schemas );

            rs = sqlMetadata.getTables( null, null, "%", toSqlTypes( types ) );
            TableMetadata tableMetadata;
            while ( rs.next( ) ) {
                tableMetadata = new TableMetadata( rs.getString( "TABLE_CAT" ),
                        rs.getString( "TABLE_SCHEM" ), rs.getString( "TABLE_NAME" ), rs.getString( "TABLE_TYPE" ) );
                tables.add( tableMetadata );
            }
            rs.close( );
            result.setTables( tables );

            return result;
        } catch ( Exception e ) {
            throw new Exception( "It was not possible to read connection metadata due to the following error: " + e.getMessage( ) );
        } finally {
            try {
                conn.close( );
            } catch ( Exception e ) {
                //we are not interested in raising this error case.
            }
        }
    }

    private static String[] toSqlTypes( DatabaseMetadata.TableType types[] ) {
        HashSet< DatabaseMetadata.TableType > typesSet = new HashSet<>( );
        String result[] = null;
        if ( types != null ) {
            for ( int i = 0; i < types.length; i++ ) {
                typesSet.add( types[ i ] );
            }
        }
        if ( !typesSet.isEmpty( ) && !typesSet.contains( DatabaseMetadata.TableType.ALL ) ) {
            result = new String[ typesSet.size( ) ];
            int i = 0;
            for ( DatabaseMetadata.TableType type : typesSet ) {
                result[ i++ ] = toSqlType( type );
            }
        }
        return result;
    }

    private static String toSqlType( DatabaseMetadata.TableType type ) {
        switch ( type ) {
            case TABLE:
                return "TABLE";
            case SYSTEM_TABLE:
                return "SYSTEM TABLE";
            case VIEW:
                return "VIEW";
            case SYSTEM_VIEW:
                return "SYSTEM VIEW";
            case SEQUENCE:
                return "SEQUENCE";
        }
        return null;
    }
}