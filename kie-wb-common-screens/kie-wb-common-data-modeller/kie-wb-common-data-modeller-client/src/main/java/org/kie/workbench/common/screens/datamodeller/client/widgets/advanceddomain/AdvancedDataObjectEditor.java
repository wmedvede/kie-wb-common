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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain;

import javax.inject.Inject;

import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.ObjectEditor;
import org.kie.workbench.common.services.datamodeller.core.DataObject;

public class AdvancedDataObjectEditor
        extends ObjectEditor
        implements AdvancedDataObjectEditorView.Presenter {

    private AdvancedDataObjectEditorView view;

    @Inject
    public AdvancedDataObjectEditor( AdvancedDataObjectEditorView view ) {
        this.view = view;
        view.setPresenter( this );
        initWidget( view.asWidget() );
    }

    @Override
    public String getName() {
        return "ADVANCED_OBJECT_EDITOR";
    }

    @Override
    public String getDomainName() {
        return AdvancedDomainEditor.ADVANCED_DOMAIN;
    }

    protected void loadDataObject( DataObject dataObject ) {
        clean();
        setReadonly( true );
        this.dataObject = dataObject;

        view.loadAnnotations( dataObject.getAnnotations() );
    }

    @Override
    public void clean() {

    }
}