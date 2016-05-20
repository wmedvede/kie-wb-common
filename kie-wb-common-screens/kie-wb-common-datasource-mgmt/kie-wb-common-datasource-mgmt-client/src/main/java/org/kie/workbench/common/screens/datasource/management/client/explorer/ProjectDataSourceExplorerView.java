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

import org.guvnor.common.services.project.model.Project;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefExplorer;
import org.uberfire.client.mvp.UberView;

public interface ProjectDataSourceExplorerView
        extends UberView<ProjectDataSourceExplorerView.Presenter> {

    interface Presenter {

    }

    void loadContent( Collection<OrganizationalUnit> organizationalUnits,
            OrganizationalUnit activeOrganizationalUnit,
            Collection<Repository> repositories,
            Repository activeRepository,
            Collection<Project> projects,
            Project activeProject );

    void addProjectSelectorHandler( ProjectSelectorHandler handler );

    void setDataSourceDefExplorer( DataSourceDefExplorer dataSourceDefExplorer );
}
