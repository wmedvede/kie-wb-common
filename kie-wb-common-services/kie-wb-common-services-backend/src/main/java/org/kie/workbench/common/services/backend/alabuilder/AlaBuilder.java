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

package org.kie.workbench.common.services.backend.alabuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
import org.guvnor.ala.build.maven.model.MavenBinary;
import org.guvnor.ala.build.maven.model.impl.MavenProjectBinaryBuildImpl;
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.pipeline.ConfigExecutor;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.IncrementalBuildResults;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.DeploymentMode;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.kie.workbench.common.services.backend.alabuilder.impl.LocalBuildExecConfigImpl;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.workbench.events.ResourceChange;

import static org.guvnor.ala.pipeline.StageUtil.*;

@ApplicationScoped
public class AlaBuilder {

    private static final String MAVEN_BUILD_PIPELINE = "maven-build-pipeline";

    private static final String LOCAL_BUILD_PIPELINE = "local-build-pipeline";

    private static final String LOCAL_FULL_BUILD_PIPELINE = "local-full-build-pipeline";

    private RepositoryService repositoryService;

    private ExtendedM2RepoService m2RepoService;

    private Instance< ConfigExecutor > configExecutors;

    private PipelineExecutor executor;

    private PipelineRegistry pipelineRegistry;

    public AlaBuilder( ) {
    }

    @Inject
    public AlaBuilder( RepositoryService repositoryService,
                       ExtendedM2RepoService m2RepoService,
                       PipelineRegistry pipelineRegistry,
                       final Instance< ConfigExecutor > configExecutors ) {
        this.repositoryService = repositoryService;
        this.m2RepoService = m2RepoService;
        this.pipelineRegistry = pipelineRegistry;
        this.configExecutors = configExecutors;
    }

    @PostConstruct
    private void init( ) {
        initMavenBuildPipeline( );
        initLocalBuildPipeline( );
        initLocalFullBuildPipeline( );
        initExecutor( );
    }

    public BuildResults build( Project project ) {
        return doBuild( project, false );
    }

    public BuildResults localBuild( Project project ) {
        final BuildResults[] result = new BuildResults[ 1 ];
        invokeLocalBuildPipeLine( project, localBinaryConfig -> {
            result[ 0 ] = localBinaryConfig.getBuildResults( );
        } );
        return result[ 0 ];
    }

    public IncrementalBuildResults localBuild( Project project, LocalBuildConfig.BuildType buildType, Path resource ) {
        final IncrementalBuildResults[] result = new IncrementalBuildResults[ 1 ];
        invokeLocalBuildPipeLine( project, buildType, resource, localBinaryConfig -> {
            result[ 0 ] = localBinaryConfig.getIncrementalBuildResults( );
        } );
        return result[ 0 ];
    }

    public IncrementalBuildResults localBuild( Project project, Map< Path, Collection< ResourceChange > > resourceChanges ) {
        final IncrementalBuildResults[] result = new IncrementalBuildResults[ 1 ];
        invokeLocalBuildPipeLine( project, resourceChanges, localBinaryConfig -> {
            result[ 0 ] = localBinaryConfig.getIncrementalBuildResults( );
        } );
        return result[ 0 ];
    }

    public BuildResults localBuildAndDeploy( final Project project,
                                             final DeploymentMode mode,
                                             final boolean suppressHandlers ) {
        final BuildResults[] result = new BuildResults[ 1 ];
        invokeLocalBuildPipeLine( project, suppressHandlers, mode, localBinaryConfig -> {
            result[ 0 ] = localBinaryConfig.getBuildResults( );
        } );
        return result[ 0 ];
    }

    public BuildResults localFullBuild( final Project project ) {
        final BuildResults[] result = new BuildResults[ 1 ];
        Pipeline pipe = pipelineRegistry.getPipelineByName( LOCAL_FULL_BUILD_PIPELINE );
        Input input = new Input( ) {
            {
                put( "root-path", project.getRootPath( ).toURI( ) );
            }
        };
        executor.execute( input, pipe, ( Consumer< LocalBinaryConfig > ) binary -> {
            result[ 0 ] = binary.getBuildResults( );
        } );
        return result[ 0 ];
    }

    private void invokeLocalBuildPipeLine( Project project,
                                           Consumer< LocalBinaryConfig > consumer ) {
        LocalBuildRequest buildRequest = new LocalBuildRequest( project );
        invokeLocalBuildPipeLine( buildRequest, consumer );
    }

    private void invokeLocalBuildPipeLine( Project project,
                                           LocalBuildConfig.BuildType buildType,
                                           Path resource,
                                           Consumer< LocalBinaryConfig > consumer ) {
        LocalBuildRequest buildRequest = new LocalBuildRequest( project, buildType, resource );
        invokeLocalBuildPipeLine( buildRequest, consumer );
    }

