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

import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.screens.datasource.management.client.editor.DataSourceDefItem;

@Dependent
@Templated
public class DataSourceDefExplorerViewImpl
        extends Composite
        implements DataSourceDefExplorerView {

    @Inject
    @DataField
    private com.google.gwt.user.client.ui.Label emptyLabel;

    @Inject
    @DataField
    private LinkedGroup itemsGroup;

    private Presenter presenter;

    public DataSourceDefExplorerViewImpl() {
    }

    @PostConstruct
    private void init() {
        //set i18n or whatever any other ui initialization here.
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void addItem( DataSourceDefItem item ) {
        itemsGroup.add( item );
    }

    @Override
    public void removeItem( DataSourceDefItem item ) {
        itemsGroup.remove( item );
    }

    @Override
    public void clear() {
        itemsGroup.clear();
    }
}