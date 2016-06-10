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

package org.kie.workbench.common.screens.datasource.management.client.explorer.common;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class DataSourceDefExplorerViewImpl
        extends Composite
        implements DataSourceDefExplorerView {


    @DataField( "content-accordion" )
    private Element contentAccordion = DOM.createDiv();

    @Inject
    @DataField( "datasources-panel-link" )
    private Anchor dataSourcesPanelLink;

    @DataField ( "datasources-panel" )
    private Element dataSourcesPanel =  DOM.createDiv();

    @Inject
    @DataField( "datasources-list-group" )
    private LinkedGroup dataSourcesListGroup;

    @Inject
    @DataField( "add-new-datasource" )
    private Anchor newDataSourceLink;


    @Inject
    @DataField( "drivers-panel-link" )
    private Anchor driversPanelLink;

    @DataField( "drivers-panel" )
    private Element driversPanel = DOM.createDiv();

    @Inject
    @DataField( "drivers-list-group" )
    private LinkedGroup driversListGroup;

    @Inject
    @DataField( "add-new-driver" )
    private Anchor newDriverLink;

    private Presenter presenter;

    public DataSourceDefExplorerViewImpl() {
    }

    @PostConstruct
    private void init() {

        //recalculate the panel group needed ids in order to enable more than one instance to be instantiated
        // on the same page.
        String contentAccordionId = Document.get().createUniqueId();
        contentAccordion.setId( contentAccordionId );

        String dataSourcesPanelId = Document.get().createUniqueId();
        dataSourcesPanelLink.getElement().setAttribute( "data-parent", "#" + contentAccordionId );
        dataSourcesPanelLink.getElement().setAttribute( "data-target", "#" + dataSourcesPanelId );
        dataSourcesPanel.setId( dataSourcesPanelId );

        String driversPanelLinkId = Document.get().createUniqueId();
        driversPanelLink.getElement().setAttribute( "data-parent", "#" + contentAccordionId );
        driversPanelLink.getElement().setAttribute( "data-target", "#" + driversPanelLinkId );
        driversPanel.setId( driversPanelLinkId );

    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void addItem( DataSourceDefItem item ) {
        dataSourcesListGroup.add( item );
    }

    @Override
    public void removeItem( DataSourceDefItem item ) {
        dataSourcesListGroup.remove( item );
    }

    @Override
    public void clear() {
        dataSourcesListGroup.clear();
    }

    @EventHandler( "add-new-datasource" )
    private void onAddNewDataSource( ClickEvent event ) {
        presenter.onAddDataSource();
    }

    @EventHandler( "add-new-driver" )
    private void onAddNewDriver( ClickEvent event ) {
        presenter.onAddDriver();
    }

}