    private void invokeLocalBuildPipeLine( Project project,
                                           Map< Path, Collection< ResourceChange > > resourceChanges,
                                           Consumer< LocalBinaryConfig > consumer ) {
        LocalBuildRequest buildRequest = new LocalBuildRequest( project, resourceChanges );
        invokeLocalBuildPipeLine( buildRequest, consumer );
    }

    private void invokeLocalBuildPipeLine( Project project,
                                           boolean suppressHandlers,
                                           DeploymentMode mode,
                                           Consumer< LocalBinaryConfig > consumer ) {
        LocalBuildRequest buildRequest = new LocalBuildRequest( project, mode, suppressHandlers );
        invokeLocalBuildPipeLine( buildRequest, consumer );
    }

    private void invokeLocalBuildPipeLine( LocalBuildRequest buildRequest,
                                           Consumer< LocalBinaryConfig > consumer ) {
        Pipeline pipe = pipelineRegistry.getPipelineByName( LOCAL_BUILD_PIPELINE );

        Input input = new Input( ) {
            {
                put( "root-path", buildRequest.getProject().getRootPath().toURI() );
                put( "build-type", buildRequest.getBuildType().name() );
                if ( buildRequest.isSingleResource() ) {
                    put( "resource", buildRequest.getResource().toURI() );
                } else {
                    put( "resource-changes", "algo" );
                }
            }
        };
        executor.execute( input, pipe, ( Consumer< LocalBinaryConfig > ) binary -> {
            consumer.accept( binary );
        } );
    }

    private BuildResults doBuild( final Project project, boolean deploy ) {
        final BuildResults results = new BuildResults( project.getPom( ).getGav( ) );
        final Path rootPath = project.getRootPath( );
        final Path repoPath = PathFactory.newPath( "repo", rootPath.toURI( ).substring( 0, rootPath.toURI( ).indexOf( rootPath.getFileName( ) ) ) );
        final Repository repository = repositoryService.getRepository( repoPath );

        final Pipeline pipe = pipelineRegistry.getPipelineByName( MAVEN_BUILD_PIPELINE );

        final Input buildInput = new Input( ) {
            {
                put( "repo-name", repository.getAlias( ) );
                put( "branch", repository.getDefaultBranch( ) );
                put( "project-dir", project.getProjectName( ) );
            }
        };
        executor.execute( buildInput, pipe, ( Consumer< MavenBinary > ) mavenBinary -> {
            processBuildResult( mavenBinary, deploy, results );
        } );

        return results;
    }

    private void processBuildResult( MavenBinary mavenBinary, boolean deploy, BuildResults results ) {
        if ( mavenBinary instanceof MavenProjectBinaryBuildImpl ) {
            results.addAllBuildMessages( ( ( MavenProjectBinaryBuildImpl ) mavenBinary ).getBuildResults( ).getMessages( ) );
        }
        if ( deploy ) {
            GAV gav = new GAV( mavenBinary.getGroupId( ), mavenBinary.getArtifactId( ), mavenBinary.getArtifactId( ) );
            try (
                    final InputStream in = Files.newInputStream( mavenBinary.getPath( ).toFile( ).toPath( ) )
            ) {
                m2RepoService.deployJar( in, gav );
            } catch ( Exception e ) {
                e.printStackTrace( );
                //TODO treat this error
            }
        }
    }

    private void initMavenBuildPipeline( ) {
        final Stage< Input, SourceConfig > sourceConfig = config( "Git Source", ( Function< Input, SourceConfig > ) ( s ) -> {
            return new GitConfig( ) {
            };
        } );

        final Stage< SourceConfig, ProjectConfig > projectConfig = config( "Maven Project", ( Function< SourceConfig, ProjectConfig > ) ( s ) -> {
            return new MavenProjectConfig( ) {
            };
        } );

        final Stage< ProjectConfig, BuildConfig > buildConfig = config( "Maven Build Config", ( Function< ProjectConfig, BuildConfig > ) ( s ) -> {
            return new MavenBuildConfig( ) {
                @Override
                public List< String > getGoals( ) {
                    final List< String > result = new ArrayList<>( );
                    result.add( "clean" );
                    result.add( "package" );
                    return result;
                }

                @Override
                public Properties getProperties( ) {
                    final Properties result = new Properties( );
                    result.setProperty( "failIfNoTests", "false" );
                    return result;
                }
            };
        } );

        final Stage< BuildConfig, BinaryConfig > buildExecution = config( "Maven Build", ( Function< BuildConfig, BinaryConfig > ) ( s ) -> {
            return new MavenBuildExecConfig( ) {
            };
        } );

        final Pipeline alaBuilderPipeline = PipelineFactory
                .startFrom( sourceConfig )
                .andThen( projectConfig )
                .andThen( buildConfig )
                .andThen( buildExecution )
                .buildAs( MAVEN_BUILD_PIPELINE );

        pipelineRegistry.registerPipeline( alaBuilderPipeline );
    }

