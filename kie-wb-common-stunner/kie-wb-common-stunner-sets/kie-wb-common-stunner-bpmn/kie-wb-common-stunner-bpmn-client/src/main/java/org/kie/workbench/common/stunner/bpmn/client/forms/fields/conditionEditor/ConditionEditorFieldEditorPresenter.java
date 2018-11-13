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
import org.kie.workbench.common.stunner.bpmn.forms.model.ScriptTypeMode;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.uberfire.client.mvp.UberElement;

public class ConditionEditorFieldEditorPresenter
        extends FieldEditorPresenter<ScriptTypeValue> {

    public interface View extends UberElement<ConditionEditorFieldEditorPresenter> {

        void addFunction(String function);

        void setSimpleConditionChecked(boolean checked);

        void setExpressionConditionChecked(boolean checked);

        void setContent(HTMLElement content);
    }

    private View view;

    private SimpleConditionEditorPresenter simpleCondition;

    private ScriptTypeFieldEditorPresenter expressionCondition;

    private Caller<ConditionEditorService> service;

    private ClientSession session;

    private List<VariableMetadata> variables = new ArrayList<>();

    @Inject
    public ConditionEditorFieldEditorPresenter(View view,
                                               SimpleConditionEditorPresenter simpleCondition,
                                               ScriptTypeFieldEditorPresenter expressionCondition,
                                               Caller<ConditionEditorService> service) {
        this.view = view;
        this.simpleCondition = simpleCondition;
        this.expressionCondition = expressionCondition;
        this.service = service;
    }

    @Override
    protected IsElement getView() {
        return view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.setSimpleConditionChecked(true);
        view.setContent(simpleCondition.getView().getElement());
        expressionCondition.setMode(ScriptTypeMode.FLOW_CONDITION);
    }

    public void init(ClientSession session) {
        this.session = session;
        simpleCondition.init(session);
        initializeVariables();
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public void setValue(ScriptTypeValue value) {
        super.setValue(value);
        //TODO WM acá me estan seteando el ScriptTypeValue que viene del modelo
        //tengo que setear la UI adecuadamente.
        //Si el script es java y ademas podemos parsearlo, entonces
        //configuramos directamente el condition editor
        //sino pasamos directamente al expression editor.

        //Cuidado, me podrian estar seteando el value antes de que haya logrado cargar las variables....
        //y el modelo...

        if (value != null) {
            //DO the job here
        }
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
        simpleCondition.setVariablesMetadata(variables);
    }

    public void onSimpleConditionSelected() {
        view.setContent(simpleCondition.getView().getElement());
    }

    public void onExpressionConditionSelected() {
        view.setContent(expressionCondition.getView().getElement());
    }

    private String unboxDefaultType(String type) {
        if ("Boolean".equals(type)) {
            return Boolean.class.getName();
        } else if ("Float".equals(type)) {
            return Float.class.getName();
        } else if ("Integer".equals(type)) {
            return Integer.class.getName();
        } else if ("Object".equals(type)) {
            return Object.class.getName();
        }
        return type;
    }
}
