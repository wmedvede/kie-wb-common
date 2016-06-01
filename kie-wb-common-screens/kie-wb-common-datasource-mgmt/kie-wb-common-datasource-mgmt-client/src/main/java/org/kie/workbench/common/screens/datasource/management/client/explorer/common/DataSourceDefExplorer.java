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
package org.kie.workbench.common.screens.datasource.management.client.explorer.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.PathPlaceRequest;

@Dependent
public class DataSourceDefExplorer
        implements IsWidget,
        DataSourceDefExplorerView.Presenter {


    private DataSourceDefExplorerView view;

    private Instance<DataSourceDefItem> itemInstance;

    private Map<String, DataSourceDefInfo> itemsMap = new HashMap<>(  );

    private PlaceManager placeManager;

    @Inject
    public DataSourceDefExplorer( DataSourceDefExplorerView view,
            Instance<DataSourceDefItem> itemInstance,
            PlaceManager placeManager ) {
        this.view = view;
        this.itemInstance = itemInstance;
        this.placeManager = placeManager;

        view.init( this );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void loadDataSources( Collection<DataSourceDefInfo> dataSourceDefInfos ) {
        clear();
        if ( dataSourceDefInfos != null ) {
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
    }

    public void clear() {
        view.clear();
        itemsMap.clear();
    }

    private void onItemClick( DataSourceDefInfo dataSourceDefInfo ) {
        placeManager.goTo( new PathPlaceRequest( dataSourceDefInfo.getPath() ) );
    }

    private DataSourceDefItem createItem() {
        return itemInstance.get();
    }
}