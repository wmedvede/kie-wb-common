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
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
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
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;
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

    private TranslationService translationService;

    private DisplayerSettingsJSONMarshaller jsonMarshaller = DisplayerSettingsJSONMarshaller.get();

    private List<DatabaseObjectRow> rows = new ArrayList<>(  );

    private DatabaseStructureExplorerSettings settings;

    @Inject
    public DatabaseStructureExplorer( DatabaseStructureExplorerView view,
                                      Caller< DataSourceDefQueryService > queryService,
                                      Caller< DatabaseMetadataService > metadataService,
                                      Caller< DataManagementService > managementService,
                                      PlaceManager placeManager,
                                      TranslationService translationService ) {
        this.view = view;
        view.init( this );
        this.queryService = queryService;
        this.metadataService = metadataService;
        this.managementService = managementService;
        this.placeManager = placeManager;
        this.translationService = translationService;
    }

    @PostConstruct
    private void init( ) {
        initializeDatabaseObjectOptions( );
        dataProvider = new AsyncDataProvider<DatabaseObjectRow>() {
            @Override
            protected void onRangeChanged( HasData<DatabaseObjectRow> display ) {
                updateRowCount( rows.size(), true );
                updateRowData( 0, rows );
            }
        };
        view.setDataProvider( dataProvider );
    }

    public void initialize( ) {
        initialize( new DatabaseStructureExplorerSettings( true ) );
    }

    public void initialize( DatabaseStructureExplorerSettings settings ) {
        this.settings = settings;
        loadDataSources( );
    }

    public DatabaseStructureExplorerView getView( ) {
        return view;
    }

    @Override
    public void onDataSourceChange( ) {
        rows.clear();
        dataProvider.updateRowCount( rows.size(), true );
        dataProvider.updateRowData( 0, rows );
        view.redraw();
        loadSchemas( view.getDataSource( ) );
    }

    @Override
    public void onSchemaChange( ) {
    }

    @Override
    public void onDatabaseObjectTypeChange( ) {
    }

    @Override
    public void onSearch( ) {
        search( view.getDataSource(), view.getSchema(), view.getDatabaseObjectType(), view.getSearchTerm() );
    }

    private void search( String dataSource, String schema, String databaseObjectType, String searchTerm ) {
        view.showBusyIndicator( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_loadingDbObjects) );
        metadataService.call( new RemoteCallback< List<TableMetadata> >( ) {
            @Override
            public void callback( List< TableMetadata > response ) {
                view.hideBusyIndicator();
                loadTables( response );
            }
        }, new HasBusyIndicatorDefaultErrorCallback( view ) ).findTables( dataSource,
                schema, buildSearchTerm( searchTerm ), DatabaseMetadata.TableType.valueOf( databaseObjectType ) );
    }

    @Override
    public void onOpen( DatabaseObjectRow row ) {
        openTable( view.getDataSource(), view.getSchema(), row.getName() );
    }

    private void openTable( String dataSourceUuid, String schema, String tableName ) {
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

    private void loadDataSources(  ) {
        queryService.call( new RemoteCallback< Collection< DataSourceDefInfo > >( ) {
            @Override
            public void callback( Collection< DataSourceDefInfo > result ) {
                loadDataSources( result );
            }

        }, new DefaultErrorCallback( ) ).findGlobalDataSources( true );
    }

    private void loadDataSources( Collection< DataSourceDefInfo > dataSourceInfos ) {
        List< Pair< String, String > > options = new ArrayList<>( );
        boolean selectedOptionDeployed = false;
        for ( DataSourceDefInfo defInfo : dataSourceInfos ) {
            if ( defInfo.isDeployed( ) ) {
                if ( !selectedOptionDeployed ) {
                    selectedOptionDeployed = defInfo.getUuid().equals( settings.getSelectedDataSourceUuid() );
                }
                if ( settings.isEnableDataSourceSelection() ) {
                    options.add( new Pair<>( defInfo.getName( ), defInfo.getUuid( ) ) );
                } else if ( defInfo.getUuid().equals( settings.getSelectedDataSourceUuid() ) ){
                    options.add( new Pair< >( defInfo.getName(), defInfo.getUuid()  ) );
                }
            }
        }
        if ( settings.getSelectedDataSourceUuid() != null && selectedOptionDeployed ) {
            view.loadDataSourceOptions( options, settings.getSelectedDataSourceUuid() );
        } else {
            view.loadDataSourceOptions( options );
        }

        if ( options.size( ) > 0 ) {
            loadSchemas( view.getDataSource() );
        }
        view.enableDataSourceSelector( settings.isEnableDataSourceSelection() );
    }

    private void loadSchemas( String dataSourceUuid ) {
        view.showBusyIndicator( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_loadingDbSchemas ) );
        metadataService.call( new RemoteCallback< DatabaseMetadata >( ) {
            @Override
            public void callback( DatabaseMetadata metadata ) {
                view.hideBusyIndicator();
                loadSchemas( metadata );
            }
        }, new HasBusyIndicatorDefaultErrorCallback( view ) ).getMetadata( dataSourceUuid, false, true );
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
            rows.add( new DatabaseObjectRow( metadata.getTableName(), metadata.getTableType() ) );
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

    public static class DatabaseStructureExplorerSettings {

        private String selectedDataSourceUuid;

        private boolean enableDataSourceSelection;

        public DatabaseStructureExplorerSettings( boolean enableDataSourceSelection ) {
            this.enableDataSourceSelection = enableDataSourceSelection;
        }

        public DatabaseStructureExplorerSettings( boolean enableDataSourceSelection, String selectedDataSourceUuid ) {
            this.enableDataSourceSelection = enableDataSourceSelection;
            this.selectedDataSourceUuid = selectedDataSourceUuid;
        }

        public String getSelectedDataSourceUuid( ) {
            return selectedDataSourceUuid;
        }

        public boolean isEnableDataSourceSelection( ) {
            return enableDataSourceSelection;
        }
    }
}