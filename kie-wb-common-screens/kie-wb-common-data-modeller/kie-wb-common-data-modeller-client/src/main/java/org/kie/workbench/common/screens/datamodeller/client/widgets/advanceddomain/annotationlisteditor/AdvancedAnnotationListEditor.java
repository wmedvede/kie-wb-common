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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationlisteditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationSource;

public class AdvancedAnnotationListEditor
    implements IsWidget,
                AdvancedAnnotationListEditorView.Presenter {

    private AdvancedAnnotationListEditorView view;

    private AdvancedAnnotationListEditorView.DeleteAnnotationHandler deleteAnnotationHandler;

    private AdvancedAnnotationListEditorView.EditValuePairHandler editValuePairHandler;

    private AdvancedAnnotationListEditorView.ClearValuePairHandler clearValuePairHandler;

    @Inject
    public AdvancedAnnotationListEditor( AdvancedAnnotationListEditorView view ) {
        this.view = view;
        view.setPresenter( this );
    }

    @Override
    public Widget asWidget() {
        return view.asWidget();
    }

    public void loadAnnotations( List<Annotation> annotations ) {
        view.loadAnnotations( annotations, new HashMap<String, AnnotationSource>(  ) );
    }

    public void loadAnnotations( List<Annotation> annotations, Map<String, AnnotationSource> annotationSources ) {
        view.loadAnnotations( annotations, annotationSources );
    }

    @Override
    public void onDeleteAnnotation( Annotation annotation ) {
        if ( deleteAnnotationHandler != null ) {
            deleteAnnotationHandler.onDeleteAnnotation( annotation );
        }
    }

    @Override
    public void onEditValuePair( Annotation annotation, String valuePair ) {
        if ( editValuePairHandler != null ) {
            editValuePairHandler.onEditValuePair( annotation, valuePair );
        }
    }

    @Override
    public void onClearValuePair( Annotation annotation, String valuePair ) {
        if ( clearValuePairHandler != null ) {
            clearValuePairHandler.onClearValuePair( annotation, valuePair );
        }
    }

    @Override
    public void addDeleteAnnotationHandler( AdvancedAnnotationListEditorView.DeleteAnnotationHandler deleteAnnotationHandler ) {
        this.deleteAnnotationHandler = deleteAnnotationHandler;
    }

    @Override
    public void addEditValuePairHandler( AdvancedAnnotationListEditorView.EditValuePairHandler editValuePairHandler ) {
        this.editValuePairHandler = editValuePairHandler;
    }

    @Override
    public void addClearValuePairHandler( AdvancedAnnotationListEditorView.ClearValuePairHandler clearValuePairHandler ) {
        this.clearValuePairHandler = clearValuePairHandler;
    }

    public void clean() {
        view.clean();
    }

    public void removeAnnotation( Annotation annotation ) {
        view.removeAnnotation( annotation );
    }
}
