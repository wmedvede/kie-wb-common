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

package org.kie.workbench.common.screens.datasource.management.client.dbexplorer.dbobjects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.kie.workbench.common.screens.datasource.management.metadata.DatabaseMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.SchemaMetadata;
import org.kie.workbench.common.screens.datasource.management.metadata.TableMetadata;
import org.kie.workbench.common.screens.datasource.management.service.DataManagementService;
import org.kie.workbench.common.screens.datasource.management.service.DatabaseMetadataService;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.callbacks.HasBusyIndicatorDefaultErrorCallback;

@Dependent
public class DatabaseObjectExplorer
        implements DatabaseObjectExplorerView.Presenter, IsElement {

    private DatabaseObjectExplorerView view;

    private Caller< DatabaseMetadataService > metadataService;

    private Caller< DataManagementService > managementService;

    private AsyncDataProvider<DatabaseObjectRow> dataProvider;

    private PlaceManager placeManager;

    private TranslationService translationService;

    private List<DatabaseObjectRow> rows = new ArrayList<>(  );

    private Settings settings;

    private DatabaseObjectExplorerView.Handler handler;

    public DatabaseObjectExplorer( ) {
    }

    @Inject
    public DatabaseObjectExplorer( DatabaseObjectExplorerView view,
                                   Caller< DatabaseMetadataService > metadataService,
                                   Caller< DataManagementService > managementService,
                                   PlaceManager placeManager,
                                   TranslationService translationService ) {
        this.view = view;
        view.init( this );
        this.metadataService = metadataService;
        this.managementService = managementService;
        this.placeManager = placeManager;
        this.translationService = translationService;
    }

    @Override
    public HTMLElement getElement( ) {
        return view.getElement();
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
        initialize( new Settings( )
                .showSchemaSelection( true )
                .showObjectTypeFilter( true )
                .showObjectNameFilter( true ) );
    }

    public void initialize( Settings settings ) {
        this.settings = settings;
        view.showSchemaSelector( settings.isShowSchemaSelection() );
        view.showObjectTypeFilter( settings.isShowObjectTypeFilter() );
        view.showObjectNameFilter( settings.isShowObjectNameFilter() );
        view.showFilterButton( settings.isShowObjectTypeFilter() || settings.isShowObjectNameFilter() );
        view.showHeaderPanel( settings.isShowSchemaSelection() ||
                settings.isShowObjectTypeFilter() || settings.isShowObjectNameFilter() );
        if ( settings.isShowSchemaSelection() ) {
            loadSchemas( settings.getDataSourceUuid(), settings.getSelectedSchemaName() );
        } else {
            search( settings.getDataSourceUuid(), settings.getSelectedSchemaName(), DatabaseMetadata.TableType.ALL.name(), "%"  );
        }
    }

    public void addHandler( DatabaseObjectExplorerView.Handler handler ) {
        this.handler = handler;
    }

    @Override
    public void onSchemaChange( ) {
    }

    @Override
    public void onDatabaseObjectTypeChange( ) {
    }

    @Override
    public void onSearch( ) {
        search( settings.getDataSourceUuid(), getSchema(), view.getObjectType( ), view.getFilterTerm( ) );
    }

    private void search( String dataSource, String schema, String databaseObjectType, String searchTerm ) {
        view.showBusyIndicator( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseObjectExplorerViewImpl_loadingDbObjects) );
        metadataService.call( new RemoteCallback< List<TableMetadata> >( ) {
            @Override
            public void callback( List< TableMetadata > response ) {
                view.hideBusyIndicator();
                loadTables( response );
            }
        }, new HasBusyIndicatorDefaultErrorCallback( view ) ).findTables( dataSource,
                schema, buildSearchTerm( searchTerm ), DatabaseMetadata.TableType.valueOf( databaseObjectType ) );
    }

    private String getSchema() {
        if ( settings.isShowSchemaSelection() ) {
            return view.getSchema();
        } else {
            return settings.getSelectedSchemaName();
        }
    }

    private void clear() {
        rows.clear( );
        dataProvider.updateRowCount( rows.size( ), true );
        dataProvider.updateRowData( 0, rows );
        view.redraw( );
    }

    @Override
    public void onOpen( DatabaseObjectRow row ) {
        handler.onOpen( getSchema(), row.getName() );
    }

    private void loadSchemas( String dataSourceUuid, String selectedSchema ) {
        view.showBusyIndicator( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseObjectExplorerViewImpl_loadingDbSchemas ) );
        metadataService.call( new RemoteCallback< DatabaseMetadata >( ) {
            @Override
            public void callback( DatabaseMetadata metadata ) {
                view.hideBusyIndicator();
                loadSchemas( metadata, selectedSchema );
            }
        }, new HasBusyIndicatorDefaultErrorCallback( view ) ).getMetadata( dataSourceUuid, false, true );
    }

    private void loadSchemas( DatabaseMetadata metadata, String selectedSchema ) {
        String currentSchema = null;
        List< Pair< String, String > > options = new ArrayList<>( );
        for ( SchemaMetadata schemaMetadata : metadata.getSchemas( ) ) {
            if ( schemaMetadata.getSchemaName().equals( selectedSchema ) ) {
                currentSchema = selectedSchema;
            }
            options.add( new Pair<>( schemaMetadata.getSchemaName( ), schemaMetadata.getSchemaName( ) ) );
        }
        if ( currentSchema == null && metadata.getSchemas().size() > 0 ) {
            currentSchema = metadata.getSchemas().get( 0 ).getSchemaName();
        }

        view.loadSchemaOptions( options, currentSchema );

        search( settings.getDataSourceUuid(), currentSchema, DatabaseMetadata.TableType.ALL.name(), "%" );
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

    public static class Settings {

        /**
         * Configures the data source that will be explored.
         */
        private String dataSourceUuid;

        /**
         * When set it's the pre-configured database schema, otherwise the first available schema will be set.
         */
        private String selectedSchemaName;

        /**
         * Indicates if the schema selector should be visible.
         */
        private boolean showSchemaSelection;

        /**
         * When true, the filtering of data objects is available.
         */
        private boolean showObjectTypeFilter;

        private boolean showObjectNameFilter;

        public Settings( ) {
        }

        public String getDataSourceUuid( ) {
            return dataSourceUuid;
        }

        public Settings dataSourceUuid( String selectedDataSourceUuid ) {
            this.dataSourceUuid = selectedDataSourceUuid;
            return this;
        }

        public String getSelectedSchemaName( ) {
            return selectedSchemaName;
        }

        public Settings selectedSchemaName( String selectedSchemaName ) {
            this.selectedSchemaName = selectedSchemaName;
            return this;
        }

        public boolean isShowSchemaSelection( ) {
            return showSchemaSelection;
        }

        public Settings showSchemaSelection( boolean showSchemaSelection ) {
            this.showSchemaSelection = showSchemaSelection;
            return this;
        }

        public boolean isShowObjectTypeFilter( ) {
            return showObjectTypeFilter;
        }

        public Settings showObjectTypeFilter( boolean showObjectTypeFilter ) {
            this.showObjectTypeFilter = showObjectTypeFilter;
            return this;
        }

        public boolean isShowObjectNameFilter( ) {
            return showObjectNameFilter;
        }

        public Settings showObjectNameFilter( boolean showObjectNameFilter ) {
            this.showObjectNameFilter = showObjectNameFilter;
            return this;
        }
    }
}