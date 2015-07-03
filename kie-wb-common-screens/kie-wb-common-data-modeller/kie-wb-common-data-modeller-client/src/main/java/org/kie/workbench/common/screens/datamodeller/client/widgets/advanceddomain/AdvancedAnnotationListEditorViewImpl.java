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

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Observes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datamodeller.client.model.DataModelerPropertyEditorFieldInfo;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.properties.AnnotationField;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.uberfire.ext.properties.editor.client.PropertyEditorWidget;
import org.uberfire.ext.properties.editor.model.PropertyEditorCategory;
import org.uberfire.ext.properties.editor.model.PropertyEditorChangeEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorEvent;
import org.uberfire.ext.properties.editor.model.PropertyEditorFieldInfo;

public class AdvancedAnnotationListEditorViewImpl
        extends Composite
        implements AdvancedAnnotationListEditorView {

    interface AdvancedAnnotationListEditorViewImplUiBinder
            extends
            UiBinder<Widget, AdvancedAnnotationListEditorViewImpl> {

    }

    private static AdvancedAnnotationListEditorViewImplUiBinder uiBinder = GWT.create( AdvancedAnnotationListEditorViewImplUiBinder.class );

    private static final String ANNOTATIONS_CATEGORY = "ANNOTATIONS_CATEGORY";

    private static final String ANNOTATIONS_EDITOR_EVENT = "ANNOTATIONS_EDITOR_EVENT";

    private Presenter presenter;

    @UiField
    PropertyEditorWidget propertyEditor;

    public AdvancedAnnotationListEditorViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );

        propertyEditor.setFilterPanelVisible( false );
        propertyEditor.setLastOpenAccordionGroupTitle( ANNOTATIONS_CATEGORY );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void loadAnnotations( List<Annotation> annotations ) {
        loadPropertyEditor( annotations );
    }

    private void loadPropertyEditor( List<Annotation> annotations ) {
        propertyEditor.handle( new PropertyEditorEvent( getCurrentEditorEventId(), getPropertyEditorCategories( annotations ) ) );
    }

    private List<PropertyEditorCategory> getPropertyEditorCategories( List<Annotation> annotations ) {

        final List<PropertyEditorCategory> categories = new ArrayList<PropertyEditorCategory>();

        PropertyEditorCategory category = new PropertyEditorCategory( ANNOTATIONS_CATEGORY, 1 );
        categories.add( category );

        if ( annotations != null ) {
            for ( Annotation annotation : annotations ) {
                category.withField( createAnnotationField( annotation ) );
            }
        }
        return categories;
    }

    private PropertyEditorFieldInfo createAnnotationField( Annotation annotation ) {
        return createField( annotation.getClassName(), annotation.getClassName(), annotation.getClassName(), AnnotationField.class, annotation );
    }

    private DataModelerPropertyEditorFieldInfo createField( String label, String key, String currentStringValue, Class<?> customFieldClass, Annotation annotation ) {
        DataModelerPropertyEditorFieldInfo fieldInfo = new DataModelerPropertyEditorFieldInfo( label, currentStringValue, customFieldClass );
        fieldInfo.withKey( key );
        fieldInfo.withRemovalSupported( true );
        fieldInfo.setCurrentValue( annotation );
        return fieldInfo;
    }

    private void onPropertyEditorChange( @Observes PropertyEditorChangeEvent event ) {
        PropertyEditorFieldInfo property = event.getProperty();

        if ( isFromCurrentEditor( property.getEventId() ) ) {

            PropertyEditorFieldInfo fieldInfo = ( PropertyEditorFieldInfo ) event.getProperty();
            presenter.onAnnotationDeleted( null );
        }
    }

    private String getCurrentEditorEventId() {
        //TODO, temporal mechanism to avoid two property editors opened in different workbench editors receiving crossed events
        return ANNOTATIONS_EDITOR_EVENT + "-" + this.hashCode();
    }

    private boolean isFromCurrentEditor( String propertyEditorEventId ) {
        return getCurrentEditorEventId().equals( propertyEditorEventId );
    }
}
