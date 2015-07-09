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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationwizard;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditorView;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.AnnotationDefinition;
import org.kie.workbench.common.services.datamodeller.core.AnnotationValuePairDefinition;
import org.kie.workbench.common.services.datamodeller.core.ElementType;
import org.kie.workbench.common.services.datamodeller.core.impl.AnnotationImpl;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationDefinitionRequest;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationDefinitionResponse;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationParseRequest;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationParseResponse;
import org.kie.workbench.common.services.datamodeller.driver.model.DriverError;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.core.client.wizards.AbstractWizard;
import org.uberfire.ext.widgets.core.client.wizards.WizardPage;

@Dependent
public class CreateAnnotationWizard extends AbstractWizard {

    private List<WizardPage> pages = new ArrayList<WizardPage>();

    private Callback<Annotation> onCloseCallback;

    private KieProject currentProject;

    private AnnotationDefinition currentAnnotationDefinition = null;

    private Annotation currentAnnotation = null;

    private  ElementType currentTarget = ElementType.FIELD;

    @Inject
    private SearchAnnotationPage searchAnnotationPage;

    @Inject
    private SummaryPage summaryPage;

    @Inject
    private SyncBeanManager iocManager;

    @Inject
    private Caller<DataModelerService> modelerService;

    List<ValuePairEditorPage> currentEditorPages = new ArrayList<ValuePairEditorPage>(  );

    public CreateAnnotationWizard() {
    }

    @PostConstruct
    private void init() {
        pages.add( searchAnnotationPage );
        pages.add( summaryPage );
        searchAnnotationPage.addSearchAnnotationHandler( new SearchAnnotationPageView.SearchAnnotationHandler() {
            @Override
            public void onSearchClass( String className ) {
                doOnSearchClass( className );
            }

            @Override
            public void onSearchClassChanged() {
                doOnSearchClassChanged();
            }
        } );
    }

    @Override
    public List<WizardPage> getPages() {
        return pages;
    }

    @Override
    public Widget getPageWidget( int pageNumber ) {
        return pages.get( pageNumber ).asWidget();
    }

    @Override
    public String getTitle() {
        return "CreateAnnotationWizard";
    }

    @Override
    public int getPreferredHeight() {
        return 300;
    }

    @Override
    public int getPreferredWidth() {
        return 450;
    }

    @Override
    public void isComplete( final Callback<Boolean> callback ) {
        callback.callback( true );

        //only when all pages are complete we can say the wizard is complete.
        for ( WizardPage page : this.pages ) {
            page.isComplete( new Callback<Boolean>() {
                @Override
                public void callback( final Boolean result ) {
                    if ( Boolean.FALSE.equals( result ) ) {
                        callback.callback( false );
                    }
                }
            } );
        }
    }

    public void onCloseCallback( final Callback<Annotation> callback ) {
        this.onCloseCallback = callback;
    }

    @Override
    public void complete() {
        super.complete();
        clearCurrentValuePairEditorPages();
        doComplete();
    }

    @Override
    public void close() {
        super.close();
        clearCurrentValuePairEditorPages();
        invokeOnCloseCallback();
    }

    public void setCurrentProject( KieProject currentProject ) {
        this.currentProject = currentProject;
    }

    public void setCurrentTarget( ElementType currentTarget ) {
        this.currentTarget = currentTarget;
    }

    private void doComplete() {
        invokeOnCloseCallback();
    }

    private void doOnSearchClassChanged() {
        searchAnnotationPage.setHelpMessage( "Annotation definition is not loaded." );
        currentAnnotationDefinition = null;
        clearCurrentValuePairEditorPages();
    }

    private void doOnSearchClass( String className ) {
        AnnotationDefinitionRequest definitionRequest = new AnnotationDefinitionRequest( className );
        modelerService.call( getOnSearchClassSuccessCallback( definitionRequest) ).resolveDefinitionRequest( definitionRequest, currentProject );
    }

    private RemoteCallback<AnnotationDefinitionResponse> getOnSearchClassSuccessCallback( final AnnotationDefinitionRequest definitionRequest ) {
        return new RemoteCallback<AnnotationDefinitionResponse>() {
            @Override
            public void callback( AnnotationDefinitionResponse definitionResponse ) {
                processAnnotationDefinitionRequest( definitionRequest, definitionResponse );
            }
        };
    }

    private void processAnnotationDefinitionRequest( AnnotationDefinitionRequest definitionRequest,
            AnnotationDefinitionResponse definitionResponse ) {

        this.currentAnnotationDefinition = definitionResponse.getAnnotationDefinition();
        if ( definitionResponse.hasErrors() || definitionResponse.getAnnotationDefinition() == null ) {
            //TODO improve this, use a details section to provide more info.
            String message = "Class name " + definitionRequest.getClassName() + " was not found. \n It was not possible to load annotation definition ";
            message += "\n" + buildErrorList( definitionResponse.getErrors() );
            searchAnnotationPage.setHelpMessage( message );
            updateValuePairPages( null );
            this.currentAnnotation = null;
        } else {
            this.currentAnnotation = new AnnotationImpl( currentAnnotationDefinition );
            updateValuePairPages( definitionResponse.getAnnotationDefinition() );
        }
    }

