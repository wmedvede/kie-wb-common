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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.TypeMetadata;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.TypeMetadataQuery;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.TypeMetadataQueryResult;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.uberfire.backend.vfs.Path;
import org.uberfire.commons.data.Pair;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchCallback;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchResults;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchService;

import static org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.SimpleConditionEditorPresenter.unboxDefaultType;

public class VariableSearchService implements LiveSearchService<String> {

    private Caller<ConditionEditorService> service;

    private Map<String, String> options = new HashMap<>();

    private Map<String, VariableMetadata> variablesMetadata = new HashMap<>();

    private Map<String, TypeMetadata> typesMetadata = new HashMap<>();

    private Map<String, String> optionType = new HashMap<>();

    @Inject
    public VariableSearchService(Caller<ConditionEditorService> service) {
        this.service = service;
    }

    public void init(ClientSession session) {
        String canvasRootUUID = session.getCanvasHandler().getDiagram().getMetadata().getCanvasRootUUID();
        if (canvasRootUUID != null) {
            Node node = session.getCanvasHandler().getDiagram().getGraph().getNode(canvasRootUUID);
            Object definition = ((org.kie.workbench.common.stunner.core.graph.content.view.View) node.getContent()).getDefinition();
            if (definition instanceof BPMNDiagram) {
                BPMNDiagramImpl bpmnDiagram = (BPMNDiagramImpl) definition;
                String processVars = bpmnDiagram.getProcessData().getProcessVariables().getValue();
                String[] variableDefs = processVars.split(",");
                List<String> types = new ArrayList<>();
                List<VariableMetadata> variables = new ArrayList<>();
                VariableMetadata variableMetadata;
                for (String variableDefItem : variableDefs) {
                    if (!variableDefItem.isEmpty()) {
                        String[] variableDef = variableDefItem.split(":");
                        if (variableDef.length == 1) {
                            variableMetadata = new VariableMetadata(variableDef[0], Object.class.getName());
                        } else {
                            variableMetadata = new VariableMetadata(variableDef[0], unboxDefaultType(variableDef[1]));
                        }
                        variables.add(variableMetadata);
                        types.add(variableMetadata.getType());
                    }
                }
                Path path = session.getCanvasHandler().getDiagram().getMetadata().getPath();
                TypeMetadataQuery query = new TypeMetadataQuery(path, types);
                service.call(result -> initVariables(variables, ((TypeMetadataQueryResult) result))).findMetadata(query);
            }
        }
    }

    @Override
    public void search(String pattern, int maxResults, LiveSearchCallback<String> callback) {
        LiveSearchResults<String> results = new LiveSearchResults<>(maxResults);
        options.entrySet().stream()
                .filter(entry -> entry.getValue().toLowerCase().startsWith(pattern.toLowerCase()))
                .forEach(entry -> results.add(entry.getKey(), entry.getValue()));
        callback.afterSearch(results);
    }

    @Override
    public void searchEntry(String key, LiveSearchCallback<String> callback) {
        LiveSearchResults<String> results = new LiveSearchResults<>();
        if (options.containsKey(key)) {
            results.add(key, options.get(key));
        }
        callback.afterSearch(results);
    }

    public String getOptionType(String key) {
        return optionType.get(key);
    }

    public void clear() {
        options.clear();
        variablesMetadata.clear();
        typesMetadata.clear();
        optionType.clear();
    }

    private void initVariables(List<VariableMetadata> variables, TypeMetadataQueryResult result) {
        variablesMetadata.clear();
        optionType.clear();
        typesMetadata = result.getTypeMetadatas().stream().collect(Collectors.toMap(TypeMetadata::getType, Function.identity()));
        variables.forEach(variableMetadata -> {
            TypeMetadata typeMetadata = Optional.ofNullable(typesMetadata.get(variableMetadata.getType())).orElse(new TypeMetadata(Object.class.getName()));
            variableMetadata.setMetadata(typeMetadata);
            variablesMetadata.put(variableMetadata.getName(), variableMetadata);
            addVariableOptions(variableMetadata);
        });
    }

    private void addVariableOptions(VariableMetadata variableMetadata) {
        Pair<String, String> variableOption = new Pair<>(variableMetadata.getName(), variableMetadata.getName());
        options.put(variableOption.getK1(), variableOption.getK2());
        optionType.put(variableOption.getK1(), unboxDefaultType(variableMetadata.getType()));
        TypeMetadata typeMetadata = variableMetadata.getMetadata();
        typeMetadata.getFieldMetadata().stream()
                .filter(fieldMetadata -> fieldMetadata.getAccessor() != null)
                .forEach(fieldMetadata -> {
                    Pair<String, String> fieldOption = new Pair<>(variableMetadata.getName() + "." + fieldMetadata.getAccessor() + "()",
                                                                  variableMetadata.getName() + "." + fieldMetadata.getName());
                    options.put(fieldOption.getK1(), fieldOption.getK2());
                    optionType.put(fieldOption.getK1(), unboxDefaultType(fieldMetadata.getType()));
                });
    }
}