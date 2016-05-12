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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class DataSourceDefItem
        implements IsWidget,
        DataSourceDefItemView.Presenter {


    private DataSourceDefItemView view;

    private DataSourceDefItemView.ItemHandler itemHandler;

    private static int itemIds = 0;

    private String itemId = "item_"+ itemIds++;

    @Inject
    public DataSourceDefItem( DataSourceDefItemView view ) {
        this.view = view;
        view.init( this );
    }

    public void setName( String name ) {
        view.setName( name );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void onClick() {
        if ( itemHandler != null ) {
            itemHandler.onClick( getId() );
        }
    }

    @Override
    public void addItemHandler( DataSourceDefItemView.ItemHandler itemHandler ) {
        this.itemHandler = itemHandler;
    }

    public String getId() {
        return itemId;
    }
}