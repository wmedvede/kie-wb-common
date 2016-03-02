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

import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.ext.editor.commons.client.BaseEditorViewImpl;

@Dependent
@Templated
public class DataSourceDefEditorViewImpl
        extends BaseEditorViewImpl
        implements  DataSourceDefEditorView {

    @Inject
    @DataField
    Label label;

    private Presenter presenter;

    public DataSourceDefEditorViewImpl() {
    }

    @PostConstruct
    private void init() {
        //UI initializations
        label.setText( "DataSourceEditorView !" );
    }

    @Override
    public void init( Presenter presenter ) {
        this.presenter = presenter;
    }
}