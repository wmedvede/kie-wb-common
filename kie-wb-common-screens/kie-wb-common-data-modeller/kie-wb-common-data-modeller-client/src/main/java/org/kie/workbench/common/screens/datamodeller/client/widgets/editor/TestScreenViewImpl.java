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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.services.datamodeller.core.DataObject;

public class TestScreenViewImpl
        extends Composite
        implements TestScreenView {

    interface Binder
            extends UiBinder<Widget, TestScreenViewImpl> {

    };

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    DivWidget containerPanel;

    private DataObjectFieldBrowser fieldBrowser;

    public TestScreenViewImpl() {
    }

    @Inject
    public TestScreenViewImpl( DataObjectFieldBrowser fieldBrowser ) {
        initWidget( uiBinder.createAndBindUi( this ) );
        this.fieldBrowser = fieldBrowser;
    }

    @PostConstruct
    private void init() {
        containerPanel.add( fieldBrowser );
    }

    @Override
    public void loadDataObject( DataObject dataObject ) {
        fieldBrowser.loadDataObject( dataObject );
    }
}
