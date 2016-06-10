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

package org.kie.workbench.common.screens.datasource.management.client.explorer.global;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.client.explorer.common.DataSourceDefExplorer;
import org.kie.workbench.common.screens.datasource.management.client.explorer.common.DataSourceDefExplorerView;
import org.kie.workbench.common.screens.datasource.management.client.explorer.project.ProjectDataSourceExplorerView;
import org.kie.workbench.common.screens.datasource.management.client.wizard.NewDataSourceDefWizard;
import org.kie.workbench.common.screens.datasource.management.events.NewDataSourceEvent;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQuery;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerContentQueryResult;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceExplorerService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class GlobalDataSourceExplorer
        implements ProjectDataSourceExplorerView.Presenter,
        IsWidget {

    private GlobalDataSourceExplorerView view;

    private DataSourceDefExplorer dataSourceDefExplorer;

    private NewDataSourceDefWizard newDataSourceDefWizard;

    private Caller<DataSourceExplorerService> explorerService;

    @Inject
    public GlobalDataSourceExplorer( final GlobalDataSourceExplorerView view,
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
        dataSourceDefExplorer.setHandler( new DataSourceDefExplorerView.Handler() {
            @Override
            public void onAddDataSource() {
                GlobalDataSourceExplorer.this.onAddDataSource();
            }

            @Override
            public void onAddDriver() {
                GlobalDataSourceExplorer.this.onAddDriver();
            }
        } );
    }

    private void onAddDriver() {
        Window.alert("Not yet implemented");
    }

    private void onAddDataSource() {
        newDataSourceDefWizard.setGlobal();
        newDataSourceDefWizard.start();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void refresh() {
        explorerService.call( getRefreshCallback(), new DefaultErrorCallback() ).executeQuery(
                new DataSourceExplorerContentQuery( true ) );
    }

    private void loadContent( final DataSourceExplorerContentQueryResult content ) {
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

    public void onDataSourceCreated( @Observes NewDataSourceEvent event ) {
        if ( event.isGlobal() ) {
            refresh();
        }
    }

}