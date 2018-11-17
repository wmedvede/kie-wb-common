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

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.commons.data.Pair;

import static org.kie.workbench.common.stunner.bpmn.client.forms.util.SelectUtils.setOptions;

@Templated
public class SimpleConditionEditorView
        implements IsElement,
                   SimpleConditionEditorPresenter.View {

    private SimpleConditionEditorPresenter presenter;

    @Inject
    @DataField("variable-selector")
    private Select variableSelector;

    @Inject
    @DataField("variable-selector-error")
    private Span variableSelectorError;

    @Inject
    @DataField("condition-selector")
    private Select conditionSelector;

    @Inject
    @DataField("condition-selector-error")
    private Span conditionSelectorError;

    @Inject
    @DataField("condition-params")
    private Div conditionParams;

    @Override
    public void init(SimpleConditionEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setVariableOptions(List<Pair<String, String>> options) {
        setOptions(variableSelector, options);
    }

    @Override
    public String getVariable() {
        return variableSelector.getValue();
    }

    @Override
    public void setVariable(String variable) {
        variableSelector.setValue(variable);
    }

    @Override
    public void setVariableError(String error) {
        variableSelectorError.setTextContent(error);
    }

    @Override
    public void clearVariableError() {
        variableSelectorError.setTextContent(null);
    }

    @Override
    public void setConditionOptions(List<Pair<String, String>> options) {
        setOptions(conditionSelector, options);
    }

    @Override
    public String getCondition() {
        return conditionSelector.getValue();
    }

    @Override
    public void setCondition(String condition) {
        conditionSelector.setValue(condition);
    }

    @Override
    public void setConditionError(String error) {
        conditionSelectorError.setTextContent(error);
    }

    @Override
    public void clearConditionError() {
        conditionSelectorError.setTextContent(null);
    }

    @Override
    public void removeParams() {
        DOMUtil.removeAllChildren(conditionParams);
    }

    @Override
    public void addParam(HTMLElement param) {
        conditionParams.appendChild(param);
    }

    @EventHandler("variable-selector")
    private void onVariableChange(@ForEvent("change") final Event event) {
        presenter.onVariableChange();
    }

    @EventHandler("condition-selector")
    private void onConditionChange(@ForEvent("change") final Event event) {
        presenter.onConditionChange();
    }
}
