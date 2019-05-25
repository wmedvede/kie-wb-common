/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.client.widgets.event;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.diagram.Diagram;

@ApplicationScoped
public class SessionEventObserver {

    private List<SessionDiagramOpenedHandler> sessionDiagramOpenedHandlers = new ArrayList<>();

    public SessionEventObserver() {
        //proxying constructor
    }

    @Inject
    public SessionEventObserver(@Any final Instance<SessionDiagramOpenedHandler> sessionDiagramOpenedHandlersInstance) {
        sessionDiagramOpenedHandlersInstance.forEach(handler -> this.sessionDiagramOpenedHandlers.add(handler));
    }

    void onSessionDiagramOpenedEvent(@Observes final SessionDiagramOpenedEvent event) {
        final Diagram currentDiagram = event.getSession().getCanvasHandler().getDiagram();
        sessionDiagramOpenedHandlers.stream()
                .filter(handler -> handler.accepts(currentDiagram))
                .forEach(handler -> handler.onSessionDiagramOpened(event.getSession(), event.isReadonly()));
    }
}
