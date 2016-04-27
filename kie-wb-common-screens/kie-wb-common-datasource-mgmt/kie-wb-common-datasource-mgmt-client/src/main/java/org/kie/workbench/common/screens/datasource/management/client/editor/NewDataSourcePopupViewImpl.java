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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;

@Dependent
public class NewDataSourcePopupViewImpl
        extends BaseModal
        implements NewDataSourcePopupPresenter.NewDataSourcePopupView {

    interface NewDataSourcePopupViewImplBinder
            extends
            UiBinder<Widget, NewDataSourcePopupViewImpl> {

    }

    private static NewDataSourcePopupViewImplBinder uiBinder = GWT.create( NewDataSourcePopupViewImplBinder.class );

    @UiField
    TextBox name;

    @UiField
    HelpBlock nameHelpInline;

    @UiField
    FormGroup nameGroup;

    NewDataSourcePopupPresenter presenter;

    public NewDataSourcePopupViewImpl( ) {

        setTitle( "New platform data source" );

        add( new ModalBody() {{
            add( uiBinder.createAndBindUi( NewDataSourcePopupViewImpl.this ) );
        }} );

        final ModalFooterOKCancelButtons footer = new ModalFooterOKCancelButtons( new Command() {
            @Override
            public void execute() {
                presenter.onOk();
            }
        }, new Command() {
            @Override
            public void execute() {
                presenter.onCancel();
            }
        } );
        add( footer );
    }

    public void init( NewDataSourcePopupPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public String getName() {
        return name.getText();
    }

    @Override
    public void setName( String name ) {
        this.name.setText( name );
    }
}