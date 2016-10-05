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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.json.DisplayerSettingsJSONMarshaller;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datasource.management.metadata.DatabaseMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.SchemaMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.TableMetadata;
import org.kie.workbench.common.screens.datasource.management.model.DataSourceDefInfo;
import org.kie.workbench.common.screens.datasource.management.service.DataManagementService;
import org.kie.workbench.common.screens.datasource.management.service.DataSourceDefQueryService;
import org.kie.workbench.common.screens.datasource.management.service.DatabaseMetadataService;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

@Dependent
public class DatabaseStructureExplorer
        implements DatabaseStructureExplorerView.Presenter {

    private DatabaseStructureExplorerView view;

    private Caller< DataSourceDefQueryService > queryService;

    private Caller< DatabaseMetadataService > metadataService;

    private Caller< DataManagementService > managementService;

    private AsyncDataProvider<DatabaseObjectRow> dataProvider;

    private PlaceManager placeManager;

    private DisplayerSettingsJSONMarshaller jsonMarshaller = DisplayerSettingsJSONMarshaller.get();

    private List<DatabaseObjectRow> rows = new ArrayList<>(  );

    @Inject
    public DatabaseStructureExplorer( DatabaseStructureExplorerView view,
                                      Caller< DataSourceDefQueryService > queryService,
                                      Caller< DatabaseMetadataService > metadataService,
                                      Caller< DataManagementService > managementService,
                                      PlaceManager placeManager ) {
        this.view = view;
        view.init( this );
        this.queryService = queryService;
        this.metadataService = metadataService;
        this.managementService = managementService;
        this.placeManager = placeManager;
    }

    @PostConstruct
    private void init( ) {
        initializeDatabaseObjectOptions( );
        loadDataSources( );
        initializeDummyRows();
        dataProvider = new AsyncDataProvider<DatabaseObjectRow>() {
            @Override
            protected void onRangeChanged( HasData<DatabaseObjectRow> display ) {
                updateRowCount( rows.size(), true );
                updateRowData( 0, rows );
            }
        };
        view.setDataProvider( dataProvider );
    }

    private void initializeDummyRows( ) {
        for ( int i = 0; i < 50 ; i++ ) {
            rows.add( new DatabaseObjectRow( "TABLE_" + i ) );
        }
    }

    public DatabaseStructureExplorerView getView( ) {
        return view;
    }

    @Override
    public void onDataSourceChange( ) {
        loadSchemas( view.getDataSource( ) );
    }

    @Override
    public void onSchemaChange( ) {
        //Window.alert( "onSchemaChange: " + view.getSchema( ) );
    }

    @Override
    public void onDatabaseObjectTypeChange( ) {
        //Window.alert( "onDatabaseObjectTypeChange: " + view.getDatabaseObjectType( ) );
    }

    @Override
    public void onSearch( ) {
        search( view.getDataSource(), view.getSchema(), view.getDatabaseObjectType(), view.getSearchTerm() );
    }

    private void search( String dataSource, String schema, String databaseObjectType, String searchTerm ) {
        metadataService.call( new RemoteCallback< List<TableMetadata> >( ) {
            @Override
            public void callback( List< TableMetadata > response ) {
                loadTables( response );
            }
        }, new DefaultErrorCallback() ).findTables( dataSource,
                schema, buildSearchTerm( searchTerm ), DatabaseMetadata.TableType.valueOf( databaseObjectType ) );
    }

    @Override
    public void onOpen( DatabaseObjectRow row ) {
        openTable( view.getDataSource(), view.getSchema(), row.getName() );
    }

    private void openTable( String dataSourceUuid, String schema, String tableName ) {
        tableName = "\"" + tableName + "\"";
        managementService.call( new RemoteCallback< DisplayerSettings >( ) {
            @Override
            public void callback( DisplayerSettings displayerSettings ) {
                PlaceRequest placeRequest = createPlaceRequest( displayerSettings );
                placeManager.goTo( placeRequest );
            }
        }, new DefaultErrorCallback( ) ).getDisplayerSettings( dataSourceUuid, schema, tableName );
    }

    private PlaceRequest createPlaceRequest( DisplayerSettings displayerSettings ) {
        String json = jsonMarshaller.toJsonString( displayerSettings );
        Map< String, String > params = new HashMap<>( );
        params.put( "json", json );
        params.put( "edit", "false" );
        params.put( "clone", "false" );
        return new DefaultPlaceRequest( "DisplayerScreen", params );
    }

    private void loadDataSources( ) {
        queryService.call( new RemoteCallback< Collection< DataSourceDefInfo > >( ) {
            @Override
            public void callback( Collection< DataSourceDefInfo > result ) {
                loadDataSources( result );
            }

        }, new DefaultErrorCallback( ) ).findGlobalDataSources( true );
    }

    private void loadDataSources( Collection< DataSourceDefInfo > dataSourceInfos ) {
        List< Pair< String, String > > options = new ArrayList<>( );
        for ( DataSourceDefInfo defInfo : dataSourceInfos ) {
            if ( defInfo.isDeployed( ) ) {
                options.add( new Pair<>( defInfo.getName( ), defInfo.getUuid( ) ) );
            }
        }
        view.loadDataSourceOptions( options );
        if ( options.size( ) > 0 ) {
            loadSchemas( options.get( 0 ).getK2( ) );
        }
    }

    private void loadSchemas( String dataSourceUuid ) {
        metadataService.call( new RemoteCallback< DatabaseMetadata >( ) {
            @Override
            public void callback( DatabaseMetadata metadata ) {
                loadSchemas( metadata );
            }
        }, new DefaultErrorCallback() ).getMetadata( dataSourceUuid, false, true );
    }

    private void loadSchemas( DatabaseMetadata metadata ) {
        List< Pair< String, String > > options = new ArrayList<>( );
        for ( SchemaMetadata schemaMetadata : metadata.getSchemas( ) ) {
            options.add( new Pair<>( schemaMetadata.getSchemaName( ), schemaMetadata.getSchemaName( ) ) );
        }
        view.loadSchemaOptions( options );
    }

    private void loadTables( List< TableMetadata > response ) {
        rows.clear();
        for ( TableMetadata metadata : response ) {
            rows.add( new DatabaseObjectRow( metadata.getTableName() ) );
        }
        dataProvider.updateRowCount( rows.size(), true );
        dataProvider.updateRowData( 0, rows );
        view.redraw();
    }

    private void initializeDatabaseObjectOptions( ) {
        List< Pair< String, String > > options = new ArrayList<>( );
        options.add( new Pair<>( DatabaseMetadata.TableType.ALL.name(), DatabaseMetadata.TableType.ALL.name() ) );
        options.add( new Pair<>( DatabaseMetadata.TableType.TABLE.name(), DatabaseMetadata.TableType.TABLE.name() ) );
        options.add( new Pair<>( DatabaseMetadata.TableType.VIEW.name(), DatabaseMetadata.TableType.VIEW.name() ) );
        view.loadDatabaseObjectTypeOptions( options );
    }

    private String buildSearchTerm( String searchTerm ) {
        if ( searchTerm == null || searchTerm.trim().isEmpty() ) {
            return "%";
        } else {
            return "%" + searchTerm.trim() + "%";
        }
    }
}