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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.User;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.model.DriverDef;
import org.kie.workbench.common.screens.datasource.management.model.DriverDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQuery;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQueryResult;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerService;
import org.kie.workbench.common.screens.datasource.management.util.DriverDefSerializer;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;
import org.uberfire.security.authz.AuthorizationManager;

import static org.uberfire.commons.validation.PortablePreconditions.*;

@Service
@ApplicationScoped
public class DataSourceExplorerServiceImpl
        implements DataSourceExplorerService {

    private static final Logger logger = LoggerFactory.getLogger( DataSourceExplorerServiceImpl.class );

    private static String DS_FILE_TYPE = ".datasource";

    private static String DRIVER_FILE_TYPE = ".driver";

    @Inject
    @Named( "ioStrategy" )
    private IOService ioService;

    @Inject
    private KieProjectService projectService;

    @Inject
    private OrganizationalUnitService organizationalUnitService;

    @Inject
    private DataSourceServicesHelper serviceHelper;

    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    private User identity;

    public DataSourceExplorerServiceImpl() {
    }

    @Override
    public Collection<DataSourceDefInfo> findGlobalDataSources() {
        return getDataSources( serviceHelper.getGlobalDataSourcesContext() );
    }

    @Override
    public Collection<DataSourceDefInfo> findProjectDataSources( final Path path ) {
        checkNotNull( "path", path );
        Project project = projectService.resolveProject( path );
        if ( project == null ) {
            return new ArrayList<>( );
        } else {
            return getDataSources( serviceHelper.getProjectDataSourcesContext( project ) );
        }
    }

    @Override
    public Collection<DriverDefInfo> findGlobalDrivers() {
        return getDrivers( serviceHelper.getGlobalDataSourcesContext() );
    }

    @Override
    public Collection<DriverDefInfo> findProjectDrivers( final Path path ) {
        checkNotNull( "path", path );
        Project project = projectService.resolveProject( path );
        if ( project == null ) {
            return new ArrayList<>( );
        } else {
            return getDrivers( serviceHelper.getProjectDataSourcesContext( project ) );
        }
    }

    @Override
    public DriverDefInfo findProjectDriver( final String uuid, final Path path ) {
        checkNotNull( "uuid", uuid );
        checkNotNull( "path", path );

        for ( DriverDefInfo driverDefInfo : findProjectDrivers( path ) ) {
            if ( uuid.equals( driverDefInfo.getUuid() ) ) {
                return driverDefInfo;
            }
        }
        return null;
    }

    @Override
    public DriverDefInfo findGlobalDriver( String uuid ) {
        checkNotNull( "uuid", uuid );

        for ( DriverDefInfo driverDefInfo : findGlobalDrivers() ) {
            if ( uuid.equals( driverDefInfo.getUuid() ) ) {
                return driverDefInfo;
            }
        }
        return null;
    }

    @Override
    public DataSourceExplorerContentQueryResult executeQuery( final DataSourceExplorerContentQuery query ) {
        checkNotNull( "query", query );
        if ( query.isGlobalQuery() ) {
            DataSourceExplorerContentQueryResult result = new DataSourceExplorerContentQueryResult();
            result.setDataSourceDefs( getDataSources( serviceHelper.getGlobalDataSourcesContext() ) );
            result.setDriverDefs( getDrivers( serviceHelper.getGlobalDataSourcesContext() ) );
            return result;
        } else {
            return resolveQuery( query );
        }
    }

    private DataSourceExplorerContentQueryResult resolveQuery( final DataSourceExplorerContentQuery query ) {

        DataSourceExplorerContentQueryResult result = new DataSourceExplorerContentQueryResult();

        //load the organizational units.
        Collection<OrganizationalUnit> organizationalUnits = getOrganizationalUnits();
        result.getOrganizationalUnits().addAll( organizationalUnits );
        if ( query.getOrganizationalUnit() == null ||
                !containsOU( organizationalUnits, query.getOrganizationalUnit() ) ) {
            //if no OU was set for filtering or the selected OU has been removed or has changed in backend.
            return result;
        }

        //set the repositories for current OU.
        Map<String, Repository> repositories = getRepositories( query.getOrganizationalUnit() );
        result.getRepositories().addAll( repositories.values() );
        if ( query.getRepository() == null ||
                !repositories.containsKey( query.getRepository().getAlias() ) ) {
            //if no Repository was set for filtering or the selected Repository has been removed or has
            // changed in backend.
            return result;
        }

        //load the projects for current OU/Repository and the selected branch.
        Map<String, Project> projects = getProjects( query.getRepository(), query.getBranch() );
        result.getProjects().addAll( projects.values() );
        if ( query.getProject() == null || !projects.containsKey( query.getProject().getProjectName() ) ) {
            //if no Project was set for filtering or the selected Project has been removed or has
            // changed in backend.
            return result;
        }

        //get the datasources and drivers for the selected project.
        result.setDataSourceDefs( getDataSources( query.getProject() ) );
        result.setDriverDefs( getDrivers( query.getProject() ) );
        return result;
    }

    private boolean containsOU( final Collection<OrganizationalUnit> organizationalUnits, final OrganizationalUnit ou ) {
        for ( OrganizationalUnit unit : organizationalUnits ) {
            if ( unit.getName().equals( ou.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    private Collection<DataSourceDefInfo> getDataSources( final Project project ) {
        if ( project != null ) {
            return getDataSources( serviceHelper.getProjectDataSourcesContext( project ) );
        } else {
            return new ArrayList<>( );
        }
    }

    private Collection<DriverDefInfo> getDrivers( final Project project ) {
        if ( project != null ) {
            return getDrivers( serviceHelper.getProjectDataSourcesContext( project ) );
        } else {
            return new ArrayList<>( );
        }
    }

    private Collection<DriverDefInfo> getDrivers( final Path path ) {

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( path );
        final List<DriverDefInfo> result = new ArrayList<>( );

        try {
            final DirectoryStream<org.uberfire.java.nio.file.Path> stream = ioService.newDirectoryStream( nioPath,
                    entry -> Files.isRegularFile( entry ) &&
                            !entry.getFileName().toString().startsWith( "." ) &&
                            entry.getFileName().toString().endsWith( DRIVER_FILE_TYPE ) );

            stream.forEach( file -> {
                result.add( createDriverInfo( file ) );
            } );
            stream.close();

            return result;
        } catch ( Exception e ) {
            logger.error( "It was not possible read drivers info from: " + path, e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private Collection<DataSourceDefInfo> getDataSources( final Path path ) {

        final org.uberfire.java.nio.file.Path nioPath = Paths.convert( path );
        final List<DataSourceDefInfo> result = new ArrayList<>( );

        try {
            final DirectoryStream<org.uberfire.java.nio.file.Path> stream = ioService.newDirectoryStream( nioPath,
                    entry -> Files.isRegularFile( entry ) &&
                            !entry.getFileName().toString().startsWith( "." ) &&
                            entry.getFileName().toString().endsWith( DS_FILE_TYPE ) );

            stream.forEach( file -> {
                result.add( createInfo( file ) );
            } );
            stream.close();

            return result;
        } catch ( Exception e ) {
            logger.error( "It was not possible read data sources info from: " + path, e );
            throw ExceptionUtilities.handleException( e );
        }
    }

    private DataSourceDefInfo createInfo( final org.uberfire.java.nio.file.Path path ) {
        String name = path.getName( path.getNameCount() -1 ).toString();
        name = name.substring( 0, name.lastIndexOf( DS_FILE_TYPE ) );
        return new DataSourceDefInfo( name, Paths.convert( path ) );
    }

    private DriverDefInfo createDriverInfo( final org.uberfire.java.nio.file.Path path ) {
        String content = ioService.readAllString( path );
        DriverDef driverDef = DriverDefSerializer.deserialize( content );
        return new DriverDefInfo( driverDef.getUuid(), driverDef.getName(), Paths.convert( path ) );
    }

    private Set<OrganizationalUnit> getOrganizationalUnits() {
        final Collection<OrganizationalUnit> organizationalUnits = organizationalUnitService.getOrganizationalUnits();
        final Set<OrganizationalUnit> authorizedOrganizationalUnits = new HashSet<>();
        for ( OrganizationalUnit organizationalUnit : organizationalUnits ) {
            if ( authorizationManager.authorize( organizationalUnit,
                    identity ) ) {
                authorizedOrganizationalUnits.add( organizationalUnit );
            }
        }
        return authorizedOrganizationalUnits;
    }

    private Map<String, Repository> getRepositories( final OrganizationalUnit organizationalUnit ) {
        final Map<String, Repository> authorizedRepositories = new HashMap<>();
        if ( organizationalUnit == null ) {
            return authorizedRepositories;
        }
        //Reload OrganizationalUnit as the organizational unit's repository list might have been changed server-side
        final Collection<Repository> repositories = organizationalUnitService.getOrganizationalUnit( organizationalUnit.getName() ).getRepositories();
        for ( Repository repository : repositories ) {
            if ( authorizationManager.authorize( repository,
                    identity ) ) {
                authorizedRepositories.put( repository.getAlias(),
                        repository );
            }
        }
        return authorizedRepositories;
    }


    private Map<String, Project> getProjects( final Repository repository,
            final String branch ) {
        final Map<String, Project> authorizedProjects = new HashMap<>();

        if ( repository == null ) {
            return authorizedProjects;
        } else {
            Set<Project> allProjects = projectService.getProjects( repository,
                    branch );

            for ( Project project : allProjects ) {
                if ( authorizationManager.authorize( project,
                        identity ) ) {
                    authorizedProjects.put( project.getProjectName(),
                            project );
                }
            }

            return authorizedProjects;
        }
    }

}