    private void updateValuePairPages( AnnotationDefinition annotationDefinition ) {
        pages.clear();
        clearCurrentValuePairEditorPages();
        pages.add( searchAnnotationPage );

        if ( annotationDefinition != null ) {
            for ( AnnotationValuePairDefinition valuePairDefinition : annotationDefinition.getValuePairs() ) {
                pages.add( createValuePairEditorPage( valuePairDefinition ) );
            }
        }

        pages.add( summaryPage );
        super.start();

        if ( annotationDefinition != null ) {
            searchAnnotationPage.setStatus( CreateAnnotationWizardPage.PageStatus.VALIDATED );
            summaryPage.setStatus( CreateAnnotationWizardPage.PageStatus.VALIDATED );
        } else {
            searchAnnotationPage.setStatus( CreateAnnotationWizardPage.PageStatus.NOT_VALIDATED );
            summaryPage.setStatus( CreateAnnotationWizardPage.PageStatus.NOT_VALIDATED );
        }

    }

    private ValuePairEditorPage createValuePairEditorPage( AnnotationValuePairDefinition valuePairDefinition ) {

        final ValuePairEditorPage valuePairEditorPage = iocManager.lookupBean( ValuePairEditorPage.class ).getInstance();
        currentEditorPages.add( valuePairEditorPage );

        valuePairEditorPage.setValuePairDefinition( valuePairDefinition );
        String required = !valuePairDefinition.hasDefaultValue() ? "* " : "";
        valuePairEditorPage.setTitle( "  -> " + required + valuePairDefinition.getName() );
        valuePairEditorPage.setHelpMessage( "Enter the value for the annotation value pair and press the validate button" );
        valuePairEditorPage.setName( valuePairDefinition.getName() );
        valuePairEditorPage.addEditorHandler( new ValuePairEditorView.ValuePairEditorHandler() {
            @Override
            public void onValidate() {
                onPageValidate( valuePairEditorPage );
            }

            @Override
            public void onValueChanged( String currentValue ) {
                onPageValueChanged( valuePairEditorPage );
            }
        } );
        return valuePairEditorPage;
    }

    private void onPageValueChanged( ValuePairEditorPage valuePairEditorPage ) {
        valuePairEditorPage.setHelpMessage( "Value is not validated" );
        if ( currentAnnotation != null ) {
            currentAnnotation.removeValue( valuePairEditorPage.getName() );
        }
    }

    private void onPageValidate( ValuePairEditorPage valuePairEditorPage ) {

        modelerService.call( getValuePairValidateSuccessCallback( valuePairEditorPage  ), new CreateAnnotationWizardErrorCallback() )
                .resolveParseRequest( new AnnotationParseRequest( currentAnnotationDefinition.getClassName(), currentTarget,
                        valuePairEditorPage.getName(), valuePairEditorPage.getValue() ), currentProject );

    }

    private RemoteCallback<AnnotationParseResponse> getValuePairValidateSuccessCallback(
            final ValuePairEditorPage valuePairEditorPage ) {
        return new RemoteCallback<AnnotationParseResponse>() {

            @Override
            public void callback( AnnotationParseResponse annotationParseResponse ) {
                CreateAnnotationWizardPage.PageStatus status = CreateAnnotationWizardPage.PageStatus.NOT_VALIDATED;

                if ( !annotationParseResponse.hasErrors() && annotationParseResponse.getAnnotation() != null ) {
                    Object newValue = annotationParseResponse.getAnnotation().getValue( valuePairEditorPage.getName() );
                    currentAnnotation.setValue( valuePairEditorPage.getName(), newValue );
                    status = CreateAnnotationWizardPage.PageStatus.VALIDATED;
                    valuePairEditorPage.setHelpMessage( "Value pair was validated!" );

                } else {
                    currentAnnotation.removeValue( valuePairEditorPage.getName() );
                    status = CreateAnnotationWizardPage.PageStatus.NOT_VALIDATED;

                    //TODO improve this error handling
                    String errorMessage = "Value pair is not validated\n" +
                            buildErrorList( annotationParseResponse.getErrors() );

                    valuePairEditorPage.setHelpMessage( errorMessage );
                }

                valuePairEditorPage.setStatus( status );
            }
        };
    }

    private void clearCurrentValuePairEditorPages() {
        int pageCount = currentEditorPages.size();

        for ( int i = 0; i < pageCount; i++ ) {
            ValuePairEditorPage valuePairEditorPage = currentEditorPages.remove( 0 );
            iocManager.destroyBean( valuePairEditorPage );
        }
    }

    private void invokeOnCloseCallback() {
        if ( onCloseCallback != null ) {
            onCloseCallback.callback( currentAnnotation );
        }
    }

    class CreateAnnotationWizardErrorCallback implements ErrorCallback<Message> {

        public CreateAnnotationWizardErrorCallback( ) {
        }

        @Override
        public boolean error( Message message, Throwable throwable ) {
            //TODO improve this exception showing
            Window.alert( "Unexpected error encountered : " + throwable.getMessage() );
            return false;
        }
    }

    private String buildErrorList( List<DriverError> errors ) {
        //TODO improve this error showing
        String message = "";
        for ( DriverError error : errors ) {
            message += error.getMessage();
        }
        return message;
    }

}
