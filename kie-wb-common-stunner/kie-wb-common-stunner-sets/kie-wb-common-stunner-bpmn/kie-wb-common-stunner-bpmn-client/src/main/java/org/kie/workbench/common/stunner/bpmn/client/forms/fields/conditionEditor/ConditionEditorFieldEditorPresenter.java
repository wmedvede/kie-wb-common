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

import org.jboss.errai.common.client.api.IsElement;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.FieldEditorPresenter;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;
//import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
//import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.uberfire.client.mvp.UberElement;

public class ConditionEditorFieldEditorPresenter
        extends FieldEditorPresenter<ScriptTypeValue> {

    public interface View extends UberElement<ConditionEditorFieldEditorPresenter> {
        void addFunction(String function);
    }

    private View view;

    //private ConditionEditorService service;

    @Inject
    public ConditionEditorFieldEditorPresenter(View view/*, ConditionEditorService service*/) {
        this.view = view;
        //this.service = service;
    }

    @Override
    protected IsElement getView() {
        return view;
    }

    @PostConstruct
    public void init() {
        view.init(this);/*
        List<FunctionDef> functions = service.getAvailableFunctions(Object.class);
        functions.forEach(functionDef -> view.addFunction(functionDef.getName()));*/
        view.addFunction("Function 1");
        view.addFunction("Function 2");
        view.addFunction("Function 3");
        view.addFunction("Function 4");
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }
}
