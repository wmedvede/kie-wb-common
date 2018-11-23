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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.FieldEditorPresenter;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParamDef;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.commons.data.Pair;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class SimpleConditionEditorPresenter
        extends FieldEditorPresenter<Condition> {

    private String FUNCTIONS_DOMAIN = "KieFunctions.";

    private static final String VARIABLE_NOT_SELECTED_ERROR = "A process variable, or variable field must be selected";

    private static final String NO_APPLICABLE_FUNCTIONS_WERE_FOUND_ERROR = "No applicable functions were found for selected variable or variable field.";

    private static final String PARAM_MUST_BE_COMPLETED_ERROR = "Param must be completed";

    private static final String DEFAULT_VARIABLE_OPTION_LABEL = "SimpleConditionEditorView.DefaultVariableOptionLabel";

    private static final String DEFAULT_FUNCTION_OPTION_LABEL = "SimpleConditionEditorView.DefaultFunctionOptionLabel";

    private static Pair<String, String> DEFAULT_VARIABLE_OPTION = new Pair<>("-- Select a process variable --", "");

    private static Pair<String, String> DEFAULT_FUNCTION_OPTION = new Pair<>("-- Select a condition --", "");

    public interface View extends UberElement<SimpleConditionEditorPresenter> {

        void setVariableOptions(List<Pair<String, String>> options, Pair<String, String> defaultOption);

        String getVariable();

        void setVariable(String variable);

        void setVariableError(String error);

        void clearVariableError();

        void setConditionOptions(List<Pair<String, String>> options, Pair<String, String> defaultOption);

        String getCondition();

        void setCondition(String condition);

        void setConditionError(String error);

        void clearConditionError();

        void removeParams();

        void addParam(HTMLElement param);
    }

    private View view;

    private ManagedInstance<ConditionParamPresenter> paramInstance;

    private ClientSession session;

    private Caller<ConditionEditorService> service;

    private ClientTranslationService translationService;

    private Map<String, VariableMetadata> variableMetadataMap = new HashMap<>();

    private Map<String, FunctionDef> currentFunctions = Collections.EMPTY_MAP;

    private List<ConditionParamPresenter> currentParams = new ArrayList<>();

    private boolean valid = false;

    @Inject
    public SimpleConditionEditorPresenter(View view,
                                          ManagedInstance<ConditionParamPresenter> paramInstance,
                                          Caller<ConditionEditorService> service,
                                          ClientTranslationService translationService) {
        this.view = view;
        this.paramInstance = paramInstance;
        this.service = service;
        this.translationService = translationService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        DEFAULT_VARIABLE_OPTION = new Pair<>(translationService.getValue(DEFAULT_VARIABLE_OPTION_LABEL), "");
        DEFAULT_FUNCTION_OPTION = new Pair<>(translationService.getValue(DEFAULT_FUNCTION_OPTION_LABEL), "");
        view.setVariableOptions(Collections.emptyList(), DEFAULT_VARIABLE_OPTION);
        view.setConditionOptions(Collections.emptyList(), DEFAULT_FUNCTION_OPTION);
    }

    public View getView() {
        return view;
    }

    public void init(ClientSession session, List<VariableMetadata> variablesMetadata) {
        this.session = session;
        setVariablesMetadata(variablesMetadata);
    }

    @Override
    public void setValue(Condition value) {
        super.setValue(value);
        clear();
        if (value != null) {
            if (value.getParams().size() >= 1) {
                Path path = session.getCanvasHandler().getDiagram().getMetadata().getPath();
                VariableMetadata variable = variableMetadataMap.get(value.getParams().get(0));
                if (variable != null) {
                    service.call(result -> onSetValue(value, ((List<FunctionDef>) result))).getAvailableFunctions(path, variable.getType());
                } else {
                    //TODO WM, acá tenemos q ver que hacemos
                    //si la variable no esta en la lista, pues podemos
                    //1) agregarla a saco
                    //2) mostrar un error, tal vez esto complica menos.
                }
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        //TODO WM set the editor in readonly mode.
    }

    public void clear() {
        view.setVariable(DEFAULT_VARIABLE_OPTION.getK2());
        view.setCondition(DEFAULT_FUNCTION_OPTION.getK2());
        removeParams();
        clearErrors();
    }

    public void onVariableChange() {
        view.clearVariableError();
        if (!isEmpty(view.getVariable())) {
            Path path = session.getCanvasHandler().getDiagram().getMetadata().getPath();
            VariableMetadata variable = variableMetadataMap.get(view.getVariable());
            service.call(result -> onLoadFunctionsSuccess((List<FunctionDef>) result),
                         (message, throwable) -> onLoadFunctionsError((Message) message, throwable)).getAvailableFunctions(path, variable.getType());
        } else {
            view.setVariableError(VARIABLE_NOT_SELECTED_ERROR);
        }
    }

    public void onConditionChange() {
        removeParams();
        if (!isEmpty(view.getCondition())) {
            initParams(view.getCondition(), Collections.EMPTY_LIST);
            validateAndApplyCondition();
        } else {
            view.setConditionError(NO_APPLICABLE_FUNCTIONS_WERE_FOUND_ERROR);
        }
    }

    public boolean isValid() {
        return valid;
    }

    private void validateAndApplyCondition() {
        Condition condition = new Condition();
        condition.setFunction(view.getCondition());
        condition.addParam(view.getVariable());

        valid = true;
        for (ConditionParamPresenter param : currentParams) {
            param.clearError();
            if (isValid(param)) {
                condition.getParams().add(param.getValue());
            } else {
                param.setError(PARAM_MUST_BE_COMPLETED_ERROR);
                valid = false;
            }
        }

        Condition oldValue = value;
        value = condition;
        notifyChange(oldValue, value);
    }

    private boolean isValid(ConditionParamPresenter param) {
        return !isEmpty(param.getValue());
    }

    private void onSetValue(Condition value, List<FunctionDef> functions) {
        setFunctions(functions);
        initParams(value.getFunction(), value.getParams());
        view.setVariable(value.getParams().get(0));
        view.setCondition(value.getFunction());
        valid = true;
    }

    private void onLoadFunctionsSuccess(List<FunctionDef> functions) {
        view.clearConditionError();
        setFunctions(functions);
        onConditionChange();
    }

    private boolean onLoadFunctionsError(Message error, Throwable throwable) {
        //TODO review this.
        Window.alert("Un expected error was produced while loading available conditions: " + throwable.getMessage());
        clear();
        return false;
    }

    private void initParams(String function, List<String> paramValues) {
        removeParams();
        Map<Integer, String> paramValue = new HashMap<>();
        if (paramValues != null) {
            for (int i = 0; i < paramValues.size(); i++) {
                paramValue.put(i, paramValues.get(i));
            }
        }
        FunctionDef functionDef = currentFunctions.get(function);
        ParamDef paramDef;
        for (int i = 1; i < functionDef.getParams().size(); i++) {
            paramDef = functionDef.getParams().get(i);
            ConditionParamPresenter param = paramInstance.get();
            currentParams.add(param);
            param.setName(paramDef.getName());
            param.setValue(paramValue.get(i));
            view.addParam(param.getView().getElement());
            param.setOnChangeCommand(() -> onParamChange(param));
        }
    }

    private void onParamChange(ConditionParamPresenter param) {
        validateAndApplyCondition();
    }

    private void setFunctions(List<FunctionDef> functions) {
        List<Pair<String, String>> functionOptions = functions.stream().
                map(functionDef -> new Pair<>(translateFunctionName(functionDef.getName()), functionDef.getName()))
                .collect(Collectors.toList());
        currentFunctions = functions.stream().collect(Collectors.toMap(FunctionDef::getName, Function.identity()));
        view.setConditionOptions(functionOptions, DEFAULT_FUNCTION_OPTION);
    }

    private String translateFunctionName(String function) {
        String result = translationService.getValue(FUNCTIONS_DOMAIN + function);
        return result != null ? result : function;
    }

    private void setVariablesMetadata(List<VariableMetadata> variablesMetadata) {
        List<Pair<String, String>> variableOptions = new ArrayList<>();
        variablesMetadata.forEach(variableMetadata -> {
            variableMetadataMap.put(variableMetadata.getName(), variableMetadata);
            variableOptions.add(new Pair<>(variableMetadata.getName(), variableMetadata.getName()));
        });
        view.setVariableOptions(variableOptions, DEFAULT_VARIABLE_OPTION);
    }

    private void clearErrors() {
        view.clearVariableError();
        view.clearConditionError();
    }

    private void removeParams() {
        currentParams.forEach(paramInstance::destroy);
        currentParams.clear();
        view.removeParams();
    }
}
