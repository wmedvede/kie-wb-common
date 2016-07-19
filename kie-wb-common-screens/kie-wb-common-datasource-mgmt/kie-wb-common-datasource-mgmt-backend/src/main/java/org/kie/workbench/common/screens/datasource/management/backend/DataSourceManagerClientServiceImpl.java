/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management.backend;

import java.sql.Connection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSource;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceManager;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverRuntimeInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagerClientService;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;

@Service
@ApplicationScoped
public class DataSourceManagerClientServiceImpl
        implements DataSourceManagerClientService {

    @Inject
    private DataSourceManager dataSourceManager;

    public DataSourceManagerClientServiceImpl() {
    }

    @Override
    public DataSourceRuntimeInfo getDataSourceRuntimeInfo( String uuid ) {
        return dataSourceManager.getDataSourceRuntimeInfo( uuid );
    }

    @Override
    public DriverRuntimeInfo getDriverRuntimeInfo( String uuid ) {
        return dataSourceManager.getDriverRuntimeInfo( uuid );
    }

    @Override
    public String test( final String uuid ) {
        StringBuilder stringBuilder = new StringBuilder(  );
        try {
            DataSource dataSource = dataSourceManager.lookup( uuid );
            return test( dataSource );
        } catch ( Exception e ) {
            stringBuilder.append( "Reference to datasource ds: " + uuid + " couldn't be obtained " );
            stringBuilder.append( "\n" );
            stringBuilder.append( "Test Failed" );
        }
        return stringBuilder.toString();
    }

    private String test( final DataSource dataSource ) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

            checkNotNull( "dataSource", dataSource );

            stringBuilder.append( "Reference to datasource was successfully obtained: " + dataSource );
            stringBuilder.append( "\n" );

            Connection conn = dataSource.getConnection();

            if ( conn == null ) {
                stringBuilder.append( "It was not possible to get connection from the datasoure." );
                stringBuilder.append( "\n" );
                stringBuilder.append( "Test Failed" );
            } else {
                stringBuilder.append( "Connection was successfully obtained: " + conn );
                stringBuilder.append( "\n" );
                stringBuilder.append( "*** DatabaseProductName: " + conn.getMetaData().getDatabaseProductName() );
                stringBuilder.append( "\n" );
                stringBuilder.append( "*** DatabaseProductVersion: " + conn.getMetaData().getDatabaseProductVersion() );
                stringBuilder.append( "\n" );
                stringBuilder.append( "*** DriverName: " + conn.getMetaData().getDriverName() );
                stringBuilder.append( "\n" );
                stringBuilder.append( "*** DriverVersion: " + conn.getMetaData().getDriverVersion() );
                stringBuilder.append( "\n" );
                conn.close();
                stringBuilder.append( "Connection was successfully released." );
                stringBuilder.append( "\n" );
                stringBuilder.append( "Test Successful" );
            }

        } catch ( Exception e ) {
            stringBuilder.append( e.getMessage() );
            stringBuilder.append( "\n" );
            stringBuilder.append( "Test Failed" );
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}