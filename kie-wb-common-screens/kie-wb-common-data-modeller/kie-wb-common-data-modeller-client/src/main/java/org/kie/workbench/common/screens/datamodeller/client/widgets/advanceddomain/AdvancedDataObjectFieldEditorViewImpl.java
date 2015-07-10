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
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationlisteditor.AdvancedAnnotationListEditor;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationlisteditor.AdvancedAnnotationListEditorView;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationwizard.CreateAnnotationWizard;
import org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.valuepaireditor.ValuePairEditor;
import org.kie.workbench.common.services.datamodeller.core.Annotation;
import org.kie.workbench.common.services.datamodeller.core.ElementType;
import org.kie.workbench.common.services.datamodeller.driver.model.AnnotationSource;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.resources.i18n.CommonConstants;
import org.uberfire.mvp.Command;

public class AdvancedDataObjectFieldEditorViewImpl
        extends Composite
        implements AdvancedDataObjectFieldEditorView {

    interface AdvancedDataObjectFieldEditorViewImplUiBinder
            extends UiBinder<Widget, AdvancedDataObjectFieldEditorViewImpl> {

    }

    private static AdvancedDataObjectFieldEditorViewImplUiBinder uiBinder = GWT.create( AdvancedDataObjectFieldEditorViewImplUiBinder.class );

    @UiField
    SimplePanel annotationEditorPanel;

    @UiField
    Button addAnnotationButton;

    @Inject
    AdvancedAnnotationListEditor annotationListEditor;

    @Inject
    ValuePairEditor valuePairEditor;

    @Inject
    private SyncBeanManager iocManager;

    private Presenter presenter;

    public AdvancedDataObjectFieldEditorViewImpl() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    @PostConstruct
    void init() {
        annotationEditorPanel.add( annotationListEditor );
        annotationListEditor.addDeleteAnnotationHandler( new AdvancedAnnotationListEditorView.DeleteAnnotationHandler() {
            @Override public void onDeleteAnnotation( Annotation annotation ) {
                presenter.onDeleteAnnotation( annotation );
            }
        } );
        annotationListEditor.addEditValuePairHandler( new AdvancedAnnotationListEditorView.EditValuePairHandler() {
            @Override public void onEditValuePair( Annotation annotation, String valuePair ) {
                presenter.onEditValuePair( annotation, valuePair );
            }
        } );
        annotationListEditor.addClearValuePairHandler( new AdvancedAnnotationListEditorView.ClearValuePairHandler() {
            @Override public void onClearValuePair( Annotation annotation, String valuePair ) {
                presenter.onClearValuePair( annotation, valuePair );
            }
        } );
        addAnnotationButton.setType( ButtonType.LINK );
    }

    @Override
    public void setPresenter( Presenter presenter ) {
        this.presenter = presenter;

    }

    @Override
    public void loadAnnotations( List<Annotation> annotations ) {
        annotationListEditor.loadAnnotations( annotations );
    }

    @Override
    public void loadAnnotations( List<Annotation> annotations, Map<String, AnnotationSource> annotationSources ) {
        annotationListEditor.loadAnnotations( annotations, annotationSources );
    }

    @Override
    public void removeAnnotation( Annotation annotation ) {
        annotationListEditor.removeAnnotation( annotation );
    }

    @Override
    public void showYesNoDialog( String message, Command yesCommand, Command noCommand, Command cancelCommand ) {

        YesNoCancelPopup yesNoCancelPopup = YesNoCancelPopup.newYesNoCancelPopup(
                CommonConstants.INSTANCE.Information(), message, yesCommand, noCommand, cancelCommand);

        yesNoCancelPopup.setCloseVisible( false );
        yesNoCancelPopup.show();
    }

    @Override
    public void invokeCreateAnnotationWizard( final Callback<Annotation> callback, KieProject kieProject ) {
        final CreateAnnotationWizard addAnnotationWizard = iocManager.lookupBean( CreateAnnotationWizard.class ).getInstance();
        //When the wizard is closed destroy it to avoid memory leak
        addAnnotationWizard.onCloseCallback( new Callback<Annotation>() {
            @Override public void callback( Annotation result ) {
                iocManager.destroyBean( addAnnotationWizard );
                callback.callback( result );
            }
        } );
        addAnnotationWizard.init( kieProject, ElementType.FIELD );
        addAnnotationWizard.start();
    }

    public void clean() {
        annotationListEditor.clean();
    }

    @UiHandler( "addAnnotationButton")
    void onAddAnnotation( ClickEvent event ) {
        presenter.onAddAnnotation();
    }

}

