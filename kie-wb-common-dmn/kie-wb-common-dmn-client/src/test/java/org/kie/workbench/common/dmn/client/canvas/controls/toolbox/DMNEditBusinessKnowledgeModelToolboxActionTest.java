/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.canvas.controls.toolbox;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.v1_1.BusinessKnowledgeModel;
import org.kie.workbench.common.dmn.api.definition.v1_1.FunctionDefinition;
import org.kie.workbench.common.dmn.client.events.EditExpressionEvent;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.ToolboxAction;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseClickEvent;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundsImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.kie.workbench.common.stunner.core.graph.processing.index.Index;
import org.kie.workbench.common.stunner.core.i18n.CoreTranslationMessages;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class DMNEditBusinessKnowledgeModelToolboxActionTest {

    private static final String E_UUID = "e1";

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private Index<?, ?> graphIndex;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private ClientSession session;

    @Mock
    private ClientTranslationService translationService;

    @Mock
    private EventSourceMock<EditExpressionEvent> editExpressionEvent;

    private DMNEditBusinessKnowledgeModelToolboxAction tested;
    private BusinessKnowledgeModel bkm;
    private FunctionDefinition bkmFunction;
    private Node<View<BusinessKnowledgeModel>, Edge> bkmNode;

    @Before
    public void setup() throws Exception {
        bkmNode = new NodeImpl<>(E_UUID);
        bkm = new BusinessKnowledgeModel();
        bkmFunction = new FunctionDefinition();
        bkm.setEncapsulatedLogic(bkmFunction);
        final Bounds bounds = new BoundsImpl(new BoundImpl(0d,
                                                           0d),
                                             new BoundImpl(100d,
                                                           150d));
        final View<BusinessKnowledgeModel> nodeContent = new ViewImpl<>(bkm,
                                                                        bounds);
        bkmNode.setContent(nodeContent);

        when(canvasHandler.getGraphIndex()).thenReturn(graphIndex);
        when(graphIndex.get(eq(E_UUID))).thenReturn(bkmNode);
        when(sessionManager.getCurrentSession()).thenReturn(session);

        this.tested = new DMNEditBusinessKnowledgeModelToolboxAction(sessionManager,
                                                                     translationService,
                                                                     editExpressionEvent);
    }

    @Test
    public void testTitle() {
        tested.getTitle(canvasHandler,
                        E_UUID);
        verify(translationService,
               times(1)).getValue(eq(CoreTranslationMessages.EDIT));
    }

    @Test
    public void testAction() {
        final MouseClickEvent event = mock(MouseClickEvent.class);
        ToolboxAction<AbstractCanvasHandler> cascade = tested.onMouseClick(canvasHandler,
                                                                           E_UUID,
                                                                           event);
        assertEquals(tested,
                     cascade);

        final ArgumentCaptor<EditExpressionEvent> eventCaptor = ArgumentCaptor.forClass(EditExpressionEvent.class);
        verify(editExpressionEvent,
               times(1)).fire(eventCaptor.capture());

        final EditExpressionEvent editExprEvent = eventCaptor.getValue();
        assertEquals(bkmFunction,
                     editExprEvent.getHasExpression());
        assertEquals(bkm,
                     editExprEvent.getHasName().get());
        assertEquals(session,
                     editExprEvent.getSession());
    }
}
