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

package org.kie.workbench.common.screens.datasource.management.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated
public class DataSourceDefItemViewImpl
        extends Composite
        implements DataSourceDefItemView {

    @Inject
    @DataField( "item" )
    LinkedGroupItem item;

    Presenter presenter;

    public DataSourceDefItemViewImpl() {
        GWT.log( "creando DataSourceDefItemViewImpl" );
    }

    @PostConstruct
    private void init() {
        GWT.log( "init() DataSourceDefItemViewImpl" );


        //TODO: ask Chistian why this alternative doesn't work.
        item.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent clickEvent ) {
                //GWT.log( "estoy en el click" );
                Window.alert( "ahora si?" );
                presenter.onClick();
            }
        } );
        GWT.log( "end of init() in DataSourceDefItemViewImpl" );
    }

    @Override
    public void setName( String name ) {
        item.setText( name );
    }

    @Override
    public String getName() {
        return item.getText();
    }

    @Override
    public void init( DataSourceDefItemView.Presenter presenter ) {
        this.presenter = presenter;
    }

    //@EventHandler( "item" )
    //@SinkNative( Event.ONCLICK )
    public void onItemClick( ClickEvent event ) {
        GWT.log( "item clicked" );
        presenter.onClick();
    }

}
