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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnClose;
import org.uberfire.lifecycle.OnFocus;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

@ApplicationScoped
@WorkbenchScreen ( identifier = "JPADomainScreen")
public class JPADomainScreen {


    JPADomainScreenView view;

    public JPADomainScreen() {
    }

    @Inject
    public JPADomainScreen( JPADomainScreenView view ) {
        this.view = view;
    }

    @OnStartup
    public void onStartup() {
        Window.alert("JPADomainScreen.onStartup");
    }

    @OnOpen
    public void onOpen() {
        Window.alert("JPADomainScreen.onOpen");
    }

    @OnFocus
    public void onFocus() {
        Window.alert("JPADomainScreen.onFocus");
    }

    @OnClose
    public void onClose() {
        Window.alert("JPADomainScreen.onClose");
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "JPADomainScreen";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

    @DefaultPosition
    public Position getDefaultPosition() {
        return CompassPosition.EAST;
    }
}
