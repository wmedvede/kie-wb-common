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
package org.kie.workbench.common.screens.datasource.management.client;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDef;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceManagementService;

@Dependent
public class DataSourceDefExplorer
        implements IsWidget,
        DataSourceDefExplorerView.Presenter {


    DataSourceDefExplorerView view;

    Caller<DataSourceManagementService> dataSourceService;

    @Inject
    public DataSourceDefExplorer( DataSourceDefExplorerView view,
            Caller<DataSourceManagementService> dataSourceService ) {
        this.view = view;
        this.dataSourceService = dataSourceService;
        view.init( this );
    }

    @PostConstruct
    public void init() {
        loadItems();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void loadDataSources() {
        view.clear();
        dataSourceService.call( new RemoteCallback<List<DataSourceDef>>() {
            @Override
            public void callback( List<DataSourceDef> dataSourceDefs ) {
                loadDataSources( dataSourceDefs );
            }
        }).getDataSources();

    }

    private void loadDataSources( List<DataSourceDef> dataSourceDefs ) {
        DataSourceDefItem item;
        for ( DataSourceDef dataSourceDef : dataSourceDefs ) {
            item = new DataSourceDefItem();
            item.setName( dataSourceDef.getName() );
            view.addItem( item );
        }
    }

    private void loadItems() {
        DataSourceDefItem item;

        for ( int i = 0; i < 10; i ++ ) {
            item = new DataSourceDefItem();
            item.setName( "Datasource def: " + i );
            view.addItem( item );
        }

    }

}
