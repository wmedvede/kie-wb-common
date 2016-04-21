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

import java.util.List;

import org.uberfire.client.mvp.UberView;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.editor.commons.client.BaseEditorView;

public interface DataSourceDefEditorPresenter {

    interface DataSourceDefEditorView
            extends
                UberView<DataSourceDefEditorPresenter>,
                BaseEditorView {

        void setName( final String text );

        String getName();

        void setJndi( final String jndi );

        String getJndi();

        String getConnectionURL();

        void setConnectionURL( final String connectionURL );

        String getUser();

        void setUser( final String user );

        String getPassword();

        void setPassword( final String password );

        void enableDeployButton( final boolean enabled );

        void enableUnDeployButton( final boolean enabled );

        void enableTestButton( final boolean enabled );

        void loadDriverOptions( final List<Pair<String,String>> driverOptions, final boolean addEmptyOption );

        String getDriver();

        void setDriver( String driver );
    }

    void onDeployDataSource();

    void onUnDeployDataSource();

    void onUnTestDataSource();

}