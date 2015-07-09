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

import java.util.Map;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditorPopup;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditorPopupView;
import org.kie.workbench.common.screens.datamodeller.client.widgets.common.domain.FieldEditor;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.AnnotationDefinition;
import org.kie.workbench.common.services.datamodeller.core.AnnotationValuePairDefinition;
import org.kie.workbench.common.services.datamodeller.core.DataObject;
import org.kie.workbench.common.services.datamodeller.core.ElementType;
import org.kie.workbench.common.services.datamodeller.core.ObjectProperty;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationParseRequest;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationParseResponse;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationSource;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationSourceRequest;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationSourceResponse;
import org.kie.workbench.common.services.datamodeller.driver.model.DriverError;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.mvp.Command;

public class AdvancedDataObjectFieldEditor
        extends FieldEditor
        implements AdvancedDataObjectFieldEditorView.Presenter {

    private AdvancedDataObjectFieldEditorView view;

    private ValuePairEditorPopup valuePairEditor;

    @Inject
    private Caller<DataModelerService> modelerService;

    private
    Map<String, AnnotationSource> annotationSources;

    @Inject
    public AdvancedDataObjectFieldEditor( AdvancedDataObjectFieldEditorView view,
            final ValuePairEditorPopup valuePairEditor ) {
        this.view = view;
        this.valuePairEditor = valuePairEditor;
        this.valuePairEditor.addPopupHandler( new ValuePairEditorPopupView.ValuePairEditorPopupHandler() {

            @Override
            public void onOk() {
                doValuePairChange( valuePairEditor.getAnnotationClassName(),
                        valuePairEditor.getName(), valuePairEditor.getValue() );
            }

            @Override
            public void onCancel() {
                valuePairEditor.hide();
            }

            @Override
            public void onClose() {
                valuePairEditor.hide();
            }
        } );
        view.setPresenter( this );
        initWidget( view.asWidget() );
    }

    @Override
    public String getName() {
        return "ADVANCED_FIELD_EDITOR";
    }

    @Override
    public String getDomainName() {
        return AdvancedDomainEditor.ADVANCED_DOMAIN;
    }

    @Override
    protected void loadDataObjectField( DataObject dataObject, ObjectProperty objectField ) {
        clean();
        setReadonly( true );
        if ( dataObject != null && objectField != null ) {
            this.dataObject = dataObject;
            this.objectField = objectField;
            //TODO optimizations can be done to not load all sources every time the field is loaded, load on demand, etc.
            if ( objectField.getAnnotations() != null &&
                    objectField.getAnnotations().size() > 0 ) {
                AnnotationSourceRequest sourceRequest = new AnnotationSourceRequest();
                sourceRequest.withAnnotations( objectField.getAnnotations() );
                modelerService.call( getLoadAnnotationSourcesSuccessCallback(), new DefaultErrorCallback() )
                        .resolveSourceRequest( sourceRequest );
            } else {
                view.loadAnnotations( objectField.getAnnotations() );
            }
        }
    }

    private RemoteCallback<AnnotationSourceResponse> getLoadAnnotationSourcesSuccessCallback() {

        return new RemoteCallback<AnnotationSourceResponse>() {
            @Override
            public void callback( AnnotationSourceResponse annotationSourceResponse ) {
                annotationSources = annotationSourceResponse.getAnnotationSources();
                view.loadAnnotations( objectField.getAnnotations(), annotationSourceResponse.getAnnotationSources() );
            }
        };
    }

    @Override
    public void onDeleteAnnotation( final Annotation annotation ) {

        String message = "Are you sure that you want to remove annotation: @" +
                annotation.getClassName() + " from field: " + objectField.getName();
        view.showYesNoDialog( message,
                new Command() {
                    @Override
                    public void execute() {
                        doDeleteAnnotation( annotation );
                    }
                },
                new Command() {
                    @Override
                    public void execute() {
                        //do nothing
                    }
                },
                new Command() {
                    @Override
                    public void execute() {
                        //do nothing.
                    }
                }
        );
    }

    private void doDeleteAnnotation( Annotation annotation ) {
        commandBuilder.buildFieldAnnotationRemoveCommand( getContext(),
                getName(),
                getDataObject(),
                getObjectField(),
                annotation.getClassName() ).execute();
        view.removeAnnotation( annotation );
    }

    @Override
    public void onEditValuePair( Annotation annotation, String valuePair ) {
        valuePairEditor.clear();
        AnnotationSource annotationSource = annotationSources.get( annotation.getClassName() );
        valuePairEditor.setValue( annotationSource != null ? annotationSource.getValuePairSource( valuePair ) : null );
        valuePairEditor.setName( valuePair );
        valuePairEditor.setAnnotationClassName( annotation.getClassName() );
        valuePairEditor.show();
    }

    private void doValuePairChange( String annotationClassName, String valuePairName, String text ) {

        modelerService.call( getValuePairChangeSuccessCallback( annotationClassName, valuePairName, text ), new DefaultErrorCallback() )
                .resolveParseRequest( new AnnotationParseRequest( annotationClassName, ElementType.FIELD, valuePairName,
                        text ), getContext().getCurrentProject() );
    }

    private RemoteCallback<AnnotationParseResponse> getValuePairChangeSuccessCallback( final String annotationClassName,
                                                                                        final String valuePairName,
                                                                                        final String value ) {
        return new RemoteCallback<AnnotationParseResponse>() {

            @Override public void callback( AnnotationParseResponse annotationParseResponse ) {
                if ( !annotationParseResponse.hasErrors() && annotationParseResponse.getAnnotation() != null ) {
                    Object newValue = annotationParseResponse.getAnnotation().getValue( valuePairName );

                    commandBuilder.buildFieldAnnotationValueChangeCommand( getContext(),
                            getName(), getDataObject(), getObjectField(), annotationClassName, valuePairName, newValue, false ).execute();

                    refreshAnnotationValues( getObjectField().getAnnotation( annotationClassName ) );
                    valuePairEditor.hide();
                    valuePairEditor.clear();

                } else {

                    //TODO improve this error handling
                    String errorMessage = "";
                    for ( DriverError error : annotationParseResponse.getErrors() ) {
                        errorMessage = errorMessage + "\n" + error.getMessage();
                    }
                    valuePairEditor.setErrorMessage( errorMessage );
                }
            }
        };
    }

    private void refreshAnnotationValues( Annotation annotation ) {
        //TODO implement the refresh for the single annotation. Currently refreshing all the field.
        loadDataObjectField( dataObject, objectField );
    }

    @Override
    public void onClearValuePair( Annotation annotation, String valuePair ) {
        AnnotationDefinition annotationDefinition = annotation.getAnnotationDefinition();
        AnnotationValuePairDefinition valuePairDefinition = annotationDefinition.getValuePair( valuePair );
        if ( valuePairDefinition.getDefaultValue() == null ) {
            //if the value pair has no default value, it should be applied wherever the annotation is applied, if not
            //the resulting code won't compile.
            String message = "Value pair: \"" + valuePair + "\" has no default value on @" + annotation.getClassName() + " annotation specification.\n" +
                    "So it should have a value whenever the annotation is applied, if not the resulting code will not be valid.";
            view.showYesNoDialog( message, null, null, new Command() {
                @Override
                public void execute() {
                    //do nothing
                }
            } );
        } else {
            commandBuilder.buildFieldAnnotationValueChangeCommand( getContext(), getName(),
                    getDataObject(), getObjectField(), annotation.getClassName(), valuePair, null, false ).execute();
            refreshAnnotationValues( annotation );
        }
    }

    @Override
    public void onAddAnnotation() {
        view.invokeCreateAnnotationWizard( new Callback<Annotation>() {
            @Override
            public void callback( Annotation annotation ) {
                //TODO review this, we don't want duplicated annotations
                doAddAnnotation( annotation );
            }
        }, getContext().getCurrentProject() );
    }

    private void doAddAnnotation( Annotation annotation ) {
        commandBuilder.buildFieldAnnotationAddCommand( getContext(), getName(), getDataObject(),
                getObjectField(), annotation ).execute();

    }

    public void clean() {
        view.clean();
    }
}