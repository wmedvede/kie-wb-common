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

package org.kie.workbench.common.screens.datasource.management.backend.service.impl;

import org.kie.workbench.common.screens.datasource.management.backend.core.DataSourceRuntimeManager;
import org.kie.workbench.common.screens.datasource.management.backend.service.DefChangeHandler;
import org.kie.workbench.common.screens.datasource.management.backend.core.DeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.backend.core.UnDeploymentOptions;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceEventHelper;
import org.kie.workbench.common.screens.datasource.management.backend.service.DataSourceServicesHelper;
import org.kie.workbench.common.screens.datasource.management.backend.service.DefResourceChangeObserver;
import org.kie.workbench.common.screens.datasource.management.events.DeleteDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.events.DeleteDriverEvent;
import org.kie.workbench.common.screens.datasource.management.events.NewDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.events.NewDriverEvent;
import org.kie.workbench.common.screens.datasource.management.events.UpdateDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.events.UpdateDriverEvent;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.model.Def;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDeploymentInfo;
import org.kie.workbench.common.screens.datasource.management.util.DataSourceDefSerializer;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.workbench.events.ResourceChangeType;

public abstract class DefaultDefChangeHandler implements DefChangeHandler {

    private static final Logger logger = LoggerFactory.getLogger( DefResourceChangeObserver.class );

    protected DataSourceRuntimeManager runtimeManager;

    protected DataSourceServicesHelper serviceHelper;

    protected IOService ioService;

    protected KieProjectService projectService;

    protected DataSourceEventHelper eventHelper;

    public DefaultDefChangeHandler( DataSourceRuntimeManager runtimeManager,
                                    DataSourceServicesHelper serviceHelper,
                                    IOService ioService,
                                    KieProjectService projectService,
                                    DataSourceEventHelper eventHelper ) {
        this.runtimeManager = runtimeManager;
        this.serviceHelper = serviceHelper;
        this.ioService = ioService;
        this.projectService = projectService;
        this.eventHelper = eventHelper;
    }

    public void processResourceAdd( Path path, SessionInfo sessionInfo ) {
        Def registeredDef;
        Def def = readDef( path );
        if ( def != null ) {
            registeredDef = serviceHelper.getDefRegistry( ).getEntry( path );
            if ( registeredDef == null || !registeredDef.equals( def ) ) {
                updateDeployment( path, registeredDef, def, sessionInfo, ResourceChangeType.ADD );
            }
        }
    }

    public void processResourceUpdate( Path path, SessionInfo sessionInfo ) {
        Def registeredDef;
        Def def = readDef( path );
        if ( def != null ) {
            registeredDef = serviceHelper.getDefRegistry().getEntry( path );
            if ( registeredDef == null || !registeredDef.equals( def ) ) {
                updateDeployment( path, registeredDef, def, sessionInfo, ResourceChangeType.UPDATE );
            }
        }
    }

    public void processResourceRename( Path originalPath, Path targetPath, SessionInfo sessionInfo ) {
        Def originalDef;
        Def registeredDef;
        Def def = readDef( targetPath );
        if ( def != null ) {
            if ( ( originalDef = serviceHelper.getDefRegistry( ).getEntry( originalPath ) ) != null ) {
                try {
                    serviceHelper.getDefRegistry( ).invalidateCache( originalPath );
                    unDeploy( originalDef );
                } catch ( Exception e ) {
                    logger.error( "Un-deployment failure for file: " + originalPath, e );
                }
            }
            registeredDef = serviceHelper.getDefRegistry( ).getEntry( targetPath );
            if ( registeredDef == null || !registeredDef.equals( def ) ) {
                updateDeployment( targetPath, registeredDef, def, sessionInfo, ResourceChangeType.RENAME );
            }
        }
    }

    public void processResourceDelete( Path path, SessionInfo sessionInfo ) {
        Def registeredDef;
        if ( ( registeredDef = serviceHelper.getDefRegistry().getEntry( path ) ) != null ) {
            try {
                unDeploy( registeredDef );
            } catch ( Exception e ) {
                logger.error( "Un-deployment failure for file: " + path, e );
            } finally {
                serviceHelper.getDefRegistry().invalidateCache( path );
                fireEvent( registeredDef, null, path, sessionInfo, ResourceChangeType.DELETE );
            }
        }
    }

