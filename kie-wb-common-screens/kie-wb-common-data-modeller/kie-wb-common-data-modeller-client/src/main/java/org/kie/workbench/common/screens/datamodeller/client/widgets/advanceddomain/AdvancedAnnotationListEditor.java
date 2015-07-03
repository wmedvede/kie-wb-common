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

import java.util.List;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.services.datamodeller.core.Annotation;

public class AdvancedAnnotationListEditor
    implements IsWidget,
                AdvancedAnnotationListEditorView.Presenter {

    private AdvancedAnnotationListEditorView view;

    private List<Annotation> annotations;

    @Inject
    public AdvancedAnnotationListEditor( AdvancedAnnotationListEditorView view ) {
        this.view = view;
        view.setPresenter( this );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    @Override
    public void onAnnotationDeleted( Annotation annotation ) {
        Window.alert( "onAnnotationDeleted" );
    }

    public void loadAnnotations( List<Annotation> annotations ) {
        this.annotations = annotations;
        view.loadAnnotations( annotations );
    }

    @Override
    public void onEditAnnotation( Annotation annotation ) {
        Window.alert( "onEditValuePair" );
    }
}
