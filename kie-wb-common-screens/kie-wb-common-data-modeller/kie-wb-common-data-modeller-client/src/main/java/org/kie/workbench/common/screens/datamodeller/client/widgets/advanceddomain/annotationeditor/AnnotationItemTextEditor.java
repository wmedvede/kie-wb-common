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

package org.kie.workbench.common.screens.datamodeller.client.widgets.advanceddomain.annotationeditor;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AnnotationItemTextEditor extends Composite {

    @UiField
    TextBox textBox;

    public AnnotationItemTextEditor() {
        initWidget( uiBinder.createAndBindUi( this ) );
        textBox.addFocusHandler( new FocusHandler() {
            @Override
            public void onFocus( FocusEvent event ) {
                textBox.selectAll();
            }
        } );
    }

    public void setText(String text){
        textBox.setText( text );
    }

    public String getText() {
        return textBox.getText();
    }

    public void addKeyDownHandler( KeyDownHandler keyDownHandler ) {
        textBox.addKeyDownHandler( keyDownHandler );
    }

    public void addBlurHandler( BlurHandler blurHandler ) {
        textBox.addBlurHandler( blurHandler );
    }

    interface MyUiBinder extends UiBinder<Widget, AnnotationItemTextEditor> {

    }

    private static MyUiBinder uiBinder = GWT.create( MyUiBinder.class );

}