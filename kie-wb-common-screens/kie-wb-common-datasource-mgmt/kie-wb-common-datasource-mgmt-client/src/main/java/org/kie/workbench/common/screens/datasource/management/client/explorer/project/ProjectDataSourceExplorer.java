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

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
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
import org.kie.workbench.common.screens.datasource.management.client.explorer.common.DataSourceDefExplorerView;
import org.kie.workbench.common.screens.datasource.management.client.wizard.NewDataSourceDefWizard;
import org.kie.workbench.common.screens.datasource.management.events.NewDataSourceEvent;
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

    private NewDataSourceDefWizard newDataSourceDefWizard;

    private Caller<DataSourceExplorerService> explorerService;

    private OrganizationalUnit activeOrganizationalUnit;

    private Repository activeRepository;

    private Project activeProject;

    private String activeBranch = "master";

    @Inject
    public ProjectDataSourceExplorer( final ProjectDataSourceExplorerView view,
            final DataSourceDefExplorer dataSourceDefExplorer,
            final NewDataSourceDefWizard newDataSourceDefWizard,
            final Caller<DataSourceExplorerService> explorerService ) {
        this.view = view;
        this.dataSourceDefExplorer = dataSourceDefExplorer;
        this.newDataSourceDefWizard = newDataSourceDefWizard;
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
        dataSourceDefExplorer.setHandler( new DataSourceDefExplorerView.Handler() {
            @Override
            public void onAddDataSource() {
                ProjectDataSourceExplorer.this.onAddDataSource();
            }

            @Override
            public void onAddDriver() {
                ProjectDataSourceExplorer.this.onAddDriver();
            }
        } );
    }

    private void onAddDriver() {
        Window.alert( "Not yet implemented" );
    }

    private void onAddDataSource() {
        final Project activeProjet = getActiveProject();
        if ( activeProjet == null ) {
            Window.alert( "No project has been selected" );
        } else {
            newDataSourceDefWizard.setProject( getActiveProject() );
            newDataSourceDefWizard.start();
        }
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

    private void loadContent( final DataSourceExplorerContentQueryResult content ) {

        dataSourceDefExplorer.clear();
        if ( activeOrganizationalUnit == null || !contains( content.getOrganizationalUnits(), activeOrganizationalUnit ) ) {
            //no organizational unit was selected or the previously selected one has been deleted at server side.

            if ( content.getOrganizationalUnits() != null && content.getOrganizationalUnits().size() > 0 ) {
                //let's select the first one
                activeOrganizationalUnit = content.getOrganizationalUnits().iterator().next();
                activeRepository = null;
                activeProject = null;
                //try a refresh.
                refresh( );
            } else {
                //there are no organizational units, nothing to do.
                activeOrganizationalUnit = null;
                activeRepository = null;
                activeProject = null;
                view.clear();
            }

        } else if ( activeRepository == null || !contains( content.getRepositories(), activeRepository ) ) {
            //an organizational unit was selected and is in the result but no repository was selected or the previously
            //selected one has been deleted at server side.

            if ( content.getRepositories() != null && content.getRepositories().size() > 0 ) {
                //let's select the first one.
                activeRepository = content.getRepositories().iterator().next();
                activeProject = null;
                //try a refresh.
                refresh();
            } else {
                //there are no repositories for the activeOrganizationalUnit
                activeRepository = null;
                activeProject = null;
                view.loadContent( content.getOrganizationalUnits(), activeOrganizationalUnit,
                        new ArrayList<>( ), activeRepository,
                        new ArrayList<>( ), activeProject );
            }
        } else if ( activeProject == null || !contains( content.getProjects(), activeProject ) ) {
            //an organization unit and a repository were selected and both are in the result, but no project is
            //selected or the selected one has been deleted at server side.
            if ( content.getProjects() != null && content.getProjects().size() > 0 ) {
                activeProject = content.getProjects().iterator().next();
                //try a refresh.
                refresh();
            } else {
                activeProject = null;
                view.loadContent( content.getOrganizationalUnits(), activeOrganizationalUnit,
                        content.getRepositories(), activeRepository,
                        new ArrayList<>( ), activeProject );
            }
        } else {
            //an organizational unit, repository and project are selected and are in the result.
            //just load the view with the results.

            view.loadContent( content.getOrganizationalUnits(), activeOrganizationalUnit,
                    content.getRepositories(), activeRepository,
                    content.getProjects(), activeProject );
            dataSourceDefExplorer.loadDataSources( content.getDataSourceDefs() );
        }

    }

    private boolean contains( Collection<Repository> repositories, Repository activeRepository ) {
        if ( repositories != null ) {
            for ( Repository repository : repositories ) {
                if ( repository.getAlias().equals( activeRepository.getAlias() ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contains( Collection<OrganizationalUnit> organizationalUnits, OrganizationalUnit activeOrganizationalUnit ) {
        if ( organizationalUnits != null ) {
            for ( OrganizationalUnit ou : organizationalUnits ) {
                if ( ou.getName().equals( activeOrganizationalUnit.getName() ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contains( Collection<Project> projects, Project activeProject ) {
        if ( projects != null ) {
            for ( Project project : projects ) {
                if ( project.getRootPath().equals( activeProject.getRootPath() ) ) {
                    return true;
                }
            }
        }
        return false;
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

    public OrganizationalUnit getActiveOrganizationalUnit() {
        return activeOrganizationalUnit;
    }

    public void setActiveOrganizationalUnit( OrganizationalUnit activeOrganizationalUnit ) {
        this.activeOrganizationalUnit = activeOrganizationalUnit;
    }

    public Repository getActiveRepository() {
        return activeRepository;
    }

    public void setActiveRepository( Repository activeRepository ) {
        this.activeRepository = activeRepository;
    }

    public Project getActiveProject() {
        return activeProject;
    }

    public void setActiveProject( Project activeProject ) {
        this.activeProject = activeProject;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public void onDataSourceCreated( @Observes NewDataSourceEvent event ) {
        if ( !event.isGlobal() && activeProject != null && activeProject.equals( event.getProject() ) ) {
            refresh();
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