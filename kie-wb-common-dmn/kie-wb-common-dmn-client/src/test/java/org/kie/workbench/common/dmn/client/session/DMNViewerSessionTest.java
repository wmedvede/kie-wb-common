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

package org.kie.workbench.common.dmn.client.session;

import java.util.Collections;
import java.util.Map;

import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.canvas.controls.CanvasControl;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DMNViewerSessionTest extends BaseDMNSessionTest<DMNViewerSession> {

    @Override
    @SuppressWarnings("unchecked")
    protected DMNViewerSession getSession() {
        final DMNViewerSession session = new DMNViewerSession(managedSession,
                                                              canvasCommandManager);
        session.constructInstance();
        return session;
    }

    @Override
    protected Map<CanvasControl, Class> getCanvasControlRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<CanvasControl, Class> getCanvasHandlerControlRegistrations() {
        return Collections.emptyMap();
    }
}
