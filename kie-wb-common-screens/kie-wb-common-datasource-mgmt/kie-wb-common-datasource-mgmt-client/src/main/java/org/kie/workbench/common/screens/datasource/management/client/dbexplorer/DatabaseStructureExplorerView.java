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

import java.util.List;

import com.google.gwt.view.client.AsyncDataProvider;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.common.HasBusyIndicator;

public interface DatabaseStructureExplorerView
        extends UberElement< DatabaseStructureExplorerView.Presenter >, HasBusyIndicator {

    interface Presenter {

        void onSchemaChange( );

        void onDatabaseObjectTypeChange( );

        void onSearch( );

        void onDataSourceChange( );

        void onOpen( DatabaseObjectRow row );
    }

    String getDataSource( );

    String getSchema( );

    String getDatabaseObjectType( );

    String getSearchTerm( );

    void loadDataSourceOptions( final List< Pair< String, String > > options, String selectedOption );

    void loadDataSourceOptions( final List< Pair< String, String > > options );

    void loadSchemaOptions( final List< Pair< String, String > > options );

    void loadDatabaseObjectTypeOptions( final List< Pair< String, String > > options );

    void setDataProvider( AsyncDataProvider< DatabaseObjectRow > dataProvider );

    void enableDataSourceSelector( boolean enabled );

    void redraw( );
}