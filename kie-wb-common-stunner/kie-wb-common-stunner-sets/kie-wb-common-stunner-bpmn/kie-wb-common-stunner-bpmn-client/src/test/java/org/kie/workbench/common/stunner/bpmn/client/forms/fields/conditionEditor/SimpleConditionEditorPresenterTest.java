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

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParamDef;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchDropDown;
import org.uberfire.ext.widgets.common.client.dropdown.SingleLiveSearchSelectionHandler;
import org.uberfire.mvp.Command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.SimpleConditionEditorPresenter.CONDITION_MAL_FORMED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleConditionEditorPresenterTest {

    private static final String TRANSLATED_MESSAGE = "TRANSLATED_MESSAGE";

    private static final String FUNCTION = "FUNCTION";
    private static final String FUNCTION_TRANSLATED_NAME = "FUNCTION_TRANSLATED_NAME";
    private static final String PARAM1 = "PARAM1";
    private static final String PARAM1_NAME = "PARAM1_NAME";
    private static final String PARAM1_TYPE = "PARAM1_TYPE";
    private static final String PARAM1_TRANSLATED_NAME = "PARAM1_TRANSLATED_NAME";
    private static final String PARAM1_TRANSLATED_HELP = "PARAM1_TRANSLATED_HELP";

    private static final String PARAM2 = "PARAM2";
    private static final String PARAM2_TYPE = "PARAM2_TYPE";
    private static final String PARAM2_NAME = "PARAM2_NAME";
    private static final String PARAM2_TRANSLATED_NAME = "PARAM2_TRANSLATED_NAME";
    private static final String PARAM2_TRANSLATED_HELP = "PARAM2_TRANSLATED_HELP";

    private static final String VARIABLE = "VARIABLE";

    @Mock
    private SimpleConditionEditorPresenter.View view;

    @Mock
    private ManagedInstance<ConditionParamPresenter> paramInstance;

    @Mock
    private VariableSearchService variableSearchService;

    @Mock
    private FunctionSearchService functionSearchService;

    @Mock
    private FunctionNamingService functionNamingService;

    @Mock
    private ClientTranslationService translationService;

    @Mock
    private LiveSearchDropDown<String> variableSelectorDropDown;

    @Captor
    private ArgumentCaptor<SingleLiveSearchSelectionHandler<String>> variableSearchSelectionHandlerCaptor;

    @Mock
    private SingleLiveSearchSelectionHandler<String> variableSearchSelectionHandler;

    @Mock
    private LiveSearchDropDown<String> conditionSelectorDropDown;

    @Captor
    private ArgumentCaptor<SingleLiveSearchSelectionHandler<String>> functionSearchSelectionHandlerCaptor;

    @Mock
    private SingleLiveSearchSelectionHandler<String> functionSearchSelectionHandler;

    @Captor
    private ArgumentCaptor<Command> commandCaptor;

    private SimpleConditionEditorPresenter presenter;

    @Mock
    private ClientSession session;

    @Before
    public void setUp() {
        when(view.getConditionSelectorDropDown()).thenReturn(conditionSelectorDropDown);
        when(view.getVariableSelectorDropDown()).thenReturn(variableSelectorDropDown);
        presenter = new SimpleConditionEditorPresenter(view,
                                                       paramInstance,
                                                       variableSearchService,
                                                       functionSearchService,
                                                       functionNamingService,
                                                       translationService) {
            @Override
            SingleLiveSearchSelectionHandler<String> newVariableSelectionHandler() {
                return variableSearchSelectionHandler;
            }

            @Override
            SingleLiveSearchSelectionHandler<String> newFunctionSelectionHandler() {
                return functionSearchSelectionHandler;
            }
        };
    }

    @Test
    public void testInit() {
        presenter.init();
        verify(view).init(presenter);
        verify(variableSelectorDropDown).init(variableSearchService, variableSearchSelectionHandler);
        verify(variableSelectorDropDown).setOnChange(any());
        verify(conditionSelectorDropDown).init(functionSearchService, functionSearchSelectionHandler);
        verify(conditionSelectorDropDown).setOnChange(any());
        verify(conditionSelectorDropDown).setSearchCacheEnabled(false);
    }

    @Test
    public void setGetView() {
        assertEquals(view, presenter.getView());
    }

    @Test
    public void testInitSession() {
        presenter.init(session);
        verify(variableSearchService).init(session);
        verify(functionSearchService).init(session);
    }

    @Test
    public void testSetValueNull() {
        presenter.setValue(null);
        verifyClear();
        assertNull(presenter.getValue());
    }

    @Test
    public void testSetValueWhenConditionMalFormed() {
        Condition condition = new Condition();
        when(translationService.getValue(CONDITION_MAL_FORMED)).thenReturn(TRANSLATED_MESSAGE);
        presenter.setValue(condition);
        verify(view).setConditionError(TRANSLATED_MESSAGE);
        verifyClear();
        assertEquals(condition, presenter.getValue());
    }

    @Test
    public void testSetValueWhenConditionOK() {
        Condition condition = new Condition(FUNCTION);
        condition.addParam(PARAM1);
        condition.addParam(PARAM2);
        when(variableSearchService.getOptionType(PARAM1)).thenReturn(PARAM1_TYPE);

        List<ParamDef> params = new ArrayList<>();
        params.add(mockParamDef(PARAM1_NAME, PARAM1_TYPE));
        params.add(mockParamDef(PARAM2_NAME, PARAM2_TYPE));
        FunctionDef functionDef = new FunctionDef(FUNCTION, params);
        when(functionSearchService.getFunction(FUNCTION)).thenReturn(functionDef);

        ConditionParamPresenter paramPresenter = mock(ConditionParamPresenter.class);
        ConditionParamPresenter.View paramPresenterView = mock(ConditionParamPresenter.View.class);
        HTMLElement element = mock(HTMLElement.class);
        when(paramPresenter.getView()).thenReturn(paramPresenterView);
        when(paramPresenterView.getElement()).thenReturn(element);
        when(paramInstance.get()).thenReturn(paramPresenter);

        when(functionNamingService.getFunctionName(FUNCTION)).thenReturn(FUNCTION_TRANSLATED_NAME);
        when(functionNamingService.getParamName(FUNCTION, PARAM1_NAME)).thenReturn(PARAM1_TRANSLATED_NAME);
        when(functionNamingService.getParamHelp(FUNCTION, PARAM1_NAME)).thenReturn(PARAM1_TRANSLATED_HELP);
        when(functionNamingService.getParamName(FUNCTION, PARAM2_NAME)).thenReturn(PARAM2_TRANSLATED_NAME);
        when(functionNamingService.getParamHelp(FUNCTION, PARAM2_NAME)).thenReturn(PARAM2_TRANSLATED_HELP);

        presenter.setValue(condition);

        verify(functionSearchService).reload(eq(PARAM1_TYPE), commandCaptor.capture());
        commandCaptor.getValue().execute();
        variableSelectorDropDown.setSelectedItem(PARAM1);
        conditionSelectorDropDown.setSelectedItem(FUNCTION);

        verify(paramPresenter).setValue(PARAM2);
        verify(paramPresenter).setName(PARAM2_TRANSLATED_NAME);
        verify(paramPresenter).setHelp(PARAM2_TRANSLATED_HELP);
    }

    @Test
    public void testSetReadonlyTrue() {
        testSetReadonly(true);
    }

    @Test
    public void testSetReadonlyFalse() {
        testSetReadonly(false);
    }

    @Test
    public void testClear() {
        presenter.clear();
        verifyClear();
    }

    @Test
    public void testOnVariableChangeWhenVariableSelected() {
        presenter.init();
        verify(variableSelectorDropDown).init(eq(variableSearchService), variableSearchSelectionHandlerCaptor.capture());
        variableSearchSelectionHandlerCaptor.getValue().selectKey("variable");

        presenter.onVariableChange();
        when()
        //verify(variableSearchSelectionHandlerCaptor.getValue()).clearSelection();
        //when(var)
        //verifyClearError();

    }

    private void testSetReadonly(boolean readonly) {
        presenter.setReadOnly(readonly);
        verify(variableSelectorDropDown).setEnabled(!readonly);
        verify(conditionSelectorDropDown).setEnabled(!readonly);
    }

    private void verifyClear() {
        verify(variableSearchSelectionHandler).clearSelection();
        verify(functionSearchSelectionHandler).clearSelection();
        functionSearchService.clear();
        verify(view).removeParams();
        verifyClearError();
    }

    private void verifyClearError() {
        verify(view).clearConditionError();
        verify(view).clearVariableError();
    }

    ParamDef mockParamDef(String name, String type) {
        ParamDef paramDef = mock(ParamDef.class);
        when(paramDef.getName()).thenReturn(name);
        when(paramDef.getType()).thenReturn(type);
        return paramDef;
    }
}
