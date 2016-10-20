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

package org.kie.workbench.common.screens.datasource.management.client.dbexplorer.common;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.ListItem;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class BreadcrumbItemViewImpl
        implements BreadcrumbItemView, IsElement {

    private Presenter presenter;

    private String name;

    @Inject
    @DataField( "breadcrumb-item" )
    private ListItem item;

    @Inject
    @DataField( "breadcrumb-item-anchor" )
    private Anchor itemAnchor;

    public BreadcrumbItemViewImpl( ) {
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setName( String name ) {
        this.name = name;
        adjustItemContent();
    }

    @Override
    public void setActive( boolean active ) {
        item.setClassName( active ? "active" : "" );
        itemAnchor.setClassName( "" );
        adjustItemContent();
    }

    private void adjustItemContent( ) {
        String currentClass = item.getClassName();
        boolean isActive = currentClass != null && currentClass.contains( "active" );
        DOMUtil.removeAllChildren( item );
        if ( isActive ) {
            item.setInnerHTML( "<strong>" + name + "</strong>" );
        } else {
            itemAnchor.setTextContent( name );
            item.appendChild( itemAnchor );
        }
    }

    @EventHandler( "breadcrumb-item-anchor" )
    private void onItemClick( ClickEvent event ) {
        presenter.onClick();
    }
}