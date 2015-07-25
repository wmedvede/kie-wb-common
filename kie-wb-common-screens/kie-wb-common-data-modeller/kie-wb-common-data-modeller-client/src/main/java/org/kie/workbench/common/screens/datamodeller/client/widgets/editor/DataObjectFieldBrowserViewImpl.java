/*
 * Copyright 2015 JBoss Inc
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

package org.kie.workbench.common.screens.datamodeller.client.widgets.editor;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.mvp.Command;

public class DataObjectFieldBrowserViewImpl
        extends Composite
        implements DataObjectFieldBrowserView {

    interface Binder extends
            UiBinder<Widget, DataObjectFieldBrowserViewImpl> {

    }

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    Button addFieldButton;

    @UiField
    DivWidget containerPanel;

    @UiField
    FlexTable items;

    private List<FieldBrowserItem> browserItems = new ArrayList<FieldBrowserItem>(  );

    private Presenter presenter;

    private boolean readOnly = false;

    int itemIds = 0;

    public DataObjectFieldBrowserViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );
        addFieldButton.setType( ButtonType.LINK );
        addFieldButton.addClickHandler( new ClickHandler() {
            @Override public void onClick( ClickEvent event ) {
                presenter.onAddItem();
            }
        } );
        //containerPanel.add( items );
        //items.addStyleName( "field-browser-navigator" );
        //items.addStyleName( "table table-bordered table-striped table-hover" );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public FieldBrowserItem addItem() {

        final String itemId = nextItemId();
        FieldBrowserItemImpl item;

        int row = getRowCount();
        int col = 0;

        Label typeLabel = new Label( );
        items.setWidget( row, col, typeLabel );
        col++;

        Anchor nameAnchor = new Anchor( );
        items.setWidget( row, col, nameAnchor );
        nameAnchor.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                presenter.onSelectItem( itemId );
            }
        } );
        items.setWidget( row, col, nameAnchor );
        col++;

        final Command deleteCommand = new Command() {
            @Override
            public void execute() {
                presenter.onRemoveItem( itemId );
            }
        };

        final FlowPanel iconContainer = new FlowPanel();
        final InlineHTML deleteContainer = new InlineHTML( getDeleteIcon( readOnly ) );
        deleteContainer.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                deleteCommand.execute();
            }
        } );
        deleteContainer.getElement().getStyle().setPaddingLeft( 10, Style.Unit.PX );
        iconContainer.add( deleteContainer );
        items.setWidget( row, col, iconContainer );

        item = new FieldBrowserItemImpl( itemId, typeLabel, nameAnchor, iconContainer, deleteCommand );
        browserItems.add( item );

        items.getFlexCellFormatter().addStyleName( row, 0, "field-browser-navigator-leftTD" );
        items.getFlexCellFormatter().addStyleName( row, 1, "field-browser-navigator-internalTD" );
        items.getFlexCellFormatter().addStyleName( row, 2, "field-browser-navigator-rightTD" );


        return item;
    }

    @Override
    public FieldBrowserItem getItem( String itemId ) {
        int index = getItemRow( itemId );
        return index >= 0 ? browserItems.get( index ) : null;
    }

    @Override
    public void removeItem( String itemId ) {
        int index = getItemRow( itemId );
        if ( index >= 0 ) {
            browserItems.remove( index );
            items.removeRow( index );
        }
    }

    @Override
    public String getItemId( String fieldName ) {
        for ( FieldBrowserItem item : browserItems ) {
            if ( item.getFieldName().equals( fieldName ) ) {
                return item.getItemId();
            }
        }
        return null;
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public void setReadOnly( boolean readOnly ) {
        for ( FieldBrowserItem item : browserItems ) {
            item.setReadOnly( readOnly );
        }
    }

    private int getRowCount() {
        return items.getRowCount();
    }

    private static String getDeleteIcon( boolean disabled ) {
        String icon = "<i class=\"icon-trash\"></i>";
        return (disabled) ? ban( icon ) : icon;
    }

    private static String ban(String icon) {
        return "<span class=\"icon-stack\">" + icon +
                "<i class=\"icon-ban-circle icon-stack-base\"></i></span>";
    }

    private String nextItemId( ) {
        return "item_" + itemIds++;
    }

    private int getItemRow( String itemId ) {
        for ( int index = 0; index < browserItems.size(); index++ ) {
            if ( browserItems.get( index ).getItemId().equals( itemId ) ) {
                return index;
            }
        }
        return -1;
    }

    public static class FieldBrowserItemImpl
            implements FieldBrowserItem {

        String itemId;

        Label typeLabel;

        Anchor nameAnchor;

        FlowPanel iconContainer;

        String fieldName;

        String typeName;

        Command deleteCommand;

        public FieldBrowserItemImpl( String itemId,
                Label typeLabel,
                Anchor nameAnchor,
                FlowPanel iconContainer,
                Command deleteCommand ) {

            this.itemId = itemId;
            this.typeLabel = typeLabel;
            this.nameAnchor = nameAnchor;
            this.iconContainer = iconContainer;
            this.deleteCommand = deleteCommand;
        }

        @Override
        public void setFieldName( String fieldName ) {
            this.fieldName = fieldName;
            nameAnchor.setText( fieldName.replaceAll( " ", "\u00a0" ) );
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public void setTypeName( String typeName, boolean multiple ) {
            typeLabel.setText( typeName + ( multiple ? "[]" : "" ) );
        }

        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        public String getItemId() {
            return itemId;
        }

        @Override
        public void setReadOnly( final boolean readOnly ) {

            iconContainer.clear();
            final InlineHTML deleteContainer = new InlineHTML( getDeleteIcon( readOnly ) );
            if ( !readOnly ) {
                deleteContainer.addClickHandler( new ClickHandler() {
                    @Override
                    public void onClick( ClickEvent event ) {
                        deleteCommand.execute();
                    }
                } );
            }
            deleteContainer.getElement().getStyle().setPaddingLeft( 10, Style.Unit.PX );
        }

    }

}
