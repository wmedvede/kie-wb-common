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

package org.kie.workbench.common.screens.datasource.management.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class DatabaseMetadata {

    public enum DatabaseType { H2, MYSQL, MARIADB, POSTGRESQL, ORACLE, SQLSERVER, DB2 }

    public enum TableType { ALL, TABLE, SYSTEM_TABLE,  VIEW, SYSTEM_VIEW, SEQUENCE }

    private DatabaseType databaseType;

    private String databaseProductName;

    private String databaseProductVersion;

    private String driverName;

    private String driverVersion;

    private int driverMajorVersion;

    private int driverMinorVersion;

    private List<SchemaMetadata> schemas = new ArrayList<>( );

    private List<CatalogMetadata> catalogs = new ArrayList<>( );

    public DatabaseMetadata( ) {
    }

    public DatabaseType getDatabaseType( ) {
        return databaseType;
    }

    public void setDatabaseType( DatabaseType databaseType ) {
        this.databaseType = databaseType;
    }

    public String getDatabaseProductName( ) {
        return databaseProductName;
    }

    public void setDatabaseProductName( String databaseProductName ) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseProductVersion( ) {
        return databaseProductVersion;
    }

    public void setDatabaseProductVersion( String databaseProductVersion ) {
        this.databaseProductVersion = databaseProductVersion;
    }

    public String getDriverName( ) {
        return driverName;
    }

    public void setDriverName( String driverName ) {
        this.driverName = driverName;
    }

    public String getDriverVersion( ) {
        return driverVersion;
    }

    public void setDriverVersion( String driverVersion ) {
        this.driverVersion = driverVersion;
    }

    public int getDriverMajorVersion( ) {
        return driverMajorVersion;
    }

    public void setDriverMajorVersion( int driverMajorVersion ) {
        this.driverMajorVersion = driverMajorVersion;
    }

    public int getDriverMinorVersion( ) {
        return driverMinorVersion;
    }

    public void setDriverMinorVersion( int driverMinorVersion ) {
        this.driverMinorVersion = driverMinorVersion;
    }

    public List< SchemaMetadata > getSchemas( ) {
        return schemas;
    }

    public void setSchemas( List< SchemaMetadata > schemas ) {
        this.schemas = schemas;
    }

    public List< CatalogMetadata > getCatalogs( ) {
        return catalogs;
    }

    public void setCatalogs( List< CatalogMetadata > catalogs ) {
        this.catalogs = catalogs;
    }
}