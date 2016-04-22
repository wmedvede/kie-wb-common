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

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefEditorContent;
import org.kie.workbench.common.screens.datasource.management.service.DriverDefEditorService;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
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
    private CommentedOptionFactory optionsFactory;

    @Override
    public DriverDefEditorContent loadContent( final Path path ) {

        checkNotNull( "path", path );

        DriverDefEditorContent editorContent = new DriverDefEditorContent();
        String content = ioService.readAllString( Paths.convert( path ) );
        DriverDef driverDef = DriverDefSerializer.deserialize( content );
        driverDef.setDriverLib( calculateJarPath( path ) );
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
    public void delete( final Path path, final String comment ) {
        checkNotNull( "path", path );
        ioService.delete( Paths.convert( path ), optionsFactory.makeCommentedOption( comment ) );
        final org.uberfire.java.nio.file.Path nioJarPath = Paths.convert( calculateJarPath( path ) );
        if ( ioService.exists( nioJarPath ) ) {
            ioService.delete( nioJarPath, optionsFactory.makeCommentedOption( comment ) );
        }
    }

    private Path calculateJarPath( final Path currentFile ) {
        String jarFileName = currentFile.getFileName() + ".jar";
        org.uberfire.java.nio.file.Path nioJarPath = Paths.convert( currentFile ).resolveSibling( jarFileName );
        return Paths.convert( nioJarPath );
    }
}