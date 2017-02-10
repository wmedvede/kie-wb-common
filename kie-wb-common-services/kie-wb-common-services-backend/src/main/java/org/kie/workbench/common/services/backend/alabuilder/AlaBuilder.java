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
import java.util.List;
import java.util.Properties;
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
import org.guvnor.ala.config.BinaryConfig;
import org.guvnor.ala.config.BuildConfig;
import org.guvnor.ala.config.ProjectConfig;
import org.guvnor.ala.config.SourceConfig;
import org.guvnor.ala.pipeline.ConfigExecutor;
import org.guvnor.ala.pipeline.Input;
import org.guvnor.ala.pipeline.Pipeline;
import org.guvnor.ala.pipeline.PipelineFactory;
import org.guvnor.ala.pipeline.Stage;
import org.guvnor.ala.pipeline.events.AfterPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.AfterStageExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforePipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.BeforeStageExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorPipelineExecutionEvent;
import org.guvnor.ala.pipeline.events.OnErrorStageExecutionEvent;
import org.guvnor.ala.pipeline.events.PipelineEventListener;
import org.guvnor.ala.pipeline.execution.PipelineExecutor;
import org.guvnor.ala.registry.PipelineRegistry;
import org.guvnor.ala.source.git.config.GitConfig;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.shared.message.Level;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;

import static org.guvnor.ala.pipeline.StageUtil.*;

@ApplicationScoped
public class AlaBuilder {

    private static final String ALA_BUILDER_PIPELINE = "ALA-Builder-Pipeline";

    private RepositoryService repositoryService;

    private ExtendedM2RepoService m2RepoService;

    private PipelineRegistry pipelineRegistry;

    private Instance< ConfigExecutor > configExecutors;

    private PipelineExecutor executor;

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
        initPipeline( );
        initExecutor( );
    }

    public BuildResults build( Project project ) {
        return doBuild( project, false );
    }

    public BuildResults buildAndDeploy( Project project ) {
        return doBuild( project, true );
    }

    private BuildResults doBuild( final Project project, boolean deploy ) {
        final BuildResults results = new BuildResults( project.getPom( ).getGav( ) );
        final Path rootPath = project.getRootPath( );
        final Path repoPath = PathFactory.newPath( "repo", rootPath.toURI( ).substring( 0, rootPath.toURI( ).indexOf( rootPath.getFileName( ) ) ) );
        final Repository repository = repositoryService.getRepository( repoPath );

        final Pipeline pipe = pipelineRegistry.getPipelineByName( ALA_BUILDER_PIPELINE );

        final Input buildInput = new Input( ) {
            {
                put( "repo-name", repository.getAlias( ) );
                put( "branch", repository.getDefaultBranch( ) );
                put( "project-dir", project.getProjectName( ) );
            }
        };
        executor.execute( buildInput,
                pipe,
                ( Consumer< MavenBinary > ) mavenBinary -> processBuildResult( mavenBinary, deploy ),
                new PipelineEventListener( ) {
                    @Override
                    public void beforePipelineExecution( BeforePipelineExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.INFO, "Build pipeline started" ) );
                    }

                    @Override
                    public void afterPipelineExecution( AfterPipelineExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.INFO, "Build pipeline finished" ) );
                    }

                    @Override
                    public void beforeStageExecution( BeforeStageExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.INFO, "Stage started: " + event.getStage().getName() ) );
                    }

                    @Override
                    public void onStageError( OnErrorStageExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.ERROR, "Stage Error: " + event.getStage().getName() ) );
                    }

                    @Override
                    public void afterStageExecution( AfterStageExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.INFO, "Stage finished: " + event.getStage().getName() ) );
                    }

                    @Override
                    public void onPipelineError( OnErrorPipelineExecutionEvent event ) {
                        results.addBuildMessage( newBuildMessage( Level.ERROR, "Build pipe line error: " + event.getError().getMessage() ) );
                    }
                } );

        return results;
    }

    private void processBuildResult( MavenBinary mavenBinary, boolean deploy ) {
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

    private void initPipeline( ) {
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
                .buildAs( ALA_BUILDER_PIPELINE );

        pipelineRegistry.registerPipeline( alaBuilderPipeline );
    }

    private void initExecutor( ) {
        final Collection< ConfigExecutor > configs = new ArrayList<>( );
        configExecutors.iterator( ).forEachRemaining( configs::add );
        executor = new PipelineExecutor( configs );
    }

    private BuildMessage newBuildMessage( Level level, String text ) {
        BuildMessage buildMessage = new BuildMessage();
        buildMessage.setLevel( level );
        buildMessage.setText( text );
        return buildMessage;
    }
}