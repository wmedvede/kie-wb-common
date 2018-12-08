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

import org.jboss.errai.common.client.api.Caller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.Command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FunctionSearchServiceTest {

    private static final String FUNCTION1 = "FUNCTION1";
    private static final String FUNCTION1_NAME = "FUNCTION1_NAME";
    private static final String FUNCTION2 = "FUNCTION2";
    private static final String FUNCTION2_NAME = "FUNCTION2_NAME";

    private Caller<ConditionEditorService> editorServiceCaller;

    @Mock
    private ConditionEditorService editorService;

    @Mock
    private FunctionNamingService functionNamingService;

    @Mock
    private FunctionSearchService searchService;

    @Mock
    private ClientSession clientSession;

    @Mock
    private CanvasHandler canvasHandler;

    @Mock
    private Diagram diagram;

    @Mock
    private Metadata metadata;

    @Mock
    private Path path;

    @Mock
    private Command command;

    @Before
    public void setUp() {
        when(clientSession.getCanvasHandler()).thenReturn(canvasHandler);
        when(canvasHandler.getDiagram()).thenReturn(diagram);
        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getPath()).thenReturn(path);
        editorServiceCaller = new CallerMock<>(editorService);
        searchService = new FunctionSearchService(editorServiceCaller, functionNamingService);
    }

    @Test
    public void testInit() {
        searchService.init(clientSession);
        verify(clientSession).getCanvasHandler();
        verify(canvasHandler).getDiagram();
        verify(diagram).getMetadata();
        verify(metadata).getPath();
    }

    @Test
    public void testReload() {
        searchService.init(clientSession);

        FunctionDef someFunction = searchService.getFunction(FUNCTION1);
        assertNull(someFunction);

        List<FunctionDef> functions = new ArrayList<>();
        functions.add(mockFunctionDef(FUNCTION1, FUNCTION1_NAME));
        functions.add(mockFunctionDef(FUNCTION2, FUNCTION2_NAME));
        when(editorService.findAvailableFunctions(path, "TYPE")).thenReturn(functions);

        searchService.reload("TYPE", command);
        verify(editorService).findAvailableFunctions(path, "TYPE");
        someFunction = searchService.getFunction(FUNCTION1);
        assertNotNull(someFunction);
        verify(command).execute();
    }

    private FunctionDef mockFunctionDef(String function, String translatedName) {
        FunctionDef functionDef = mock(FunctionDef.class);
        when(functionDef.getName()).thenReturn(function);
        when(functionNamingService.getFunctionName(function)).thenReturn(translatedName);
        return functionDef;
    }
}
