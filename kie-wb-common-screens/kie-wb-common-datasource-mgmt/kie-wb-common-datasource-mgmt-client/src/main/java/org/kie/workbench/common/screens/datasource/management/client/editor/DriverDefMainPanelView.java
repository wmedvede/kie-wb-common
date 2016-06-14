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

import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;

public interface DriverDefMainPanelView extends UberView<DriverDefMainPanelView.Presenter> {

    interface Presenter extends Handler {

    }

    interface Handler {

        void onNameChange();

        void onDriverClassChange();

    }

    void setName( final String text );

    String getName();

    void setDriverClass( final String driverClass );

    String getDriverClass();

    void setPath( Path path );

    void setFileName( String fileName );

    void upload( final Command successCallback, final Command errorCallback );

}
