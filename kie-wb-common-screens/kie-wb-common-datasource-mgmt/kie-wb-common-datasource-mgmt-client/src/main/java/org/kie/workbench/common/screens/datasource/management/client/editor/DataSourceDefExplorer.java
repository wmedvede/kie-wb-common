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
package org.kie.workbench.common.screens.datasource.management.client.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefEditorService;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.mvp.impl.PathPlaceRequest;

@Dependent
public class DataSourceDefExplorer
        implements IsWidget,
        DataSourceDefExplorerView.Presenter {


    DataSourceDefExplorerView view;

    Caller<DataSourceDefEditorService> editorService;

    Instance<DataSourceDefItem> itemInstance;

    Map<String, DataSourceDefInfo> itemsMap = new HashMap<>(  );

    PlaceManager placeManager;

    public DataSourceDefExplorer() {
    }

    @Inject
    public DataSourceDefExplorer( DataSourceDefExplorerView view,
            Caller<DataSourceDefEditorService> editorService,
            Instance<DataSourceDefItem> itemInstance,
            PlaceManager placeManager ) {
        this.view = view;
        this.editorService = editorService;
        this.itemInstance = itemInstance;
        this.placeManager = placeManager;

        view.init( this );
    }

    @PostConstruct
    private void init() {
        loadDataSources();
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void loadDataSources() {
        view.clear();
        editorService.call( new RemoteCallback<List<DataSourceDefInfo>>() {
            @Override
            public void callback( List<DataSourceDefInfo> dataSourceDefInfos ) {
                loadDataSources( dataSourceDefInfos );
            }
        }, new DefaultErrorCallback() ).getGlobalDataSources();
    }

    private void loadDataSources( List<DataSourceDefInfo> dataSourceDefInfos ) {
        DataSourceDefItem item;
        for ( DataSourceDefInfo dataSourceDefInfo : dataSourceDefInfos ) {
            item = createItem();
            item.setName( dataSourceDefInfo.getName() );
            item.addItemHandler( new DataSourceDefItemView.ItemHandler() {
                @Override
                public void onClick( String itemId ) {
                    onItemClick( itemsMap.get( itemId ) );
                }
            } );
            itemsMap.put( item.getId(), dataSourceDefInfo );
            view.addItem( item );
        }
    }

    private void onItemClick( DataSourceDefInfo dataSourceDefInfo ) {
        placeManager.goTo( new PathPlaceRequest( dataSourceDefInfo.getPath() ) );
    }

    protected DataSourceDefItem createItem() {
        return itemInstance.get();
    }


}