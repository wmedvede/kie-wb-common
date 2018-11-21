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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.scriptEditor.ScriptTypeFieldEditorPresenter;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.FieldEditorPresenter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionExpression;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.GenerateConditionResult;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParseConditionResult;
import org.kie.workbench.common.stunner.bpmn.forms.model.ScriptTypeMode;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.uberfire.client.mvp.UberElement;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class ConditionEditorFieldEditorPresenter
        extends FieldEditorPresenter<ScriptTypeValue> {

    private static final String DEFAULT_LANGUAGE = "java";

    private static final String SCRIPT_PARSING_ERROR = "It was not possible to parse current script into a condition";

    public interface View extends UberElement<ConditionEditorFieldEditorPresenter> {

        void setSimpleConditionChecked(boolean checked);

        void setSimpleConditionEnabled(boolean enabled);

        void setScriptConditionChecked(boolean checked);

        void setContent(HTMLElement content);

        void showError(String error);

        void clearError();
    }

    private View view;

    private SimpleConditionEditorPresenter simpleConditionEditor;

    private ScriptTypeFieldEditorPresenter scriptEditor;

    private Caller<ConditionEditorService> service;

    private ClientSession session;

    private List<VariableMetadata> variables = new ArrayList<>();

    @Inject
    public ConditionEditorFieldEditorPresenter(View view,
                                               SimpleConditionEditorPresenter simpleConditionEditor,
                                               ScriptTypeFieldEditorPresenter scriptEditor,
                                               Caller<ConditionEditorService> service) {
        this.view = view;
        this.simpleConditionEditor = simpleConditionEditor;
        this.scriptEditor = scriptEditor;
        this.service = service;
    }

    @Override
    protected IsElement getView() {
        return view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        scriptEditor.setMode(ScriptTypeMode.FLOW_CONDITION);
        scriptEditor.addChangeHandler(this::onScriptChange);
        simpleConditionEditor.addChangeHandler(this::onSimpleConditionChange);
        showSimpleConditionEditor();
    }

    public void init(ClientSession session) {
        this.session = session;
        initializeVariables();
        simpleConditionEditor.init(session, variables);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        simpleConditionEditor.setReadOnly(readOnly);
        scriptEditor.setReadOnly(readOnly);
    }

    @Override
    public void setValue(ScriptTypeValue value) {
        super.setValue(value);
        scriptEditor.setValue(value);
        simpleConditionEditor.clear();
        clearError();
        if (value != null) {
            if (isInDefaultLanguage(value)) {
                if (!isEmpty(value.getScript())) {
                    //TODO WM check unexpected error case
                    service.call(result -> onSetValue((ParseConditionResult)result)).parseCondition(value.getScript());
                } else {
                    showSimpleConditionEditor();
                }
            } else {
                showScriptEditor();
            }
        } else {
            simpleConditionEditor.setValue(null);
            showSimpleConditionEditor();
        }
    }

    public void onSimpleConditionSelected() {
        clearError();
        if (value != null && !isEmpty(value.getScript())) {
            service.call(result -> onSimpleConditionSelected((ParseConditionResult) result)).parseCondition(value.getScript());
        } else {
            showSimpleConditionEditor();
        }
    }

    public void onScriptEditorSelected() {
        scriptEditor.setValue(value);
        showScriptEditor();
    }

    private void onSimpleConditionChange(ConditionExpression oldValue, ConditionExpression newValue) {
        if (simpleConditionEditor.isValid()) {
            service.call(result -> onSimpleConditionChange((GenerateConditionResult) result)).generateCondition(newValue.getConditions().get(0));
        } else {
            clearError();
        }
    }

    private void onSimpleConditionChange(GenerateConditionResult result) {
        clearError();
        if (!result.hasError()) {
            ScriptTypeValue oldValue = value;
            value = new ScriptTypeValue(DEFAULT_LANGUAGE, result.getExpression());
            notifyChange(oldValue, value);
        } else {
            showError(result.getError());
        }
    }

    private void onScriptChange(ScriptTypeValue oldValue, ScriptTypeValue newValue) {
        value = newValue;
        notifyChange(oldValue, newValue);
        enableSimpleConditionEditor(isInDefaultLanguage(newValue));
    }

    private void onSetValue(ParseConditionResult result) {
        if (!result.hasError()) {
            ConditionExpression conditionExpression = new ConditionExpression();
            conditionExpression.getConditions().add(result.getCondition());
            simpleConditionEditor.setValue(conditionExpression);
            showSimpleConditionEditor();
        } else {
            scriptEditor.setValue(value);
            showScriptEditor();
        }
    }

    private void onSimpleConditionSelected(ParseConditionResult result) {
        if (!result.hasError()) {
            ConditionExpression conditionExpression = new ConditionExpression();
            conditionExpression.getConditions().add(result.getCondition());
            simpleConditionEditor.setValue(conditionExpression);
        } else {
            showError(SCRIPT_PARSING_ERROR + ": " + result.getError());
            simpleConditionEditor.setValue(null);
        }
        showSimpleConditionEditor();
    }

    private void initializeVariables() {
        //TODO WM, review this initialization.
        //It should include parent process variables in case of a subprocess?
        String canvasRootUUID = session.getCanvasHandler().getDiagram().getMetadata().getCanvasRootUUID();
        if (canvasRootUUID != null) {
            Node node = session.getCanvasHandler().getDiagram().getGraph().getNode(canvasRootUUID);
            Object definition = ((org.kie.workbench.common.stunner.core.graph.content.view.View) node.getContent()).getDefinition();
            if (definition instanceof BPMNDiagram) {
                BPMNDiagramImpl bpmnDiagram = (BPMNDiagramImpl) definition;
                String processVars = bpmnDiagram.getProcessData().getProcessVariables().getValue();
                String[] variableDefs = processVars.split(",");
                for (String variableDefItem : variableDefs) {
                    if (!variableDefItem.isEmpty()) {
                        String[] variableDef = variableDefItem.split(":");
                        if (variableDef.length == 1) {
                            variables.add(new VariableMetadata(variableDef[0], Object.class.getName()));
                        } else {
                            variables.add(new VariableMetadata(variableDef[0], unboxDefaultType(variableDef[1])));
                        }
                    }
                }
            }
        }
    }

    private String unboxDefaultType(String type) {
        if ("Boolean".equals(type)) {
            return Boolean.class.getName();
        } else if ("Float".equals(type)) {
            return Float.class.getName();
        } else if ("Integer".equals(type)) {
            return Integer.class.getName();
        } else if ("String".equals(type)) {
            return String.class.getName();
        } else if ("Object".equals(type)) {
            return Object.class.getName();
        }
        return type;
    }

    private void enableSimpleConditionEditor(boolean enable) {
        view.setSimpleConditionEnabled(enable);
    }

    private boolean isInDefaultLanguage(ScriptTypeValue value) {
        return value != null && DEFAULT_LANGUAGE.equals(value.getLanguage());
    }

    private void showSimpleConditionEditor() {
        view.setSimpleConditionChecked(true);
        view.setScriptConditionChecked(false);
        view.setContent(simpleConditionEditor.getView().getElement());
    }

    private void showScriptEditor() {
        view.setScriptConditionChecked(true);
        view.setSimpleConditionChecked(false);
        view.setContent(scriptEditor.getView().getElement());
    }

    private void showError(String error) {
        view.showError(error);
    }

    private void clearError() {
        view.clearError();
    }
}
