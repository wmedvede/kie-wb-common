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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.commons.data.Pair;

public class SimpleConditionEditorPresenter {

    public interface View extends UberElement<SimpleConditionEditorPresenter> {

        void setVariableOptions(List<Pair<String, String>> options);

        String getVariable();

        void setConditionOptions(List<Pair<String,String>> options);

        String getCondition();
    }

    private View view;

    private ClientSession session;

    private Caller<ConditionEditorService> service;

    private Map<String, VariableMetadata> variableMetadataMap = new HashMap<>();

    @Inject
    public SimpleConditionEditorPresenter(View view,
                                          Caller<ConditionEditorService> service) {
        this.view = view;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public View getView() {
        return view;
    }

    public void init(ClientSession session) {
        this.session = session;
    }

    public void setVariablesMetadata(List<VariableMetadata> variablesMetadata) {
        List<Pair<String, String>> variableOptions = new ArrayList<>();
        variablesMetadata.forEach(variableMetadata -> {
            variableMetadataMap.put(variableMetadata.getName(), variableMetadata);
            variableOptions.add(new Pair<>(variableMetadata.getName(), variableMetadata.getName()));
        });
        view.setVariableOptions(variableOptions);
    }

    public void onVariableChange() {
        Path path = session.getCanvasHandler().getDiagram().getMetadata().getPath();
        VariableMetadata variable = variableMetadataMap.get(view.getVariable());
        service.call((RemoteCallback<List<FunctionDef>>) this::loadFunctions).getAvailableFunctions(path, variable.getType());
    }

    private void loadFunctions(List<FunctionDef> functions) {
        List<Pair<String, String>> functionOptions = functions.stream().
                map(functionDef -> new Pair<>(functionDef.getName(), functionDef.getName()))
                .collect(Collectors.toList());
        view.setConditionOptions(functionOptions);
    }
}
