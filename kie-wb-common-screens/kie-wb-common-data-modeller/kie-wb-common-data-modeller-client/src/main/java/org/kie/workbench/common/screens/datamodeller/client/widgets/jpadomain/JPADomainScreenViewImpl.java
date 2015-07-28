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

package org.kie.workbench.common.screens.datamodeller.client.widgets.jpadomain;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class JPADomainScreenViewImpl
        extends Composite
        implements JPADomainScreenView {

    interface Binder
            extends UiBinder<Widget, JPADomainScreenViewImpl> {

    };

    private static Binder uiBinder = GWT.create( Binder.class );

    @UiField
    DivWidget containerPanel;

    private JPADomainEditor domainEditor;

    public JPADomainScreenViewImpl() {
    }

    @Inject
    public JPADomainScreenViewImpl( JPADomainEditor domainEditor ) {
        initWidget( uiBinder.createAndBindUi( this ) );
        //just a comment, if we use field injection for domainEditor we get an error because of the uberfire property editor.
        this.domainEditor = domainEditor;
    }

    @PostConstruct
    private void init() {
        containerPanel.add( domainEditor );
    }
}
