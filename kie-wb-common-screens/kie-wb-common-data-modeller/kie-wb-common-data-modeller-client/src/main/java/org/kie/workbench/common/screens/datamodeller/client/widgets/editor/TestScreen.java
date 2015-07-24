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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectSelectedEvent;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

@ApplicationScoped
@WorkbenchScreen ( identifier = "DataModelerTestScreen")
public class TestScreen {

    private TestScreenView view;

    public TestScreen() {
    }

    @Inject
    public TestScreen( TestScreenView view ) {
        this.view = view;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "DataModelerTestScreen";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

    @DefaultPosition
    public Position getDefaultPosition() {
        return CompassPosition.WEST;
    }

    protected void onDataObjectSelected( @Observes DataObjectSelectedEvent event ) {
        view.loadDataObject( event.getCurrentDataObject() );
    }
}
