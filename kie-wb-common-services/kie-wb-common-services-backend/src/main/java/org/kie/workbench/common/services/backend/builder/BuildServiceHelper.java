/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.services.backend.builder;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.builder.service.PostBuildHandler;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.MavenRepositoryMetadata;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.model.ProjectRepositories;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.common.services.project.service.GAVAlreadyExistsException;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectRepositoriesService;
import org.guvnor.common.services.project.service.ProjectRepositoryResolver;
import org.guvnor.common.services.shared.message.Level;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.ResourceChange;

@ApplicationScoped
public class BuildServiceHelper {

    private static final Logger logger = LoggerFactory.getLogger( BuildServiceHelper.class );

    private POMService pomService;

    private ExtendedM2RepoService m2RepoService;

    private LRUBuilderCache cache;

    private KieProjectService projectService;

    private ProjectRepositoryResolver repositoryResolver;

    private ProjectRepositoriesService projectRepositoriesService;

    private Instance<User> identity;

    private Instance<PostBuildHandler> handlers;

    public BuildServiceHelper( ) {
    }

    @Inject
    public BuildServiceHelper( final POMService pomService,
                               final ExtendedM2RepoService m2RepoService,
                               final KieProjectService projectService,
                               final ProjectRepositoryResolver repositoryResolver,
                               final ProjectRepositoriesService projectRepositoriesService,
                               final LRUBuilderCache cache,
                               final Instance<PostBuildHandler> handlers,
                               final Instance<User> identity ) {
        this.pomService = pomService;
        this.m2RepoService = m2RepoService;
        this.projectService = projectService;
        this.repositoryResolver = repositoryResolver;
        this.projectRepositoriesService = projectRepositoriesService;
        this.cache = cache;
        this.handlers = handlers;
        this.identity = identity;
    }

    public BuildResults build( final Project project ) {
        try {
            cache.invalidateCache( project );
            Builder builder = cache.assertBuilder( project );
            final BuildResults results = builder.build();

            BuildMessage infoMsg = new BuildMessage();

            infoMsg.setLevel( Level.INFO );
            infoMsg.setText( buildResultMessage( project, results ).toString() );

            results.addBuildMessage( 0, infoMsg );

            return results;

        } catch ( Exception e ) {
            logger.error( e.getMessage(),
                    e );
            return buildExceptionResults( e, project.getPom().getGav() );
        }
    }

