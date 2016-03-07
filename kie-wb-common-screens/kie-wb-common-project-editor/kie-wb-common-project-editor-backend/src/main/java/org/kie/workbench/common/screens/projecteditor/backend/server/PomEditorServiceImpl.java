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

package org.kie.workbench.common.screens.projecteditor.backend.server;

import java.io.IOException;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.backend.metadata.MetadataServerSideService;
import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.backend.server.utils.POMContentHandler;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.MavenRepositoryMetadata;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.ProjectRepositories;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.GAVAlreadyExistsException;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.screens.defaulteditor.service.DefaultEditorContent;
import org.kie.workbench.common.screens.defaulteditor.service.DefaultEditorService;
import org.kie.workbench.common.screens.projecteditor.service.PomEditorService;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

@Service
@ApplicationScoped
public class PomEditorServiceImpl implements PomEditorService {

    private static final String UNDETERMINED = "<undetermined>";
    private static final GAV GAV_UNDETERMINED = new GAV( UNDETERMINED,
                                                         UNDETERMINED,
                                                         UNDETERMINED );

    private static final Logger logger = LoggerFactory.getLogger( PomEditorServiceImpl.class );

    private IOService ioService;
    private DefaultEditorService defaultEditorService;
    private MetadataServerSideService metadataService;
    private CommentedOptionFactory commentedOptionFactory;

    private KieProjectService projectService;
    private POMContentHandler pomContentHandler;
    private ProjectRepositoryResolver repositoryResolver;
    private ProjectRepositoriesService projectRepositoriesService;

    public PomEditorServiceImpl() {
        //Zero-parameter constructor for WELD proxies
    }

    @Inject
    public PomEditorServiceImpl( final @Named("ioStrategy") IOService ioService,
                                 final DefaultEditorService defaultEditorService,
                                 final MetadataServerSideService metadataService,
                                 final CommentedOptionFactory commentedOptionFactory,
                                 final KieProjectService projectService,
                                 final POMContentHandler pomContentHandler,
                                 final ProjectRepositoryResolver repositoryResolver,
                                 final ProjectRepositoriesService projectRepositoriesService ) {
        this.ioService = ioService;
        this.defaultEditorService = defaultEditorService;
        this.metadataService = metadataService;
        this.commentedOptionFactory = commentedOptionFactory;

        this.projectService = projectService;
        this.pomContentHandler = pomContentHandler;
        this.repositoryResolver = repositoryResolver;
        this.projectRepositoriesService = projectRepositoriesService;
    }

    @Override
    public DefaultEditorContent loadContent( final Path path ) {
        return defaultEditorService.loadContent( path );
    }

    @Override
    public Path save( final Path pomPath,
                      final String pomXml,
                      final Metadata metadata,
                      final String comment,
                      final DeploymentMode mode ) {
        if ( DeploymentMode.VALIDATED.equals( mode ) ) {
            checkRepositories( pomPath,
                               pomXml );
        }

        try {
            final org.uberfire.java.nio.file.Path nioPomPath = Paths.convert( pomPath );
            ioService.startBatch( nioPomPath.getFileSystem() );
            ioService.write( nioPomPath,
                             pomXml,
                             metadataService.setUpAttributes( pomPath,
                                                              metadata ),
                             commentedOptionFactory.makeCommentedOption( comment ) );

            return pomPath;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        } finally {
            ioService.endBatch();
        }
    }

    private void checkRepositories( final Path pomPath,
                                    final String pomXml ) {
        // Check is the POM's GAV has been changed.
        final KieProject project = projectService.resolveProject( pomPath );
        POM pom = new POM( GAV_UNDETERMINED );
        try {
            pom = pomContentHandler.toModel( pomXml );
            if ( pom.getGav().equals( project.getPom().getGav() ) ) {
                return;
            }

        } catch ( IOException ioe ) {
            logger.warn( "Unable to load pom.xml. It is therefore impossible to ascertain GAV.",
                         ioe );

        } catch ( XmlPullParserException pe ) {
            logger.warn( "Unable to load pom.xml. It is therefore impossible to ascertain GAV.",
                         pe );
        }

        // Check is the POM's GAV resolves to any pre-existing artifacts.
        // Filter resolved Repositories by those enabled for the Project.
        final ProjectRepositories projectRepositories = projectRepositoriesService.load( project.getRepositoriesPath() );
        final Set<MavenRepositoryMetadata> repositories = repositoryResolver.getRepositoriesResolvingArtifact( pomXml,
                                                                                                               projectRepositories.filterByIncluded() );
        if ( repositories.size() > 0 ) {
            throw new GAVAlreadyExistsException( pom.getGav(),
                                                 repositories );
        }
    }

}
