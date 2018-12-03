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

package org.kie.workbench.common.stunner.core.client.util;

import java.util.Collection;

import org.kie.workbench.common.stunner.core.client.canvas.controls.select.SelectionControl;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.client.session.impl.ViewerSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Node;

public class ClientUtils {

    public static String getSelectedElementUUID(ClientSession clientSession) {
        SelectionControl selectionControl = null;
        if (clientSession instanceof EditorSession) {
            selectionControl = ((EditorSession) clientSession).getSelectionControl();
        } else if (clientSession instanceof ViewerSession) {
            selectionControl = ((ViewerSession) clientSession).getSelectionControl();
        }

        if (selectionControl != null) {
            @SuppressWarnings("unchecked")
            final Collection<String> selectedItems = selectionControl.getSelectedItems();
            if (selectedItems != null && !selectedItems.isEmpty()) {
                return selectedItems.iterator().next();
            }
        }
        return null;
    }

    public static Node getSelectedElement(Diagram diagram, ClientSession clientSession) {
        String uuid = getSelectedElementUUID(clientSession);
        return uuid != null ? diagram.getGraph().getNode(uuid) : null;
    }
}
