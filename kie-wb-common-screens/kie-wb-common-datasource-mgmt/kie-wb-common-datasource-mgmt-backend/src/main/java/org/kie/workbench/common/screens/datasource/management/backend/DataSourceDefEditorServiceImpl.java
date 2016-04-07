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
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceDefSerializer;
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

    @Inject
    @Named("ioStrategy")
    IOService ioService;

    @Inject
    private CommentedOptionFactory optionsFactory;

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

        checkNotNull( "path", path );
        checkNotNull( "content", editorContent );

        String content = DataSourceDefSerializer.serialize( editorContent.getDataSourceDef() );
        ioService.write( Paths.convert( path ), content, optionsFactory.makeCommentedOption( comment ) );
        return path;
    }

    @Override
    public Path create( final Path context,
            final String fileName ) {

        checkNotNull( "context", context );
        checkNotNull( "fileName", fileName );

        DataSourceDef dataSourceDef = new DataSourceDef();
        dataSourceDef.setUuid( UUID.randomUUID().toString() );
        dataSourceDef.setName( fileName );
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
    public String test( String jndi ) {
        //TODO experimental method
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
    public void delete( Path path, String comment ) {
        checkNotNull( "path", path );
        ioService.delete( Paths.convert( path ), optionsFactory.makeCommentedOption( comment ) );
    }
}
