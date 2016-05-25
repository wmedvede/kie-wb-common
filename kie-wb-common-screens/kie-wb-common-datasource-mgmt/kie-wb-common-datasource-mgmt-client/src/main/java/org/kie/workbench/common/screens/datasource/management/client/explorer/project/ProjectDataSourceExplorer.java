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

package org.kie.workbench.common.screens.datasource.management.client.explorer.project;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.explorer.common.DataSourceDefExplorer;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQuery;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQueryResult;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class ProjectDataSourceExplorer
        implements ProjectDataSourceExplorerView.Presenter,
        IsWidget {

    private ProjectDataSourceExplorerView view;

    private DataSourceDefExplorer dataSourceDefExplorer;

    private Caller<DataSourceExplorerService> explorerService;

    private OrganizationalUnit activeOrganizationalUnit;

    private Repository activeRepository;

    private Project activeProject;

    private String activeBranch = "master";

    @Inject
    public ProjectDataSourceExplorer( final ProjectDataSourceExplorerView view,
            final DataSourceDefExplorer dataSourceDefExplorer,
            final Caller<DataSourceExplorerService> explorerService ) {
        this.view = view;
        this.dataSourceDefExplorer = dataSourceDefExplorer;
        this.explorerService = explorerService;
    }

    @PostConstruct
    private void init() {
        view.setDataSourceDefExplorer( dataSourceDefExplorer );
        view.addProjectSelectorHandler( new ProjectSelectorHandler() {
            @Override
            public void onOrganizationalUnitSelected( OrganizationalUnit ou ) {
                ProjectDataSourceExplorer.this.onOrganizationalUnitSelected( ou );
            }

            @Override
            public void onRepositorySelected( Repository repository ) {
                ProjectDataSourceExplorer.this.onRepositorySelected( repository );
            }

            @Override
            public void onProjectSelected( Project project ) {
                ProjectDataSourceExplorer.this.onProjectSelected( project );
            }
        } );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void refresh() {
        refresh( new DataSourceExplorerContentQuery( activeOrganizationalUnit,
                activeRepository,
                activeProject,
                activeBranch ) );
    }

    private void refresh( final DataSourceExplorerContentQuery query ) {
        explorerService.call( getRefreshCallback(), new DefaultErrorCallback() ).executeQuery( query );
    }

    public void loadContent( final DataSourceExplorerContentQueryResult content ) {

        //TODO aqui tendria que dar la oportunidad a compoarar el resultado contra la
        //seleccion q tenia la UI, porque podria darse el caso donde la OU ya no existe por ej
        //y en la UI la teniamos seleccionada.

        view.loadContent( content.getOrganizationalUnits(), activeOrganizationalUnit,
                content.getRepositories(), activeRepository,
                content.getProjects(), activeProject );
        dataSourceDefExplorer.loadDataSources( content.getDataSourceDefs() );
    }

    private RemoteCallback<?> getRefreshCallback() {
        return new RemoteCallback<DataSourceExplorerContentQueryResult>() {
            @Override
            public void callback( DataSourceExplorerContentQueryResult content ) {
                loadContent( content );
            }
        };
    }

    public void onOrganizationalUnitSelected( final OrganizationalUnit ou ) {
        Window.alert( "OrganizationalUnit Selected: " + ou );
        if ( hasChanged( ou ) ) {
            activeOrganizationalUnit = ou;
            activeRepository = null;
            activeProject = null;
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            query.setOrganizationalUnit( ou );
            refresh( query );
        }
    }

    public void onRepositorySelected( final Repository repository ) {
        Window.alert( "Repository Selected: " + repository );
        if ( hasChanged( repository ) ) {
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            if ( activeOrganizationalUnit != null ) {
                activeRepository = repository;
                activeProject = null;
                query.setOrganizationalUnit( activeOrganizationalUnit );
                query.setRepository( repository );
                query.setBranch( activeBranch );
            } else {
                activeRepository = null;
                activeProject = null;
            }
            refresh( query );
        }
    }

    public void onProjectSelected( final Project project ) {
        Window.alert( "Project Selected: " + project );
        if ( hasChanged( project ) ) {
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            if ( activeOrganizationalUnit != null && activeRepository != null ) {
                activeProject = project;
                query.setOrganizationalUnit( activeOrganizationalUnit );
                query.setRepository( activeRepository );
                query.setBranch( activeBranch );
                query.setProject( project );
            } else {
                activeProject = null;
            }
            refresh( query );
        }
    }

    private boolean hasChanged( final OrganizationalUnit ou ) {
        return activeOrganizationalUnit != null ? !activeOrganizationalUnit.equals( ou ) : ou != null;
    }

    private boolean hasChanged( final Repository repository ) {
        return activeRepository != null ? !activeRepository.equals( repository ) : repository != null;
    }

    private boolean hasChanged( final Project project ) {
        return activeProject != null ? !activeProject.equals( project ) : project != null;
    }


}