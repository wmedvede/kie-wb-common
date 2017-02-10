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

package org.kie.workbench.common.services.backend.ala;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.ala.build.maven.config.MavenBuildConfig;
import org.guvnor.ala.build.maven.config.MavenBuildExecConfig;
import org.guvnor.ala.build.maven.config.MavenProjectConfig;
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
import org.kie.workbench.common.services.backend.ala.impl.LocalBuildConfigImpl;
import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;

import static org.guvnor.ala.pipeline.StageUtil.config;

@ApplicationScoped
@Startup( StartupType.BOOTSTRAP )
public class BuildPipelineInitializer {

    public static final String MAVEN_BUILD_PIPELINE = "maven-build-pipeline";

    public static final String LOCAL_BUILD_PIPELINE = "local-build-pipeline";

    private PipelineRegistry pipelineRegistry;

    private Instance< ConfigExecutor > configExecutors;

    private PipelineExecutor executor;

    public BuildPipelineInitializer( ) {
    }

    @Inject
    public BuildPipelineInitializer( final PipelineRegistry pipelineRegistry,
                                     final Instance< ConfigExecutor > configExecutors ) {
        this.pipelineRegistry = pipelineRegistry;
        this.configExecutors = configExecutors;
    }

    /**
     * @return A pipeline executor for executing the initialized pipelines.
     */
    public PipelineExecutor getExecutor( ) {
        return executor;
    }

    @PostConstruct
    private void init( ) {
        initMavenBuildPipeline( );
        initLocalBuildPipeline( );
        initExecutors( );
    }

    /**
     * Initializes a maven build based pipeline.
     */
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

    /**
     * Initializes a build pipeline based on local structures and optimizations required by the workbench.
     */
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
            return new LocalBuildConfigImpl( ) {
            };
        } );

        final Stage< BuildConfig, BinaryConfig > localBuildExecStage = config( "Local Build Exec", ( Function< BuildConfig, BinaryConfig > ) ( s ) -> {
            return new LocalBuildExecConfig( ) {
            };
        } );

        final Pipeline localBuildPipeline = PipelineFactory
                .startFrom( sourceConfigStage )
                .andThen( projectConfigStage )
                .andThen( localBuildConfigStage )
                .andThen( localBuildExecStage )
                .buildAs( LOCAL_BUILD_PIPELINE );
        pipelineRegistry.registerPipeline( localBuildPipeline );
    }

    private void initExecutors( ) {
        final Collection< ConfigExecutor > configs = new ArrayList<>( );
        configExecutors.iterator( ).forEachRemaining( configs::add );
        executor = new PipelineExecutor( configs );
    }
}