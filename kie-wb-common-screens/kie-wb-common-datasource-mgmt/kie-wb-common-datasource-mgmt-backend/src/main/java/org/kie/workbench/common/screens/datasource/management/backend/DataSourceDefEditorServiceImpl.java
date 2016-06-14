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
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.events.NewDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceDefSerializer;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileAlreadyExistsException;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Service
@ApplicationScoped
public class DataSourceDefEditorServiceImpl
        implements DataSourceDefEditorService {

    private static final Logger logger = LoggerFactory.getLogger( DataSourceDefEditorServiceImpl.class );

    @Inject
    @Named( "ioStrategy" )
    private IOService ioService;

    @Inject
    private CommentedOptionFactory optionsFactory;

    @Inject
    protected KieProjectService projectService;

    @Inject
    private DataSourceExplorerService dataSourceExplorerService;

    @Inject
    private DataSourceManagementService dataSourceManagementService;

    @Inject
    private DataSourceServicesHelper serviceHelper;

    @Inject
    private Event<NewDataSourceEvent> newDataSourceEvent;

    @Override
    public DataSourceDefEditorContent loadContent( final Path path ) {

        checkNotNull( "path", path );

        DataSourceDefEditorContent editorContent = new DataSourceDefEditorContent();
        String content = ioService.readAllString( Paths.convert( path ) );
        DataSourceDef dataSourceDef = DataSourceDefSerializer.deserialize( content );
        editorContent.setDataSourceDef( dataSourceDef );
        return editorContent;
    }

    @Override
    public Path save( final Path path,
            final DataSourceDefEditorContent editorContent,
            final String comment ) {
        return save( path, editorContent, comment, true );
    }

    public Path save( final Path path,
            final DataSourceDefEditorContent editorContent,
            final String comment,
            final boolean updateDeployment) {

        checkNotNull( "path", path );
        checkNotNull( "content", editorContent );

        String content = DataSourceDefSerializer.serialize( editorContent.getDataSourceDef() );

        try {
            if ( updateDeployment && dataSourceManagementService.isEnabled() ) {
                DataSourceDeploymentInfo deploymentInfo = dataSourceManagementService.getDeploymentInfo(
                        editorContent.getDataSourceDef().getUuid() );
                if ( deploymentInfo != null ) {
                    dataSourceManagementService.undeploy( deploymentInfo.getUuid() );
                }
                dataSourceManagementService.deploy( editorContent.getDataSourceDef() );
            }

            ioService.write( Paths.convert( path ), content, optionsFactory.makeCommentedOption( comment ) );
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
        return path;
    }

    @Override
    public Path create( final Path context,
            final String dataSourceName,
            final String fileName ) {

        checkNotNull( "context", context );
        checkNotNull( "dataSourceName", dataSourceName );
        checkNotNull( "fileName", fileName );

        DataSourceDef dataSourceDef = new DataSourceDef();
        dataSourceDef.setUuid( UUID.randomUUID().toString() );
        dataSourceDef.setName( dataSourceName );
        String content = DataSourceDefSerializer.serialize( dataSourceDef );

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( context ).resolve( fileName );
        final Path newPath = Paths.convert( nioPath );

        if ( ioService.exists( nioPath ) ) {
            throw new FileAlreadyExistsException( nioPath.toString() );
        }

        ioService.write( nioPath,
                content,
                new CommentedOption( optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    @Override
    public Path create( final DataSourceDef dataSourceDef,
            final Project project,
            final boolean updateDeployment ) {
        checkNotNull( "dataSourceDef", dataSourceDef );
        checkNotNull( "project", project );

        Path context = serviceHelper.getProjectDataSourcesContext( project );
        Path newPath = create( dataSourceDef, context, updateDeployment );

        newDataSourceEvent.fire( new NewDataSourceEvent( dataSourceDef,
                project, optionsFactory.getSafeSessionId(), optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    @Override
    public Path createGlobal( DataSourceDef dataSourceDef, boolean updateDeployment ) {
        checkNotNull( "dataSourceDef", dataSourceDef );

        Path context = serviceHelper.getGlobalDataSourcesContext();
        Path newPath = create( dataSourceDef, context, updateDeployment );

        newDataSourceEvent.fire( new NewDataSourceEvent( dataSourceDef, optionsFactory.getSafeSessionId(),
                optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    @Override
    public Path getGlobalDataSourcesContext() {
        return serviceHelper.getGlobalDataSourcesContext();
    }

    @Override
    public Path getProjectDataSourcesContext( final Project project ) {
        return serviceHelper.getProjectDataSourcesContext( project );
    }

    private Path create( final DataSourceDef dataSourceDef,
            final Path context,
            boolean updateDeployment ) {
        checkNotNull( "dataSourceDef", dataSourceDef );
        checkNotNull( "context", context );

        if ( dataSourceDef.getUuid() == null ) {
            dataSourceDef.setUuid( UUID.randomUUID().toString() );
        }

        String fileName = dataSourceDef.getName() + ".datasource";
        String content = DataSourceDefSerializer.serialize( dataSourceDef );

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( context ).resolve( fileName );
        final Path newPath = Paths.convert( nioPath );
        boolean fileCreated = false;

        if ( ioService.exists( nioPath ) ) {
            throw new FileAlreadyExistsException( nioPath.toString() );
        }

        try {
            ioService.startBatch( nioPath.getFileSystem() );

            //create the datasource file.
            ioService.write( nioPath,
                    content,
                    new CommentedOption( optionsFactory.getSafeIdentityName() ) );
            fileCreated = true;

            if ( updateDeployment && dataSourceManagementService.isEnabled() ) {
                //deploy the datasource
                dataSourceManagementService.deploy( dataSourceDef );
            }

        } catch ( Exception e1 ) {
            logger.error( "An exception was produced during data source creation: {}", dataSourceDef.getName(), e1 );
            if ( fileCreated ) {
                //the file was created, but the deployment failed.
                try {
                    ioService.delete( nioPath );
                } catch ( Exception e2 ) {
                    logger.warn( "Removal of orphan data source file failed: {}", newPath, e2 );
                }
            }
            throw ExceptionUtilities.handleException( e1 );
        } finally {
            ioService.endBatch();
        }
        return newPath;
    }

    @Override
    public String test( final String jndi ) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

            InitialContext context = new InitialContext();
            DataSource ds = ( DataSource ) context.lookup( jndi );
            if ( ds == null ) {
                stringBuilder.append( "Reference to datasource ds: " + jndi + " couldn't be obtained " );
                stringBuilder.append( "\n" );
                stringBuilder.append( "Test Failed" );
            } else {
                stringBuilder.append( "Reference to datasource ds: " + jndi + " was successfully obtained: " + ds );
                stringBuilder.append( "\n" );

                Connection conn = ds.getConnection();

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
            }

        } catch ( Exception e ) {
            stringBuilder.append( e.getMessage() );
            stringBuilder.append( "\n" );
            stringBuilder.append( "Test Failed" );
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    @Override
    public void delete( final Path path, final String comment ) {
        checkNotNull( "path", path );

        String content = ioService.readAllString( Paths.convert( path ) );
        DataSourceDef dataSourceDef = DataSourceDefSerializer.deserialize( content );

        try {
            if ( dataSourceDef.getUuid() != null && dataSourceManagementService.isEnabled() ) {
                dataSourceManagementService.undeploy( dataSourceDef.getUuid() );
            }
            ioService.delete( Paths.convert( path ), optionsFactory.makeCommentedOption( comment ) );
        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

}