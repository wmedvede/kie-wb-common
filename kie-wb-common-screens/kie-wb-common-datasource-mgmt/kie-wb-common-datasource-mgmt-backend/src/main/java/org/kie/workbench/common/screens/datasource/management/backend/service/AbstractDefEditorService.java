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

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.model.Project;
import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceRuntimeManager;
import org.kie.workbench.common.screens.datasource.management.backend.core.DeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.backend.core.UnDeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.model.Def;
import org.kie.workbench.common.screens.datasource.management.model.DefEditorContent;
import org.kie.workbench.common.screens.datasource.management.model.DeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.util.MavenArtifactResolver;
import org.kie.workbench.common.screens.datasource.management.util.UUIDGenerator;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.editor.commons.service.RenameService;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.base.options.CommentedOption;
import org.uberfire.java.nio.file.FileAlreadyExistsException;

import static org.uberfire.commons.validation.PortablePreconditions.*;

public abstract class AbstractDefEditorService<C extends DefEditorContent<D>,D extends Def, I extends DeploymentInfo> {

    private static final Logger logger = LoggerFactory.getLogger( AbstractDefEditorService.class );

    protected DataSourceRuntimeManager runtimeManager;

    protected DataSourceServicesHelper serviceHelper;

    protected IOService ioService;

    protected KieProjectService projectService;

    protected CommentedOptionFactory optionsFactory;

    protected RenameService renameService;

    protected MavenArtifactResolver artifactResolver;

    public AbstractDefEditorService() {
    }

    public AbstractDefEditorService( DataSourceRuntimeManager runtimeManager,
            DataSourceServicesHelper serviceHelper,
            IOService ioService,
            KieProjectService projectService,
            CommentedOptionFactory optionsFactory,
            RenameService renameService,
            MavenArtifactResolver artifactResolver ) {
        this.runtimeManager = runtimeManager;
        this.serviceHelper = serviceHelper;
        this.ioService = ioService;
        this.projectService = projectService;
        this.optionsFactory = optionsFactory;
        this.renameService = renameService;
        this.artifactResolver = artifactResolver;
    }

    protected abstract C newContent();

    protected abstract String serializeDef( D def );

    protected abstract D deserializeDef( String source );

    protected abstract I readDeploymentInfo( String uuid ) throws Exception;

    protected abstract void deploy( D def, DeploymentOptions options ) throws Exception;

    protected abstract void unDeploy( I deploymentInfo, UnDeploymentOptions options ) throws Exception;

    protected abstract void fireCreateEvent( D def, Project project );

    protected abstract void fireCreateEvent( D def );

    protected abstract void fireUpdateEvent( D def, Project project, D originalDef );

    protected abstract void fireDeleteEvent( D def, Project project );

    protected abstract String buildFileName( D def );

    public C loadContent( final Path path ) {

        checkNotNull( "path", path );

        C editorContent = newContent();
        String content = ioService.readAllString( Paths.convert( path ) );
        D def = deserializeDef( content );
        editorContent.setDef( def );
        editorContent.setProject( projectService.resolveProject( path ) );
        return editorContent;
    }

    public Path save( final Path path,
            final C editorContent,
            final String comment ) {

        checkNotNull( "path", path );
        checkNotNull( "content", editorContent );

        Path newPath = path;
        try {
            final D originalDef = deserializeDef( ioService.readAllString( Paths.convert( path ) ) );
            final String content = serializeDef( editorContent.getDef() );

            I deploymentInfo = readDeploymentInfo( editorContent.getDef().getUuid() );
            if ( deploymentInfo != null ) {
                unDeploy( deploymentInfo, UnDeploymentOptions.forcedUnDeployment() );
            }
            deploy( editorContent.getDef(), DeploymentOptions.create() );

            ioService.write( Paths.convert( path ), content, optionsFactory.makeCommentedOption( comment ) );

            if ( originalDef.getName() != null &&
                    !originalDef.getName().equals( editorContent.getDef().getName() ) ) {
                newPath = renameService.rename( path, editorContent.getDef().getName(), comment );
            }

            fireUpdateEvent( editorContent.getDef(), editorContent.getProject(), originalDef );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
        return newPath;
    }

    public Path create( final D def, final Project project ) {
        checkNotNull( "def", def );
        checkNotNull( "project", project );

        Path context = serviceHelper.getProjectDataSourcesContext( project );
        Path newPath = create( def , context );

        fireCreateEvent( def, project );

        return newPath;
    }

    public Path createGlobal( final D def ) {
        checkNotNull( "def", def );

        Path context = serviceHelper.getGlobalDataSourcesContext();
        Path newPath = create( def, context );

        fireCreateEvent( def );

        return newPath;
    }

    protected Path create( final D def, final Path context ) {
        checkNotNull( "def", def );
        checkNotNull( "context", context );

        if ( def.getUuid() == null ) {
            def.setUuid( UUIDGenerator.generateUUID() );
        }

        String fileName = buildFileName( def );
        String content = serializeDef( def );

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( context ).resolve( fileName );
        final Path newPath = Paths.convert( nioPath );
        boolean fileCreated = false;

        if ( ioService.exists( nioPath ) ) {
            throw new FileAlreadyExistsException( nioPath.toString() );
        }

        try {
            ioService.startBatch( nioPath.getFileSystem() );

            //create the file.
            ioService.write( nioPath, content, new CommentedOption( optionsFactory.getSafeIdentityName() ) );
            fileCreated = true;

            deploy( def, DeploymentOptions.create() );

        } catch ( Exception e1 ) {
            logger.error( "It was not possible to create: {}", def.getName(), e1 );
            if ( fileCreated ) {
                //the file was created, but the deployment failed.
                try {
                    ioService.delete( nioPath );
                } catch ( Exception e2 ) {
                    logger.warn( "Removal of orphan definition file failed: {}", newPath, e2 );
                }
            }
            throw ExceptionUtilities.handleException( e1 );
        } finally {
            ioService.endBatch();
        }
        return newPath;
    }

    public void delete( final Path path, final String comment ) {
        checkNotNull( "path", path );

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( path );
        if ( ioService.exists( nioPath ) ) {
            String content = ioService.readAllString( Paths.convert( path ) );
            D def = deserializeDef( content );
            Project project = projectService.resolveProject( path );
            try {

                I deploymentInfo = readDeploymentInfo( def.getUuid() );
                if ( deploymentInfo != null ) {
                    unDeploy( deploymentInfo, UnDeploymentOptions.forcedUnDeployment() );
                }

                ioService.delete( Paths.convert( path ), optionsFactory.makeCommentedOption( comment ) );
                fireDeleteEvent( def, project );
            } catch ( Exception e ) {
                throw ExceptionUtilities.handleException( e );
            }
        }
    }
}