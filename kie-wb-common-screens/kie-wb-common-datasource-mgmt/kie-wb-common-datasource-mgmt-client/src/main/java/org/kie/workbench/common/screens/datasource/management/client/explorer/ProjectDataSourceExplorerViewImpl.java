/*
 * Copyright 2016 JBoss Inc
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

import java.util.Collection;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Composite;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefExplorer;
import org.kie.workbench.common.screens.explorer.client.widgets.navigator.NavigatorExpandCollapseButton;

@Dependent
@Templated
public class ProjectDataSourceExplorerViewImpl
        extends Composite
        implements ProjectDataSourceExplorerView {

    @Inject
    @DataField( "project-selector" )
    private ProjectSelector projectSelector;

    @Inject
    @DataField( "datasource-explorer-container")
    private FlowPanel container;

    private Presenter presenter;

    public ProjectDataSourceExplorerViewImpl() {
    }

    @Override
    public void init( final Presenter presenter ) {
        this.presenter = presenter;
    }

    @PostConstruct
    void init() {
        projectSelector.init( NavigatorExpandCollapseButton.Mode.COLLAPSED, null );
        ///container.add( projectSelector );
    }

    @Override
    public void loadContent( final Collection<OrganizationalUnit> organizationalUnits,
            final OrganizationalUnit activeOrganizationalUnit,
            final Collection<Repository> repositories,
            final Repository activeRepository,
            final Collection<Project> projects,
            final Project activeProject ) {

        projectSelector.setupHeader( Collections.organizationalUnits,
                activeOrganizationalUnit,
                repositories,
                activeRepository,
                projects,
                activeProject );
    }

    @Override
    public void addProjectSelectorHandler( ProjectSelectorHandler handler ) {
        projectSelector.addProjectSelectorHandler( handler );
    }

    @Override
    public void setDataSourceDefExplorer( DataSourceDefExplorer dataSourceDefExplorer ) {
        container.add( dataSourceDefExplorer );
    }
}