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
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.screens.datasource.management.client.resources.i18n.DataSourceManagementConstants;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;
import org.uberfire.ext.widgets.common.client.tables.PagedTable;

@Dependent
@Templated
public class DatabaseStructureExplorerViewImpl
        implements DatabaseStructureExplorerView, IsElement {

    private Presenter presenter;

    @Inject
    @DataField( "datasource-selector" )
    private ListBox dataSourceSelector;

    @Inject
    @DataField( "schema-selector" )
    private ListBox schemaSelector;

    @Inject
    @DataField( "object-type-selector" )
    private ListBox objectTypeSelector;

    @Inject
    @DataField( "search-term-textbox" )
    private TextBox searchTermTextBox;

    @Inject
    @DataField( "search-button" )
    private Button searchButton;

    @Inject
    @DataField( "results-panel" )
    private FlowPanel resultsPanel;

    private PagedTable< DatabaseObjectRow > dataGrid;

    @Inject
    private TranslationService translationService;

    public DatabaseStructureExplorerViewImpl( ) {
    }

    @PostConstruct
    private void init( ) {
        searchTermTextBox.setPlaceholder( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_namePatternHelp ) );
        dataGrid = new PagedTable<>( 20, new ProvidesKey< DatabaseObjectRow >( ) {
            @Override
            public Object getKey( DatabaseObjectRow item ) {
                return item.getName( );
            }
        } );
        initializeResultsTable( );
        resultsPanel.add( dataGrid );
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public String getDataSource( ) {
        return dataSourceSelector.getSelectedValue( );
    }

    @Override
    public String getSchema( ) {
        return schemaSelector.getSelectedValue( );
    }

    @Override
    public String getDatabaseObjectType( ) {
        return objectTypeSelector.getSelectedValue( );
    }

    @Override
    public String getSearchTerm( ) {
        return searchTermTextBox.getText( );
    }

    @Override
    public void loadDataSourceOptions( List< Pair< String, String > > options, String selectedOption ) {
        loadOptions( dataSourceSelector, options, selectedOption );
    }

    @Override
    public void loadDataSourceOptions( final List< Pair< String, String > > options ) {
        loadOptions( dataSourceSelector, options );
    }

    @Override
    public void loadSchemaOptions( List< Pair< String, String > > options ) {
        loadOptions( schemaSelector, options );
    }

    @Override
    public void loadDatabaseObjectTypeOptions( List< Pair< String, String > > options ) {
        loadOptions( objectTypeSelector, options );
    }

    @Override
    public void setDataProvider( AsyncDataProvider< DatabaseObjectRow > dataProvider ) {
        dataGrid.setDataProvider( dataProvider );
    }

    @Override
    public void redraw( ) {
        dataGrid.redraw( );
    }

    @Override
    public void enableDataSourceSelector( boolean enabled ) {
        dataSourceSelector.setEnabled( enabled );
    }

    @Override
    public void showBusyIndicator( String message ) {
        BusyPopup.showMessage( message );
    }

    @Override
    public void hideBusyIndicator( ) {
        BusyPopup.close();
    }

    @EventHandler( "datasource-selector" )
    private void onDataSourceChange( ChangeEvent event ) {
        presenter.onDataSourceChange( );
    }

    @EventHandler( "schema-selector" )
    private void onSchemaChangeHandler( ChangeEvent event ) {
        presenter.onSchemaChange( );
    }

    @EventHandler( "object-type-selector" )
    private void onObjectTypeChange( ChangeEvent event ) {
        presenter.onDatabaseObjectTypeChange( );
    }

    @EventHandler( "search-button" )
    private void onSearchClick( ClickEvent event ) {
        presenter.onSearch( );
    }

    private void initializeResultsTable( ) {
        dataGrid.setEmptyTableCaption( translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dbObjectsListEmpty ) );
        dataGrid.setToolBarVisible( false );
        addNameColumn( );
        addTypeColumn( );
        addOpenColumn( );
    }

    private void addNameColumn( ) {
        Column< DatabaseObjectRow, String > column = new Column< DatabaseObjectRow, String >( new TextCell( ) ) {
            @Override
            public String getValue( DatabaseObjectRow row ) {
                return row.getName( );
            }
        };
        dataGrid.addColumn( column, translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dbObjectNameColumn ) );
        dataGrid.setColumnWidth( column, 80, Style.Unit.PCT );
    }

    private void addTypeColumn( ) {
        Column< DatabaseObjectRow, String > column = new Column< DatabaseObjectRow, String >( new TextCell( ) ) {
            @Override
            public String getValue( DatabaseObjectRow row ) {
                return row.getType( );
            }
        };
        dataGrid.addColumn( column, translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dbObjectTypeColumn ) );
        dataGrid.setColumnWidth( column, 10, Style.Unit.PCT );
    }

    private void addOpenColumn( ) {
        Column< DatabaseObjectRow, String > column = new Column< DatabaseObjectRow, String >( new ButtonCell( ButtonType.DEFAULT, ButtonSize.SMALL ) ) {
            @Override
            public String getValue( DatabaseObjectRow row ) {
                return translationService.getTranslation(
                        DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dbObjectOpen );
            }
        };
        column.setFieldUpdater( new FieldUpdater< DatabaseObjectRow, String >( ) {
            @Override
            public void update( int index,
                                DatabaseObjectRow row,
                                String value ) {
                onOpen( row );
            }
        } );
        dataGrid.addColumn( column, translationService.getTranslation(
                DataSourceManagementConstants.DatabaseStructureExplorerViewImpl_dbObjectActionColumn ) );
        dataGrid.setColumnWidth( column, 10, Style.Unit.PCT );
    }

    private void onOpen( DatabaseObjectRow row ) {
        presenter.onOpen( row );
    }

    private static void loadOptions( ListBox listBox, List< Pair< String, String > > options ) {
        loadOptions( listBox, options, null );
    }

    private static void loadOptions( ListBox listBox, List< Pair< String, String > > options, String selectedOption ) {
        Pair< String, String > option;
        int selectedIndex = -1;
        listBox.clear( );
        for ( int i = 0; i < options.size( ); i++ ) {
            option = options.get( i );
            listBox.addItem( option.getK1( ), option.getK2( ) );
            if ( selectedIndex < 0 && selectedOption != null && selectedOption.equals( option.getK2( ) ) ) {
                selectedIndex = i;
            }
        }
        if ( selectedIndex >= 0 ) {
            listBox.setSelectedIndex( selectedIndex );
        }
    }
}