    private void initLocalBuildPipeline( ) {

        final Stage< Input, SourceConfig > sourceConfigStage = config( "Local Source Config", ( Function< Input, SourceConfig > ) ( s ) -> {
            return new LocalSourceConfig( ) {
            };
        } );

        final Stage< SourceConfig, ProjectConfig > projectConfigStage = config( "Local Project Config", ( Function< SourceConfig, ProjectConfig > ) ( s ) -> {
            return new LocalProjectConfig( ) {
            };
        } );

        final Stage< ProjectConfig, BuildConfig > localBuildConfigStage = config( "Local Build Config", ( Function< ProjectConfig, BuildConfig > ) ( s ) -> {
            return new LocalBuildConfig( ) {
            };
        } );

        final Stage< BuildConfig, BinaryConfig > localBuildExecStage = config( "Local Build Exec", ( Function< BuildConfig, BinaryConfig > ) ( s ) -> {
            return new LocalBuildExecConfig( ) {};
        } );

        final Pipeline localBuildPipeline = PipelineFactory
                .startFrom( sourceConfigStage )
                .andThen( projectConfigStage )
                .andThen( localBuildConfigStage )
                .andThen( localBuildExecStage )
                .buildAs( LOCAL_BUILD_PIPELINE );
        pipelineRegistry.registerPipeline( localBuildPipeline );

    }

    private void initLocalFullBuildPipeline( ) {
        /*
        final Stage< Input, LocalSourceConfig> sourceConfig = config( "Local Source Config", ( Function< Input, LocalSourceConfig> ) ( input ) -> {
            return new LocalSourceConfig() {};
        } );

        final Stage< LocalSourceConfig, LocalBuildExecConfig > buildConfig = config( "Local Full Build", ( Function< LocalSourceConfig, LocalBuildExecConfig > ) ( s ) -> {
            return new LocalBuildExecConfigImpl();
        } );

        final Pipeline localFullBuildPipeline = PipelineFactory.startFrom( sourceConfig ).
                andThen( buildConfig ).
                buildAs( LOCAL_FULL_BUILD_PIPELINE );
        pipelineRegistry.registerPipeline( localFullBuildPipeline );
        */
    }

    private void initExecutor( ) {
        final Collection< ConfigExecutor > configs = new ArrayList<>( );
        configExecutors.iterator( ).forEachRemaining( configs::add );
        executor = new PipelineExecutor( configs );
    }

    private class LocalBuildRequest {

        private String uuid = UUID.randomUUID( ).toString( );

        private Project project;

        private LocalBuildConfig.BuildType buildType = LocalBuildConfig.BuildType.FULL_BUILD;

        private Path resource;

        private Map< Path, Collection< ResourceChange > > resourceChanges = new HashMap<>( );

        private DeploymentMode deploymentMode = DeploymentMode.VALIDATED;

        private boolean suppressHandlers;

        public LocalBuildRequest( Project project ) {
            this.project = project;
            this.buildType = LocalBuildConfig.BuildType.FULL_BUILD;
        }

        public LocalBuildRequest( Project project, LocalBuildConfig.BuildType buildType, Path resource ) {
            this.project = project;
            this.buildType = buildType;
            this.resource = resource;
        }

        public LocalBuildRequest( Project project, Map< Path, Collection< ResourceChange > > resourceChanges ) {
            this.project = project;
            this.resourceChanges = resourceChanges;
            this.buildType = LocalBuildConfig.BuildType.BATCH_CHANGES;
        }

        public LocalBuildRequest( Project project, DeploymentMode deploymentMode, boolean suppressHandlers ) {
            this.project = project;
            this.deploymentMode = deploymentMode;
            this.suppressHandlers = suppressHandlers;
            this.buildType = LocalBuildConfig.BuildType.FULL_BUILD_AND_DEPLOY;
        }

        public String getUuid( ) {
            return uuid;
        }

        public LocalBuildConfig.BuildType getBuildType( ) {
            return buildType;
        }

        public Project getProject( ) {
            return project;
        }

        public Path getResource( ) {
            return resource;
        }

        public Map< Path, Collection< ResourceChange > > getResourceChanges( ) {
            return resourceChanges;
        }

        public DeploymentMode getDeploymentMode( ) {
            return deploymentMode;
        }

        public boolean isSuppressHandlers( ) {
            return suppressHandlers;
        }

        public boolean isSingleResource() {
            return resource != null;
        }
    }

}