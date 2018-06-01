/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.common.rendering.client.widgets.lsListBox;

import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.Label;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TextInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.views.pfly.widgets.ValidationState;

@Templated
public class LiveSearchEntryCreationEditorViewImpl implements LiveSearchEntryCreationEditor.View,
                                                              IsElement {

    @Inject
    @DataField
    private Div newEntryInputForm;

    @Inject
    @DataField
    private Label newEntryInputLabel;

    @Inject
    @DataField
    private TextInput newEntryInput;

    @Inject
    @DataField
    private Span newEntryInputHelpBlock;

    @Inject
    @DataField
    private Anchor acceptButton;

    @Inject
    @DataField
    private Anchor cancelButton;

    private LiveSearchEntryCreationEditor presenter;

    @Override
    public void init(LiveSearchEntryCreationEditor presenter) {
        this.presenter = presenter;
        newEntryInputLabel.setTextContent(presenter.getFieldLabel());
    }

    @Override
    public void clear() {
        newEntryInput.setValue("");
        clearErrors();
    }

    @Override
    public String getValue() {
        return newEntryInput.getValue();
    }

    @Override
    public void showError(String errorMessage) {
        DOMUtil.addCSSClass(newEntryInputForm,
                            ValidationState.ERROR.getCssName());
        newEntryInputHelpBlock.setTextContent(errorMessage);
    }

    @Override
    public void clearErrors() {
        DOMUtil.removeCSSClass(newEntryInputForm,
                               ValidationState.ERROR.getCssName());
        newEntryInputHelpBlock.setTextContent("");
    }

    @EventHandler("acceptButton")
    public void onAccept(@ForEvent("click") Event event) {
        presenter.onAccept();
        event.stopPropagation();
    }

    @EventHandler("cancelButton")
    public void onCancel(@ForEvent("click") Event event) {
        presenter.onCancel();
        event.stopPropagation();
    }
}
