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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.common.BreadcrumbItem;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.common.InitializeCallback;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.dbobjects.DatabaseObjectExplorer;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.dbobjects.DatabaseObjectExplorerView;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.table.TableObjectViewer;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.schemas.DatabaseSchemaExplorer;
import org.kie.workbench.common.screens.datasource.management.client.dbexplorer.schemas.DatabaseSchemaExplorerView;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.uberfire.mvp.Command;

@Dependent
public class DatabaseStructureExplorer
        implements DatabaseStructureExplorerView.Presenter, IsElement {

    private DatabaseStructureExplorerView view;

    private DatabaseSchemaExplorer schemaExplorer;

    private DatabaseObjectExplorer objectExplorer;

    private TableObjectViewer objectViewer;

    private ManagedInstance<BreadcrumbItem> itemInstance;

    private BreadcrumbItem dataSourceBreadcrumbItem;

    private BreadcrumbItem schemasBreadcrumbItem;

    private BreadcrumbItem objectsBreadcrumbItem;

    private BreadcrumbItem objectViewerBreadcrumbItem;

    private List<BreadcrumbItem> currentBreadcrumbs = new ArrayList<>(  );

    private TranslationService translationService;

    private Settings settings;

    private DatabaseStructureExplorerView.Handler handler;

    @Inject
    public DatabaseStructureExplorer( DatabaseStructureExplorerView view,
                                      DatabaseSchemaExplorer schemaExplorer,
                                      DatabaseObjectExplorer objectExplorer,
                                      TableObjectViewer objectViewer,
                                      ManagedInstance<BreadcrumbItem> itemInstance,
                                      TranslationService translationService ) {
        this.view = view;
        view.init( this );
        this.schemaExplorer = schemaExplorer;
        this.objectExplorer = objectExplorer;
        this.objectViewer = objectViewer;
        this.itemInstance = itemInstance;
        this.translationService = translationService;
    }

    @Override
    public HTMLElement getElement( ) {
        return view.getElement();
    }

    @PostConstruct
    private void init( ) {

        dataSourceBreadcrumbItem = createItem(
                buildDisplayableName( DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dataSourceTitle ),
                new Command( ) {
                    @Override
                    public void execute( ) {
                        onDataSourceBreacrumbItemSelected( );
                    }
                } );

        schemasBreadcrumbItem = createItem(
                translationService.getTranslation( DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_schemasTitle ),
                new Command( ) {
                    @Override
                    public void execute( ) {
                        onShemasBreadcrumbItemSelected( );
                    }
                } );

        objectsBreadcrumbItem = createItem( "", new Command( ) {
            @Override
            public void execute( ) {
                onObjectsBreadcrumbItemSelected();
            }
        } );

        objectViewerBreadcrumbItem = createItem( "", new Command( ) {
            @Override
            public void execute( ) {
                objectViewerBreadcrumbItemSelected();
            }
        } );

        schemaExplorer.addHandler( new DatabaseSchemaExplorerView.Handler( ) {
            @Override
            public void onOpen( String schemaName ) {
                onSchemaSelected( schemaName );
            }
        } );

        objectExplorer.addHandler( new DatabaseObjectExplorerView.Handler( ) {
            @Override
            public void onOpen( String schemaName, String objectName ) {
                onDataBaseObjectSelected( schemaName, objectName );
            }
        } );
    }

    public void initialize( Settings settings, InitializeCallback callback ) {
        dataSourceBreadcrumbItem.setName( settings.dataSourceName( ) );
        this.settings = settings;
        schemaExplorer.initialize( new DatabaseSchemaExplorer.Settings( )
                        .dataSourceUuid( settings.dataSourceUuid( ) ),
                new InitializeCallback( ) {
                    @Override
                    public void onInitializeError( Throwable throwable ) {
                        if ( callback != null ) {
                            callback.onInitializeError( throwable );
                        }
                    }

                    @Override
                    public void onInitializeSuccess( ) {
                        showSchemas( );
                        if ( callback != null ) {
                            callback.onInitializeSuccess();
                        }
                    }
                } );
    }

    public void addHandler( DatabaseStructureExplorerView.Handler handler ) {
        this.handler = handler;
    }

    private void showSchemas() {
        if ( schemaExplorer.hasItems( ) ) {
            clearBreadcrumbs( );
            addBreadcrumbs( dataSourceBreadcrumbItem, schemasBreadcrumbItem );
            activateLastBreadcrum( true );
            view.clearContent( );
            view.setContent( schemaExplorer );
        } else {
            onSchemaSelected( null );
        }
    }

    private void onSchemaSelected( String schemaName ) {
        view.clearContent();
        view.setContent( objectExplorer );
        objectExplorer.initialize( new DatabaseObjectExplorer.Settings()
                .dataSourceUuid( settings.dataSourceUuid() )
                .selectedSchemaName( schemaName )
                .showSchemaSelection( false )
        );
        objectsBreadcrumbItem.setName( schemaName != null ? schemaName :
                translationService.getTranslation( DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_defaultSchema ) );
        clearBreadcrumbs();
        if ( schemaExplorer.hasItems() ) {
            addBreadcrumbs( dataSourceBreadcrumbItem, schemasBreadcrumbItem, objectsBreadcrumbItem );
        } else {
            addBreadcrumbs( dataSourceBreadcrumbItem, objectsBreadcrumbItem );
        }
        activateLastBreadcrum( true );
    }

    private void onDataBaseObjectSelected( String schemaName, String objectName ) {
        view.clearContent();
        view.setContent( objectViewer );
        objectViewer.initialize( new TableObjectViewer.Settings()
                .dataSourceUuid( settings.dataSourceUuid() )
                .schemaName( schemaName )
                .tableName( objectName ) );
        objectViewerBreadcrumbItem.setName( objectName );
        clearBreadcrumbs();
        if ( schemaExplorer.hasItems() ) {
            addBreadcrumbs( dataSourceBreadcrumbItem,
                    schemasBreadcrumbItem, objectsBreadcrumbItem, objectViewerBreadcrumbItem );
        } else {
            addBreadcrumbs( dataSourceBreadcrumbItem, objectsBreadcrumbItem, objectViewerBreadcrumbItem );
        }
        activateLastBreadcrum( true );
    }

    private void onDataSourceBreacrumbItemSelected( ) {
        if ( handler != null ) {
            handler.onDataSourceSelected();
        }
    }

    private void onShemasBreadcrumbItemSelected( ) {
        view.clearContent( );
        view.setContent( schemaExplorer );

        clearBreadcrumbs( );
        addBreadcrumbs( dataSourceBreadcrumbItem, schemasBreadcrumbItem );
        activateLastBreadcrum( true );
    }

    private void onObjectsBreadcrumbItemSelected( ) {
        view.clearContent();
        view.setContent( objectExplorer );

        clearBreadcrumbs();
        if ( schemaExplorer.hasItems() ) {
            addBreadcrumbs( dataSourceBreadcrumbItem, schemasBreadcrumbItem, objectsBreadcrumbItem );
        } else {
            addBreadcrumbs( dataSourceBreadcrumbItem, objectsBreadcrumbItem );
        }
        activateLastBreadcrum( true );
    }

    private void objectViewerBreadcrumbItemSelected( ) {
        Window.alert( "objectViewerBreadcrumbItemSelected" );
    }

    private void clearBreadcrumbs() {
        view.clearBreadcrumbs();
        currentBreadcrumbs.clear();
        dataSourceBreadcrumbItem.setActive( false );
        schemasBreadcrumbItem.setActive( false );
        objectsBreadcrumbItem.setActive( false );
        objectViewerBreadcrumbItem.setActive( false );
    }

    private void addBreadcrumbs( BreadcrumbItem ... items ) {
        for ( BreadcrumbItem item : items ) {
            view.addBreadcrumbItem( item );
            currentBreadcrumbs.add( item );
        }
    }

    private void activateLastBreadcrum( boolean active ) {
        if ( currentBreadcrumbs.size() > 1 ) {
            currentBreadcrumbs.get( currentBreadcrumbs.size() -1 ).setActive( active );
        }
    }

    private BreadcrumbItem createItem( String name, Command command ) {
        BreadcrumbItem item = itemInstance.get();
        item.setName( name );
        item.setCommand( command );
        return item;
    }

    private String buildDisplayableName( String dataSourceName ) {
        return "<<" + dataSourceName;
    }

    public static class Settings {

        /**
         * Configures the data source name that will be displayed as label for the initial navigation breadcrumb.
         */
        private String dataSourceName;

        /**
         * Configures the data source that will be explored.
         */
        private String dataSourceUuid;

        public Settings( ) {
        }

        public String dataSourceName( ) {
            return dataSourceName;
        }

        public Settings dataSourceName( String dataSourceName ) {
            this.dataSourceName = dataSourceName;
            return this;
        }

        public String dataSourceUuid( ) {
            return dataSourceUuid;
        }

        public Settings dataSourceUuid( String selectedDataSourceUuid ) {
            this.dataSourceUuid = selectedDataSourceUuid;
            return this;
        }
    }
}