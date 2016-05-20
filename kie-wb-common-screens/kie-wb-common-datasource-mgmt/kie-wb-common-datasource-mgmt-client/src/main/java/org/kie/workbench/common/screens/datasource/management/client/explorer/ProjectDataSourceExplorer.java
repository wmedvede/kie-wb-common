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

package org.kie.workbench.common.screens.datasource.management.client.explorer;

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
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefExplorer;
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

    OrganizationalUnit activeOrganizationalUnit;

    Repository activeRepository;

    Project activeProject;

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

    public void refresh( DataSourceExplorerContentQuery query ) {

        explorerService.call( getLoadContentCallback(), new DefaultErrorCallback() ).executeQuery( query );

    }

    public void loadContent( DataSourceExplorerContentQueryResult content ) {
        view.loadContent( content.getOrganizationalUnits(), activeOrganizationalUnit,
                content.getRepositories(), activeRepository,
                content.getProjects(), activeProject );
        dataSourceDefExplorer.loadDataSources( content.getDataSourceDefs() );
    }

    private RemoteCallback<?> getLoadContentCallback() {
        return new RemoteCallback<DataSourceExplorerContentQueryResult> () {
            @Override
            public void callback( DataSourceExplorerContentQueryResult content ) {
                loadContent( content );
            }
        };
    }

    public void onOrganizationalUnitSelected( OrganizationalUnit ou ) {
        Window.alert( "OrganizationalUnit Selected: " + ou );
        if ( ouChanged( ou ) ) {
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            query.setOrganizationalUnit( ou );
            refresh( query );
        }
    }

    public void onRepositorySelected( Repository repository ) {
        Window.alert( "Repository Selected: " + repository );
        if ( repositoryChanged( repository ) ) {
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            if ( activeOrganizationalUnit != null ) {
                query.setOrganizationalUnit( activeOrganizationalUnit );
                query.setRepository( repository );
            }
            refresh( query );
        }
    }

    public void onProjectSelected( Project project ) {
        Window.alert( "Project Selected: " + project );
        if ( projectChanged( project ) ) {
            DataSourceExplorerContentQuery query = new DataSourceExplorerContentQuery();
            if ( activeOrganizationalUnit != null && activeRepository != null ) {
                query.setOrganizationalUnit( activeOrganizationalUnit );
                query.setRepository( activeRepository );
                query.setProject( project );
            }
            refresh( query );
        }
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    private boolean ouChanged( OrganizationalUnit ou ) {
        return activeOrganizationalUnit != null ? !activeOrganizationalUnit.equals( ou ) : ou != null;
    }

    private boolean repositoryChanged( Repository repository ) {
        return activeRepository != null ? !activeRepository.equals( repository ) : repository != null;
    }

    private boolean projectChanged( Project project ) {
        return activeProject != null ? !activeProject.equals( project ) : project != null;
    }
}
