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
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQuery;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQueryResult;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerService;
import org.kie.workbench.common.services.shared.project.KieProject;
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

    private IOService ioService;

    private KieProjectService projectService;

    private OrganizationalUnitService organizationalUnitService;

    private DataSourceServicesHelper serviceHelper;

    private AuthorizationManager authorizationManager;

    private User identity;

    public DataSourceExplorerServiceImpl() {
    }

    @Inject
    public DataSourceExplorerServiceImpl( final @Named( "ioStrategy" ) IOService ioService,
            final KieProjectService projectService,
            final OrganizationalUnitService organizationalUnitService,
            final DataSourceServicesHelper serviceHelper,
            final AuthorizationManager authorizationManager,
            final User identity ) {
        this.ioService = ioService;
        this.projectService = projectService;
        this.serviceHelper = serviceHelper;
        this.organizationalUnitService = organizationalUnitService;
        this.authorizationManager = authorizationManager;
        this.identity = identity;
    }

    @Override
    public Collection<DataSourceDefInfo> findGlobalDataSources() {
        return getDataSources( serviceHelper.getGlobalDataSourcesContext() );
    }

    @Override
    public Collection<DataSourceDefInfo> findProjectDataSources( final Path path ) {
        checkNotNull( "path", path );
        KieProject project = projectService.resolveProject( path );
        if ( project == null ) {
            return new ArrayList<>( );
        } else {
            return getDataSources( serviceHelper.getProjectDataSourcesContext( project ) );
        }
    }

    @Override
    public DataSourceExplorerContentQueryResult executeQuery( final DataSourceExplorerContentQuery query ) {
        checkNotNull( "query", query );
        if ( query.isGlobalQuery() ) {
            DataSourceExplorerContentQueryResult result = new DataSourceExplorerContentQueryResult();
            result.setDataSourceDefs( getDataSources( serviceHelper.getGlobalDataSourcesContext() ) );
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

        //get the datasources for the selected project.
        result.setDataSourceDefs( getDataSourceDefInfos( query.getProject() ) );
        return result;
    }

    private boolean containsOU( Collection<OrganizationalUnit> organizationalUnits, OrganizationalUnit ou ) {
        for ( OrganizationalUnit unit : organizationalUnits ) {
            if ( unit.getName().equals( ou.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    private Collection<DataSourceDefInfo> getDataSourceDefInfos( final Project project ) {
        if ( project != null ) {
            Path rootPath = project.getRootPath();
            org.uberfire.java.nio.file.Path dataSourcesNioPath =
                    Paths.convert( rootPath ).resolve( "src/main/resources/META-INF" );
            return getDataSources( Paths.convert( dataSourcesNioPath ) );
        } else {
            return new ArrayList<>( );
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
        final Map<String, Project> authorizedProjects = new HashMap<String, Project>();

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