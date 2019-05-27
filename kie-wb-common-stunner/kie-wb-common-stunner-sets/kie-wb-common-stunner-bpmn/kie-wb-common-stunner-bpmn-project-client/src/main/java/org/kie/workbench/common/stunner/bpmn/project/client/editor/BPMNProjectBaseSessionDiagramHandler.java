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

package org.kie.workbench.common.stunner.bpmn.project.client.editor;

import java.util.logging.Logger;

import javax.enterprise.event.Event;

import org.kie.workbench.common.stunner.bpmn.project.client.type.BPMNDiagramResourceType;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.util.CanvasFileExport;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.service.ClientDiagramService;
import org.kie.workbench.common.stunner.core.client.service.ClientRuntimeError;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.client.session.event.SessionDiagramHandler;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.kie.workbench.common.stunner.project.diagram.ProjectMetadata;
import org.uberfire.backend.vfs.Path;
import org.uberfire.workbench.events.NotificationEvent;

public abstract class BPMNProjectBaseSessionDiagramHandler implements SessionDiagramHandler {

    private static Logger LOGGER = Logger.getLogger(BPMNProjectBaseSessionDiagramHandler.class.getName());

    private final BPMNDiagramResourceType bpmnDiagramResourceType;
    private final ClientDiagramService diagramService;
    private final CanvasFileExport canvasExport;
    private final Event<NotificationEvent> notificationEvent;
    private final ClientTranslationService translationService;

    public BPMNProjectBaseSessionDiagramHandler() {
        this(null, null, null, null, null);
    }

    public BPMNProjectBaseSessionDiagramHandler(final BPMNDiagramResourceType bpmnDiagramResourceType,
                                                final ClientDiagramService diagramService,
                                                final CanvasFileExport canvasExport,
                                                final Event<NotificationEvent> notificationEvent,
                                                final ClientTranslationService translationService) {
        this.bpmnDiagramResourceType = bpmnDiagramResourceType;
        this.diagramService = diagramService;
        this.canvasExport = canvasExport;
        this.notificationEvent = notificationEvent;
        this.translationService = translationService;
    }

    @Override
    public boolean accepts(final Diagram diagram) {
        return diagram instanceof ProjectDiagram && bpmnDiagramResourceType.accept(diagram.getMetadata().getPath());
    }

    protected void saveOrUpdateDiagram(final AbstractCanvasHandler canvasHandler) {
        final String rawSvg = canvasExport.exportToSvg(canvasHandler);
        final ProjectMetadata metadata = (ProjectMetadata) canvasHandler.getDiagram().getMetadata();
        if (metadata.getDiagramSVGPath() == null) {
            final Path path = canvasHandler.getDiagram().getMetadata().getPath();
            diagramService.saveOrUpdateSvg(path, rawSvg, new ServiceCallback<Path>() {
                @Override
                public void onSuccess(Path path) {
                    LOGGER.info("Diagram SVG saved on " + path);
                    notificationEvent.fire(new NotificationEvent("Guay, ido todo bien", NotificationEvent.NotificationType.SUCCESS));
                }

                @Override
                public void onError(ClientRuntimeError error) {
                    notificationEvent.fire(new NotificationEvent("Se jodio todo", NotificationEvent.NotificationType.ERROR));
                    LOGGER.severe("An error was produced when generating svg for diagram: " + path + ":" + error.getMessage());
                }
            });
        }
    }
}
