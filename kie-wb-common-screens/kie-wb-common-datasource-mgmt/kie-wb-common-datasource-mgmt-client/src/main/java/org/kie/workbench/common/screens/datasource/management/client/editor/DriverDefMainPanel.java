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

package org.kie.workbench.common.screens.datasource.management.client.editor;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.uberfire.backend.vfs.Path;
import org.uberfire.mvp.Command;

@Dependent
public class DriverDefMainPanel
        implements DriverDefMainPanelView.Presenter, IsWidget {

    private DriverDefMainPanelView view;

    private Path path;

    private DriverDefMainPanelView.Handler handler;

    @Inject
    public DriverDefMainPanel( DriverDefMainPanelView view ) {
        this.view = view;
        view.init( this );
    }

    @Override
    public void onNameChange() {
        if ( handler != null ) {
            handler.onNameChange();
        }
    }

    @Override
    public void onDriverClassChange() {
        if ( handler != null ) {
            handler.onDriverClassChange();
        }
    }

    public void setHandler( DriverDefMainPanelView.Handler handler ) {
        this.handler = handler;
    }

    public void upload( final Command successCallback, final Command errorCallback ) {
        view.upload( successCallback, errorCallback );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void setPath( Path path ) {
        view.setPath( path );
    }

    public void setFileName( String fileName ) {
        view.setFileName( fileName );
    }


    public void setName( String name ) {
        view.setName( name );
    }

    public String getName() {
        return view.getName();
    }

    public void setDriverClass( String driverClass ) {
        view.setDriverClass( driverClass );
    }

    public String getDriverClass() {
        return view.getDriverClass();
    }

}
