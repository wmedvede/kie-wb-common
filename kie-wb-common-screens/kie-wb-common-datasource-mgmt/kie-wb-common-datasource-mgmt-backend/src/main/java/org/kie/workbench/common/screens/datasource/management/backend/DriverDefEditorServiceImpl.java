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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.events.NewDriverEvent;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileAlreadyExistsException;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Service
@ApplicationScoped
public class DriverDefEditorServiceImpl
        implements DriverDefEditorService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private DataSourceServicesHelper serviceHelper;

    @Inject
    private CommentedOptionFactory optionsFactory;

    @Inject
    private MavenArtifactResolver artifactResolver;

    @Inject
    private Event<NewDriverEvent> newDriverEvent;

    @Override
    public DriverDefEditorContent loadContent( final Path path ) {

        checkNotNull( "path", path );

        DriverDefEditorContent editorContent = new DriverDefEditorContent();
        String content = ioService.readAllString( Paths.convert( path ) );
        DriverDef driverDef = DriverDefSerializer.deserialize( content );
        editorContent.setDriverDef( driverDef );

        return editorContent;
    }

    @Override
    public Path save( final Path path, final DriverDefEditorContent editorContent, final String comment ) {

        checkNotNull( "path", path );
        checkNotNull( "content", editorContent );

        String content = DriverDefSerializer.serialize( editorContent.getDriverDef() );
        ioService.write( Paths.convert( path ), content, optionsFactory.makeCommentedOption( comment ) );
        return path;
    }

    @Override
    public Path create( final Path context, final String driverName, final String fileName ) {
        checkNotNull( "context", context );
        checkNotNull( "driverName", driverName );
        checkNotNull( "fileName", fileName );

        DriverDef driverDef = new DriverDef();
        driverDef.setUuid( UUID.randomUUID().toString() );
        driverDef.setName( driverName );
        String content = DriverDefSerializer.serialize( driverDef );

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
    public Path create( final DriverDef driverDef,
            final Project project,
            final boolean updateDeployment ) {
        checkNotNull( "driverDef", driverDef );
        checkNotNull( "project", project );

        Path context = serviceHelper.getProjectDataSourcesContext( project );
        Path newPath = create( driverDef, context, updateDeployment );

        newDriverEvent.fire( new NewDriverEvent( driverDef,
                project, optionsFactory.getSafeSessionId(), optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    @Override
    public Path createGlobal( final DriverDef driverDef, final boolean updateDeployment ) {
        checkNotNull( "driverDef", driverDef );

        Path context = serviceHelper.getGlobalDataSourcesContext();
        Path newPath = create( driverDef, context, updateDeployment );

        newDriverEvent.fire( new NewDriverEvent( driverDef,
                optionsFactory.getSafeSessionId(), optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    private Path create( final DriverDef driverDef,
            final Path context,
            final boolean deploy ) {

        try {
            validateDriver( driverDef );
        } catch ( Exception e ) {
            throw new RuntimeException( e.getMessage(), e );
        }

        if ( driverDef.getUuid() == null ) {
            driverDef.setUuid( UUID.randomUUID().toString() );
        }

        String fileName = driverDef.getName() + ".driver";
        String content = DriverDefSerializer.serialize( driverDef );

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( context ).resolve( fileName );
        final Path newPath = Paths.convert( nioPath );

        if ( ioService.exists( nioPath ) ) {
            throw new FileAlreadyExistsException( nioPath.toString() );
        }

        ioService.write( nioPath, content, new CommentedOption( optionsFactory.getSafeIdentityName() ) );

        return newPath;
    }

    private void validateDriver( DriverDef driverDef ) throws Exception {

        final URI uri = artifactResolver.resolve( new GAV( driverDef.getGroupId(),
                driverDef.getArtifactId(), driverDef.getVersion() ) );

        if ( uri == null ) {
            throw new Exception( "maven artifact was not found: " + driverDef.getGroupId() + ":"
                    + driverDef.getArtifactId() + ":" + driverDef.getVersion() );
        }

        final URL[] urls = {uri.toURL()};
        final URLClassLoader classLoader = new URLClassLoader( urls );

        try {
            Class driverClass = classLoader.loadClass( driverDef.getDriverClass() );

            if ( !Driver.class.isAssignableFrom( driverClass ) ) {
                throw new Exception( "class: " + driverDef.getDriverClass() + " do not extend from: " + Driver.class.getName() );
            }
        } catch ( ClassNotFoundException e ) {
            throw new Exception( "driver class: " + driverDef.getDriverClass() + " was not found in current gav" );
        }
    }

    @Override
    public void delete( final Path path, final String comment ) {
        checkNotNull( "path", path );
        ioService.delete( Paths.convert( path ), optionsFactory.makeCommentedOption( comment ) );
        final org.uberfire.java.nio.file.Path nioJarPath = Paths.convert( calculateJarPath( path ) );
        if ( ioService.exists( nioJarPath ) ) {
            ioService.delete( nioJarPath, optionsFactory.makeCommentedOption( comment ) );
        }
    }

    @Override
    public Path getGlobalDriversContext() {
        return serviceHelper.getGlobalDataSourcesContext();
    }

    @Override
    public Path getProjectDriversContext( Project project ) {
        return serviceHelper.getProjectDataSourcesContext( project );
    }

    private Path calculateJarPath( final Path currentFile ) {
        String jarFileName = currentFile.getFileName() + ".jar";
        org.uberfire.java.nio.file.Path nioJarPath = Paths.convert( currentFile ).resolveSibling( jarFileName );
        return Paths.convert( nioJarPath );
    }
}