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

package org.kie.workbench.common.screens.datasource.management.backend.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Service
@ApplicationScoped
public class DataSourceDefQueryServiceImpl
        implements DataSourceDefQueryService {

    private static final Logger logger = LoggerFactory.getLogger( DataSourceDefQueryServiceImpl.class );

    private static String DS_FILE_TYPE = ".datasource";

    private static String DRIVER_FILE_TYPE = ".driver";

    @Inject
    @Named( "ioStrategy" )
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private DataSourceServicesHelper serviceHelper;

    public DataSourceDefQueryServiceImpl() {
    }

    @Override
    public Collection<DataSourceDefInfo> findGlobalDataSources() {
        return resolveDataSources( serviceHelper.getGlobalDataSourcesContext() );
    }

    @Override
    public Collection<DataSourceDefInfo> findProjectDataSources( final Path path ) {
        checkNotNull( "path", path );
        Project project = projectService.resolveProject( path );
        if ( project == null ) {
            return new ArrayList<>( );
        } else {
            return resolveDataSources( serviceHelper.getProjectDataSourcesContext( project ) );
        }
    }

    @Override
    public Collection<DataSourceDefInfo> findProjectDataSources( final Project project ) {
        if ( project != null ) {
            return resolveDataSources( serviceHelper.getProjectDataSourcesContext( project ) );
        } else {
            return new ArrayList<>( );
        }
    }

    @Override
    public Collection<DriverDefInfo> findGlobalDrivers() {
        return resolveDrivers( serviceHelper.getGlobalDataSourcesContext() );
    }

    @Override
    public Collection<DriverDefInfo> findProjectDrivers( final Path path ) {
        checkNotNull( "path", path );
        Project project = projectService.resolveProject( path );
        if ( project == null ) {
            return new ArrayList<>( );
        } else {
            return resolveDrivers( serviceHelper.getProjectDataSourcesContext( project ) );
        }
    }

    @Override
    public DriverDefInfo findProjectDriver( final String uuid, final Path path ) {
        checkNotNull( "uuid", uuid );
        checkNotNull( "path", path );

        for ( DriverDefInfo driverDefInfo : findProjectDrivers( path ) ) {
            if ( uuid.equals( driverDefInfo.getUuid() ) ) {
                return driverDefInfo;
            }
        }
        return null;
    }

    @Override
    public Collection<DriverDefInfo> findProjectDrivers( final Project project ) {
        if ( project != null ) {
            return resolveDrivers( serviceHelper.getProjectDataSourcesContext( project ) );
        } else {
            return new ArrayList<>( );
        }
    }

    @Override
    public DriverDefInfo findGlobalDriver( String uuid ) {
        checkNotNull( "uuid", uuid );

        for ( DriverDefInfo driverDefInfo : findGlobalDrivers() ) {
            if ( uuid.equals( driverDefInfo.getUuid() ) ) {
                return driverDefInfo;
            }
        }
        return null;
    }

    private Collection<DriverDefInfo> resolveDrivers( final Path path ) {

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( path );
        final List<DriverDefInfo> result = new ArrayList<>( );

        try {
            final DirectoryStream<org.uberfire.java.nio.file.Path> stream = ioService.newDirectoryStream( nioPath,
                    entry -> Files.isRegularFile( entry ) &&
                            !entry.getFileName().toString().startsWith( "." ) &&
                            entry.getFileName().toString().endsWith( DRIVER_FILE_TYPE ) );

            stream.forEach( file -> {
                result.add( createDriverInfo( file ) );
            } );
            stream.close();

            return result;
        } catch ( Exception e ) {
            logger.error( "It was not possible read drivers info from: " + path, e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private Collection<DataSourceDefInfo> resolveDataSources( final Path path ) {

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( path );
        final List<DataSourceDefInfo> result = new ArrayList<>( );

        try {
            final DirectoryStream<org.uberfire.java.nio.file.Path> stream = ioService.newDirectoryStream( nioPath,
                    entry -> Files.isRegularFile( entry ) &&
                            !entry.getFileName().toString().startsWith( "." ) &&
                            entry.getFileName().toString().endsWith( DS_FILE_TYPE ) );

            stream.forEach( file -> {
                result.add( createDataSourceInfo( file ) );
            } );
            stream.close();

            return result;
        } catch ( Exception e ) {
            logger.error( "It was not possible read data sources info from: " + path, e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private DataSourceDefInfo createDataSourceInfo( final org.uberfire.java.nio.file.Path path ) {
        String name = path.getName( path.getNameCount() -1 ).toString();
        name = name.substring( 0, name.lastIndexOf( DS_FILE_TYPE ) );
        return new DataSourceDefInfo( name, Paths.convert( path ) );
    }

    private DriverDefInfo createDriverInfo( final org.uberfire.java.nio.file.Path path ) {
        String content = ioService.readAllString( path );
        DriverDef driverDef = DriverDefSerializer.deserialize( content );
        return new DriverDefInfo( driverDef.getUuid(), driverDef.getName(), Paths.convert( path ) );
    }
}