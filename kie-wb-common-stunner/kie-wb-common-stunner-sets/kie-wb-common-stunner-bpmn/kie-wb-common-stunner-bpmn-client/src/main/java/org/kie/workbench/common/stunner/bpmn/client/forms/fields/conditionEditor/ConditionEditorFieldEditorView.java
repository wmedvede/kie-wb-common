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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.RadioInput;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class ConditionEditorFieldEditorView
        implements IsElement,
                   ConditionEditorFieldEditorPresenter.View {

    @Inject
    @DataField("simple-condition-radio")
    private RadioInput simpleCondition;

    @Inject
    @DataField("expression-condition-radio")
    private RadioInput expressionCondition;

    @Inject
    @DataField("editor-container")
    private Div editorContainer;

    @Inject
    @DataField("error-container")
    private Div errorContainer;

    private ConditionEditorFieldEditorPresenter presenter;

    @Override
    public void init(ConditionEditorFieldEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    public void init() {
        //TODO WM, do something
    }

    @Override
    public void addFunction(String function) {
        editorContainer.setTextContent(editorContainer.getTextContent() + ", " + function);
    }

    @Override
    public void setSimpleConditionChecked(boolean checked) {
        simpleCondition.setChecked(checked);
    }

    @Override
    public void setExpressionConditionChecked(boolean checked) {
        expressionCondition.setChecked(checked);
    }

    @Override
    public void setContent(HTMLElement content) {
        DOMUtil.removeAllChildren(editorContainer);
        editorContainer.appendChild(content);
    }

    @EventHandler("simple-condition-radio")
    private void onSimpleConditionChange(@ForEvent("change") final Event event) {
        presenter.onSimpleConditionSelected();
    }

    @EventHandler("expression-condition-radio")
    private void onExpressionConditionChange(@ForEvent("change") final Event event) {
        presenter.onExpressionConditionSelected();
    }
}
