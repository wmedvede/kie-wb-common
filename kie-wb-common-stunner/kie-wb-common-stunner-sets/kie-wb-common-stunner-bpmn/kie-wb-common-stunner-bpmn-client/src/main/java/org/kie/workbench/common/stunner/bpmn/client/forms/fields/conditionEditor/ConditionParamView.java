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

import javax.inject.Inject;

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

@Templated
public class ConditionParamView
        implements IsElement,
                   ConditionParamPresenter.View {

    @Inject
    @DataField("param-group")
    private Div paramGroup;

    @Inject
    @DataField("param-label")
    private Label paramLabel;

    @Inject
    @DataField("param-input")
    private TextInput paramInput;

    @Inject
    @DataField("param-error")
    private Span paramError;

    private ConditionParamPresenter presenter;

    @Override
    public void init(ConditionParamPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setName(String name) {
        paramLabel.setTextContent(name);
    }

    @Override
    public void clear() {
        setValue(null);
        clearError();
    }

    @Override
    public String getValue() {
        return paramInput.getValue();
    }

    @Override
    public void setValue(String value) {
        paramInput.setValue(value);
    }

    @Override
    public void clearError() {
        DOMUtil.removeCSSClass(paramGroup, "has-error");
        paramError.setTextContent(null);
    }

    @Override
    public void setError(String error) {
        DOMUtil.addCSSClass(paramGroup, "has-error");
        paramError.setTextContent(error);
    }

    @EventHandler("param-input")
    private void onValueChange(@ForEvent("change") final Event event) {
        presenter.onValueChange();
    }
}