    private void updateDeployment( Path path, Def registeredDef,
                                   Def def,
                                   SessionInfo sessionInfo,
                                   ResourceChangeType changeType ) {
        try {
            if ( registeredDef != null ) {
                unDeploy( registeredDef );
            }
            serviceHelper.getDefRegistry( ).invalidateCache( path );
            deploy( def );
            serviceHelper.getDefRegistry().setEntry( path, def );
            fireEvent( def, registeredDef, path, sessionInfo, changeType );
        } catch ( Exception e ) {
            logger.error( "Deployment update failed for file: " +
                    path, ", registeredDef: " + registeredDef, " def: " + def, e );
        }
    }

    private void unDeploy( Def def ) throws Exception {
        if ( def instanceof DataSourceDef ) {
            DataSourceDeploymentInfo deploymentInfo = runtimeManager.getDataSourceDeploymentInfo( def.getUuid( ) );
            if ( deploymentInfo != null ) {
                runtimeManager.unDeployDataSource( deploymentInfo, UnDeploymentOptions.forcedUnDeployment( ) );
            }
        } else {
            DriverDeploymentInfo deploymentInfo = runtimeManager.getDriverDeploymentInfo( def.getUuid( ) );
            if ( deploymentInfo != null ) {
                runtimeManager.unDeployDriver( deploymentInfo, UnDeploymentOptions.forcedUnDeployment( ) );
            }
        }
    }

    private void deploy( Def def ) throws Exception {
        if ( def instanceof DataSourceDef ) {
            runtimeManager.deployDataSource( (DataSourceDef) def, DeploymentOptions.create() );
        } else {
            runtimeManager.deployDriver( (DriverDef ) def, DeploymentOptions.create() );
        }
    }

    private void fireEvent( Def def, Def originalDef,
                            Path path,
                            SessionInfo sessionInfo,
                            ResourceChangeType changeType ) {
        if ( def instanceof DriverDef ) {
            switch ( changeType ) {
                case ADD:
                    eventHelper.fireCreateEvent(
                            new NewDriverEvent( (DriverDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
                    break;
                case UPDATE:
                case RENAME:
                    eventHelper.fireUpdateEvent(
                            new UpdateDriverEvent( (DriverDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
                    break;
                case DELETE:
                    eventHelper.fireDeleteEvent(
                            new DeleteDriverEvent( (DriverDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
            }
        } else {
            switch ( changeType ) {
                case ADD:
                    eventHelper.fireCreateEvent(
                            new NewDataSourceEvent( (DataSourceDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
                    break;
                case UPDATE:
                case RENAME:
                    eventHelper.fireUpdateEvent(
                            new UpdateDataSourceEvent( (DataSourceDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
                    break;
                case DELETE:
                    eventHelper.fireDeleteEvent(
                            new DeleteDataSourceEvent( (DataSourceDef) def,
                                    projectService.resolveProject( path ),
                                    getSessionId( sessionInfo ),
                                    getIdentifier( sessionInfo ) ) );
            }
        }
    }

    private Def readDef( Path path ) {
        Def def = null;
        try {
            if ( isDataSourceFile( path ) ) {
                def = DataSourceDefSerializer.deserialize( ioService.readAllString( Paths.convert( path ) ) );
            } else {
                def = DriverDefSerializer.deserialize( ioService.readAllString( Paths.convert( path ) ) );
            }
        } catch ( Exception e ) {
            logger.error( "It was not possible to deserialize content from path: {}", path );
        }
        return def;
    }

    private boolean isDriverFile( Path path ) {
        return path.getFileName( ).endsWith( ".driver" );
    }

    private boolean isDataSourceFile( Path path ) {
        return path.getFileName( ).endsWith( ".datasource" );
    }

    private String getSessionId( SessionInfo sessionInfo ) {
        return sessionInfo != null ? sessionInfo.getId( ) : null;
    }

    private String getIdentifier( SessionInfo sessionInfo ) {
        return sessionInfo != null && sessionInfo.getIdentity( ) != null ? sessionInfo.getIdentity( ).getIdentifier( ) : null;
    }
}