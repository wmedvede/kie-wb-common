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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.FieldEditorPresenter;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParamDef;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchDropDown;
import org.uberfire.ext.widgets.common.client.dropdown.SingleLiveSearchSelectionHandler;

import static org.kie.workbench.common.stunner.core.util.StringUtils.isEmpty;

public class SimpleConditionEditorPresenter
        extends FieldEditorPresenter<Condition> {

    static final String VARIABLE_NOT_SELECTED_ERROR = "SimpleConditionEditorView.VariableNotSelectedErrorMessage";

    static final String FUNCTION_NOT_SELECTED_ERROR = "SimpleConditionEditorView.FunctionNotSelectedErrorMessage";

    static final String VARIABLE_NOT_FOUND_ERROR = "SimpleConditionEditorView.VariableNotFoundError";

    static final String CONDITION_MAL_FORMED = "SimpleConditionEditorView.ConditionMalFormedError";

    public interface View extends UberElement<SimpleConditionEditorPresenter> {

        void setVariableError(String error);

        void clearVariableError();

        void setConditionError(String error);

        void clearConditionError();

        void removeParams();

        void addParam(HTMLElement param);

        LiveSearchDropDown<String> getVariableSelectorDropDown();

        LiveSearchDropDown<String> getConditionSelectorDropDown();
    }

    private View view;

    private ManagedInstance<ConditionParamPresenter> paramInstance;

    private VariableSearchService variableSearchService;

    private FunctionSearchService functionSearchService;

    private SingleLiveSearchSelectionHandler<String> variableSearchSelectionHandler;

    private SingleLiveSearchSelectionHandler<String> functionSearchSelectionHandler;

    private FunctionNamingService functionNamingService;

    private ClientTranslationService translationService;

    private List<ConditionParamPresenter> currentParams = new ArrayList<>();

    private boolean valid = false;

    @Inject
    public SimpleConditionEditorPresenter(View view,
                                          ManagedInstance<ConditionParamPresenter> paramInstance,
                                          VariableSearchService variableSearchService,
                                          FunctionSearchService functionSearchService,
                                          FunctionNamingService functionNamingService,
                                          ClientTranslationService translationService) {
        this.view = view;
        this.paramInstance = paramInstance;
        this.variableSearchService = variableSearchService;
        this.functionSearchService = functionSearchService;
        this.functionNamingService = functionNamingService;
        this.translationService = translationService;
        this.variableSearchSelectionHandler = newVariableSelectionHandler();
        this.functionSearchSelectionHandler = newFunctionSelectionHandler();
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.getVariableSelectorDropDown().init(variableSearchService, variableSearchSelectionHandler);
        view.getVariableSelectorDropDown().setOnChange(this::onVariableChange);
        view.getConditionSelectorDropDown().init(functionSearchService, functionSearchSelectionHandler);
        view.getConditionSelectorDropDown().setSearchCacheEnabled(false);
        view.getConditionSelectorDropDown().setOnChange(this::onConditionChange);
    }

    public View getView() {
        return view;
    }

    public void init(ClientSession session) {
        variableSearchService.init(session);
        functionSearchService.init(session);
    }

    @Override
    public void setValue(Condition value) {
        super.setValue(value);
        clear();
        if (value != null) {
            if (value.getParams().size() >= 1) {
                String type = variableSearchService.getOptionType(value.getParams().get(0));
                if (type != null) {
                    functionSearchService.reload(type, () -> onSetValue(value));
                } else {
                    view.setVariableError(translationService.getValue(VARIABLE_NOT_FOUND_ERROR, value.getParams().get(0)));
                }
            } else {
                //uncommon case
                view.setConditionError(translationService.getValue(CONDITION_MAL_FORMED));
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        view.getVariableSelectorDropDown().setEnabled(!readOnly);
        view.getConditionSelectorDropDown().setEnabled(!readOnly);
        currentParams.forEach(param -> param.setReadonly(readOnly));
    }

    public void clear() {
        variableSearchSelectionHandler.clearSelection();
        functionSearchSelectionHandler.clearSelection();
        functionSearchService.clear();
        removeParams();
        clearErrors();
    }

    void onVariableChange() {
        clearErrors();
        String variable = variableSearchSelectionHandler.getSelectedKey();
        if (!isEmpty(variable)) {
            String type = variableSearchService.getOptionType(variable);
            functionSearchService.reload(type, () -> {
                functionSearchSelectionHandler.clearSelection();
                view.getConditionSelectorDropDown().clear();
                onConditionChange();
            });
        } else {
            functionSearchService.clear();
            functionSearchSelectionHandler.clearSelection();
            view.getConditionSelectorDropDown().clear();
            view.setVariableError(translationService.getValue(VARIABLE_NOT_SELECTED_ERROR));
            applyChange();
        }
    }

    void onConditionChange() {
        view.clearConditionError();
        removeParams();
        String function = functionSearchSelectionHandler.getSelectedKey();
        if (!isEmpty(function)) {
            initParams(function, Collections.EMPTY_LIST);
        } else {
            view.setConditionError(translationService.getValue(FUNCTION_NOT_SELECTED_ERROR));
        }
        applyChange();
    }

    public boolean isValid() {
        return valid;
    }

    private void applyChange() {
        String variable = variableSearchSelectionHandler.getSelectedKey();
        String type = variableSearchService.getOptionType(variable);
        String function = functionSearchSelectionHandler.getSelectedKey();
        Condition condition = new Condition();
        condition.setFunction(function);
        condition.addParam(variable);

        valid = !isEmpty(variable) && !isEmpty(function) && !isEmpty(type);
        for (ConditionParamPresenter param : currentParams) {
            if (!param.validateParam(type)) {
                valid = false;
            }
            condition.getParams().add(param.getValue());
        }

        Condition oldValue = value;
        value = condition;
        notifyChange(oldValue, value);
    }

    private void onSetValue(Condition value) {
        initParams(value.getFunction(), value.getParams());
        view.getVariableSelectorDropDown().setSelectedItem(value.getParams().get(0));
        view.getConditionSelectorDropDown().setSelectedItem(value.getFunction());
        valid = true;
    }

    private void initParams(String function, List<String> paramValues) {
        removeParams();
        Map<Integer, String> paramValue = new HashMap<>();
        if (paramValues != null) {
            for (int i = 0; i < paramValues.size(); i++) {
                paramValue.put(i, paramValues.get(i));
            }
        }
        FunctionDef functionDef = functionSearchService.getFunction(function);
        ParamDef paramDef;
        for (int i = 1; i < functionDef.getParams().size(); i++) {
            paramDef = functionDef.getParams().get(i);
            ConditionParamPresenter param = paramInstance.get();
            currentParams.add(param);
            param.setName(functionNamingService.getParamName(functionDef.getName(), paramDef.getName()));
            param.setHelp(functionNamingService.getParamHelp(functionDef.getName(), paramDef.getName()));
            param.setValue(paramValue.get(i));
            view.addParam(param.getView().getElement());
            param.setOnChangeCommand(this::onParamChange);
        }
    }

    private void onParamChange() {
        applyChange();
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

    /**
     * For facilitating testing
     */
    SingleLiveSearchSelectionHandler<String> newVariableSelectionHandler() {
        return new SingleLiveSearchSelectionHandler<>();
    }

    /**
     * For facilitating testing
     */
    SingleLiveSearchSelectionHandler<String> newFunctionSelectionHandler() {
        return new SingleLiveSearchSelectionHandler<>();
    }

}