    public IncrementalBuildResults addPackageResource( final Path resource ) {
        try {
            IncrementalBuildResults results = new IncrementalBuildResults();
            final KieProject project = projectService.resolveProject( resource );
            if ( project == null ) {
                return results;
            }
            final Builder builder = cache.assertBuilder( project );
            if ( !builder.isBuilt() ) {
                throw new IllegalStateException( "Incremental Build requires a full build be completed first." );
            } else {
                results = builder.addResource( Paths.convert( resource ) );
            }

            return results;

        } catch ( Exception e ) {
            logger.error( e.getMessage(),
                    e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    public IncrementalBuildResults deletePackageResource( final Path resource ) {
        try {
            IncrementalBuildResults results = new IncrementalBuildResults();
            final KieProject project = projectService.resolveProject( resource );
            if ( project == null ) {
                return results;
            }
            final Builder builder = cache.assertBuilder( project );
            if ( !builder.isBuilt() ) {
                throw new IllegalStateException( "Incremental Build requires a full build be completed first." );
            } else {
                results = builder.deleteResource( Paths.convert( resource ) );
            }

            return results;

        } catch ( Exception e ) {
            logger.error( e.getMessage(),
                    e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    public IncrementalBuildResults updatePackageResource( final Path resource ) {
        try {
            final Project project = projectService.resolveProject( resource );

            if ( project == null ) {
                return new IncrementalBuildResults();
            }

            final Builder builder = cache.assertBuilder( project );

            if ( !builder.isBuilt() ) {
                throw new IllegalStateException( "Incremental Build requires a full build be completed first." );
            }

            return  builder.updateResource( Paths.convert( resource ) );

        } catch ( Exception e ) {
            logger.error( e.getMessage(),
                    e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    public IncrementalBuildResults applyBatchResourceChanges( final Project project,
                                                              final Map<Path, Collection<ResourceChange>> changes ) {
        IncrementalBuildResults results = new IncrementalBuildResults();
        try {
            if ( project == null ) {
                return results;
            }
            final Builder builder = cache.assertBuilder( project );
            if ( !builder.isBuilt() ) {
                throw new IllegalStateException( "Incremental Build requires a full build be completed first." );
            } else {
                results = builder.applyBatchResourceChanges( changes );
            }

            return results;

        } catch ( Exception e ) {
            logger.error( e.getMessage(),
                    e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private StringBuffer buildResultMessage( final Project project,
                                             final BuildResults results ) {
        StringBuffer message = new StringBuffer();

        message.append( "Build of project '" );
        message.append( project.getProjectName() );
        message.append( "' (requested by " );
        message.append( getIdentifier() );
        message.append( ") completed.\n" );
        message.append( " Build: " );
        message.append( results.getErrorMessages().isEmpty() ? "SUCCESSFUL" : "FAILURE" );

        return message;
    }

    /**
     * When an exception is produced by the builder service, this method is uses to generate an instance of
     * <code>org.guvnor.common.services.project.builder.model.BuildResults</code> in generated with the exception details.
     * @param e
     *         The error exception.
     * @param gav
     * @return An instance of BuildResults with the exception details.
     */
    public BuildResults buildExceptionResults( Exception e,
                                                GAV gav ) {
        BuildResults exceptionResults = new BuildResults( gav );
        BuildMessage exceptionMessage = new BuildMessage();
        exceptionMessage.setLevel( Level.ERROR );
        exceptionMessage.setText( e.getMessage() );
        exceptionResults.addBuildMessage( exceptionMessage );

        return exceptionResults;
    }

    public BuildResults buildAndDeploy( final Project project ) {
        return buildAndDeploy( project,
                DeploymentMode.VALIDATED );
    }

    public BuildResults buildAndDeploy( final Project project,
                                        final DeploymentMode mode ) {
        if ( DeploymentMode.VALIDATED.equals( mode ) ) {
            checkRepositories( project );
        }
        return doBuildAndDeploy( project,
                false );
    }

    public BuildResults buildAndDeploy( final Project project,
                                        final boolean suppressHandlers ) {
        return buildAndDeploy( project,
                suppressHandlers,
                DeploymentMode.VALIDATED );
    }

    public BuildResults buildAndDeploy( final Project project,
                                        final boolean suppressHandlers,
                                        final DeploymentMode mode ) {
        if ( DeploymentMode.VALIDATED.equals( mode ) ) {
            checkRepositories( project );
        }
        return doBuildAndDeploy( project,
                suppressHandlers );
    }

    private BuildResults doBuildAndDeploy( final Project project,
                                           final boolean suppressHandlers ) {
        try {
            //Build
            final BuildResults results = build( project );
            StringBuffer message = new StringBuffer();
            message.append( "Build of project '" + project.getProjectName() + "' (requested by " + getIdentifier() + ") completed.\n" );
            message.append( " Build: " + ( results.getErrorMessages().isEmpty() ? "SUCCESSFUL" : "FAILURE" ) );

            //Deploy, if no errors
            final POM pom = pomService.load( project.getPomXMLPath() );
            if ( results.getErrorMessages().isEmpty() ) {
                final Builder builder = cache.assertBuilder( project );
                final InternalKieModule kieModule = (InternalKieModule) builder.getKieModule();
                final ByteArrayInputStream input = new ByteArrayInputStream( kieModule.getBytes() );
                m2RepoService.deployJar( input,
                        pom.getGav() );
                message.append( " Maven: SUCCESSFUL" );
                if ( !suppressHandlers ) {
                    for ( PostBuildHandler handler : handlers ) {
                        try {
                            handler.process( results );
                        } catch ( Exception e ) {
                            logger.warn( "PostBuildHandler {} failed due to {}", handler, e.getMessage() );
                        }
                    }
                    message.append( " Deploy: " + ( results.getErrorMessages().isEmpty() ? "SUCCESSFUL" : "FAILURE" ) );
                }
            }

            return results;

        } catch ( Exception e ) {
            logger.error( e.getMessage(), e );

            // BZ-1007894: If throwing the exception, an error popup will be displayed, but it's not the expected behavior. The excepted one is to show the errors in problems widget.
            // So, instead of throwing the exception, a BuildResults instance is produced on the fly to simulate the error in the problems widget.
            return buildExceptionResults( e, project.getPom().getGav() );
        }
    }


    private void checkRepositories( final Project project ) {
        // Check is the POM's GAV resolves to any pre-existing artifacts.
        final GAV gav = project.getPom().getGav();
        if ( gav.isSnapshot() ) {
            return;
        }
        final ProjectRepositories projectRepositories = projectRepositoriesService.load( ( (KieProject) project ).getRepositoriesPath() );
        final Set<MavenRepositoryMetadata> repositories = repositoryResolver.getRepositoriesResolvingArtifact( gav,
                projectRepositories.filterByIncluded() );
        if ( repositories.size() > 0 ) {
            throw new GAVAlreadyExistsException( gav,
                    repositories );
        }
    }

    private String getIdentifier() {
        if ( identity.isUnsatisfied() ) {
            return "system";
        }
        try {
            return identity.get().getIdentifier();
        } catch ( ContextNotActiveException e ) {
            return "system";
        }
    }
}