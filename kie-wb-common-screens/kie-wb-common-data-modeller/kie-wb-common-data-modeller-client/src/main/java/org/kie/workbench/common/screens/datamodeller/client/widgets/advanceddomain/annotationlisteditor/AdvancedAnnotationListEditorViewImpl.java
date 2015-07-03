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

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.util.CommandDrivenAccordionGroup;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.AnnotationValuePairDefinition;
import org.uberfire.mvp.Command;

public class AdvancedAnnotationListEditorViewImpl
        extends Composite
        implements AdvancedAnnotationListEditorView {

    interface AdvancedAnnotationListEditorViewImplUiBinder
            extends
            UiBinder<Widget, AdvancedAnnotationListEditorViewImpl> {

    }

    private static AdvancedAnnotationListEditorViewImplUiBinder uiBinder = GWT.create( AdvancedAnnotationListEditorViewImplUiBinder.class );

    @UiField
    FlowPanel containerPanel;

    private DivWidget accordionsContainer = new DivWidget( );

    private Presenter presenter;

    private Map<Annotation, CommandDrivenAccordionGroup> annotationAccordion = new HashMap<Annotation, CommandDrivenAccordionGroup>(  );

    public AdvancedAnnotationListEditorViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );
        containerPanel.add( accordionsContainer );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void loadAnnotations( List<Annotation> annotations ) {
        if ( annotations != null ) {
            for ( Annotation annotation : annotations ) {
                createAnnotationAccordionGroup( annotation );
            }
        }
    }

    @Override
    public void removeAnnotation( Annotation annotation ) {
        CommandDrivenAccordionGroup accordionGroup = annotationAccordion.get( annotation );
        if ( accordionGroup != null ) {
            accordionsContainer.remove( accordionGroup );
            annotationAccordion.remove( annotation );
        }
    }

    private void createAnnotationAccordionGroup( final Annotation annotation ) {

        CommandDrivenAccordionGroup accordionGroup = new CommandDrivenAccordionGroup( "Delete", new Command() {
            @Override public void execute() {
                presenter.onDeleteAnnotation( annotation );
            }
        } );
        annotationAccordion.put( annotation, accordionGroup );

        accordionGroup.setHeading( accordionHeading( annotation ));
        accordionsContainer.add( accordionGroup );

        if ( annotation.getAnnotationDefinition() != null &&
                annotation.getAnnotationDefinition().getValuePairs() != null ) {
            for ( AnnotationValuePairDefinition valuePairDefinition : annotation.getAnnotationDefinition().getValuePairs() ) {
                accordionGroup.add( createValuePairItem( annotation, valuePairDefinition ) );
            }
        }
    }

    private Widget createValuePairItem( final Annotation annotation, final AnnotationValuePairDefinition valuePairDefinition ) {
        FlowPanel valuePairRow = new FlowPanel( );
        valuePairRow.addStyleName( "row-fluid");
        valuePairRow.addStyleName( "control-group" );

        valuePairRow.add( new Label( valuePairDefinition.getName() + ":" ) );

        TextBox content = new TextBox();
        content.addStyleName( "span8" );
        content.addStyleName( "controls" );
        content.setText( getValuePairStringValue( annotation, valuePairDefinition ) );
        content.setReadOnly( true );
        content.setTitle( "This is the long content value for the value pair just in case it doesn't fit in the text field: "
                + getValuePairStringValue( annotation, valuePairDefinition ) );
        valuePairRow.add( content );

        Button editButton = new Button( "edit", new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                presenter.onEditValuePair( annotation, valuePairDefinition.getName() );
            }
        } );
        editButton.setType( ButtonType.LINK );
        valuePairRow.add( editButton );

        Button cleanButton = new Button( "clean", new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                presenter.onClearValuePair( annotation, valuePairDefinition.getName() );
            }
        } );
        cleanButton.setType( ButtonType.LINK );
        valuePairRow.add( cleanButton );


        return valuePairRow;
    }

    private String getValuePairStringValue( Annotation annotation, AnnotationValuePairDefinition valuePairDefinition ) {
        Object value = annotation.getValue( valuePairDefinition.getName() );
        String strValue = value != null ? value.toString() : "(value not set)";
        return strValue;
    }

    private String accordionHeading( Annotation annotation ) {
        return "@" + annotation.getClassName();
    }

    @Override
    public void clean() {
        accordionsContainer.clear();
    }
}
