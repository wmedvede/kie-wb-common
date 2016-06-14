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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.widgets.core.client.editors.defaulteditor.DefaultEditorNewFileUpload;
import org.uberfire.mvp.Command;

@Dependent
@Templated
public class DriverDefMainPanelViewImpl
        extends Composite
        implements DriverDefMainPanelView {


    @DataField( "name-form-group" )
    Element nameFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "name" )
    TextBox nameTextBox;

    @DataField ( "driver-class-form-group" )
    Element driverClassFormGroup =  DOM.createDiv();

    @Inject
    @DataField ( "driver-class" )
    TextBox driverClassTextBox;

    @Inject
    @DataField( "file-upload" )
    DefaultEditorNewFileUpload fileUpload;

    private DriverDefMainPanelView.Presenter presenter;

    public DriverDefMainPanelViewImpl() {
    }

    @Override
    public void init( final DriverDefMainPanelView.Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setName( final String name ) {
        this.nameTextBox.setText( name );
    }

    @Override
    public String getName() {
        return nameTextBox.getText();
    }

    @Override
    public void setDriverClass( final String driverClass ) {
        this.driverClassTextBox.setText( driverClass );
    }

    @Override
    public String getDriverClass() {
        return driverClassTextBox.getText();
    }

    @Override
    public void setPath( final Path path ) {
        fileUpload.setFolderPath( path );
    }

    @Override
    public void setFileName( final String fileName ) {
        fileUpload.setFileName( fileName );
    }

    @Override
    public void upload( final Command successCallback, final Command errorCallback ) {
        fileUpload.upload( successCallback, errorCallback );
    }

    @EventHandler( "name" )
    public void onNameChange( final ChangeEvent event ) {
        presenter.onNameChange();
    }

    @EventHandler( "driver-class" )
    public void onDriverClassChange( final ChangeEvent event ) {
        presenter.onDriverClassChange();
    }

}
