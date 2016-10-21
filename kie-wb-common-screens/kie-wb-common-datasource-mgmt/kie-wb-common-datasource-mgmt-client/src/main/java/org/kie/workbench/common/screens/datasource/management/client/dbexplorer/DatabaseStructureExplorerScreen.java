/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management.client.dbexplorer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@Dependent
@WorkbenchScreen( identifier = "DatabaseStructureExplorerScreen" )
public class DatabaseStructureExplorerScreen {

    @Inject
    private DatabaseStructureExplorer browser;

    private PlaceRequest placeRequest;

    public DatabaseStructureExplorerScreen( ) {
    }

    @OnStartup
    public void onStartup( final PlaceRequest placeRequest ) {
        this.placeRequest = placeRequest;
        String dataSourceUuid = placeRequest.getParameter( "dataSourceUuid", null );
        String dataDataSourceName = placeRequest.getParameter( "dataSourceName", "" );
        browser.initialize( new DatabaseStructureExplorer.Settings( )
                .dataSourceUuid( dataSourceUuid )
                .dataSourceName( dataDataSourceName ), null );
    }

    @WorkbenchPartView
    public IsWidget getView( ) {
        return ElementWrapperWidget.getWidget( browser.getElement( ) );
    }

    @WorkbenchPartTitle
    public String getTitle( ) {
        return "Database Structure Screen";
    }

}


