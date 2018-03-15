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

package org.kie.workbench.common.stunner.bpmn.client.shape.def;

import com.ait.lienzo.test.LienzoMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.bpmn.workitem.ServiceTask;
import org.kie.workbench.common.stunner.bpmn.workitem.WorkItemDefinitionRegistry;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundsImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.svg.client.shape.view.impl.SVGShapeViewImpl;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LienzoMockitoTestRunner.class)
public class ServiceTaskShapeDefTest {

    private static final BoundsImpl BOUNDS = BoundsImpl.build(0, 0, 111d, 121d);

    @Mock
    private WorkItemDefinitionRegistry registry;

    @Mock
    private View node;

    @Mock
    private SVGShapeViewImpl view;

    private ServiceTaskShapeDef tested;
    private ServiceTask task;

    @Before
    public void init() throws Exception {
        this.task = new ServiceTask();
        task.getDimensionsSet().getWidth().setValue(111d);
        task.getDimensionsSet().getHeight().setValue(121d);
        this.task.setName("task1");
        when(node.getDefinition()).thenReturn(task);
        when(node.getBounds()).thenReturn(BOUNDS);
        this.tested = new ServiceTaskShapeDef(() -> registry);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSizeHandler() {
        tested.newSizeHandler().handle(node, view);
        verify(view, times(1)).setSize(eq(111d),
                                       eq(121d));
    }

    // TODO

}
