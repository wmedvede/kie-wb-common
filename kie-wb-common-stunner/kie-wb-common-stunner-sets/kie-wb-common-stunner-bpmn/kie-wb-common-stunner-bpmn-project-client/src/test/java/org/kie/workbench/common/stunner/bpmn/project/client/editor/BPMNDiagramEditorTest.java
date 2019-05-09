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

package org.kie.workbench.common.stunner.bpmn.project.client.editor;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.project.client.editor.AbstractProjectDiagramEditorTest;

@RunWith(GwtMockitoTestRunner.class)
public class BPMNDiagramEditorTest extends AbstractProjectDiagramEditorTest {

    //TODO , WM review this test
    /*
    private static final String MIGRATE_ACTION_TITLE = "MIGRATE_ACTION_TITLE";
    private static final String MIGRATE_ACTION_WARNING = "MIGRATE_ACTION_WARNING";
    private static final String MIGRATE_ACTION = "MIGRATE_ACTION";
    private static final String MIGRATE_CONFIRM_ACTION = "MIGRATE_CONFIRM_ACTION";
    private static final String COMMIT_MESSAGE = "COMMIT_MESSAGE";

    @Mock
    private EventSourceMock<BPMNMigrateDiagramEvent> migrateDiagramEvent;

    @Mock
    private PopupUtil popupUtil;

    @Mock
    private PlaceRequest currentPlace;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private BPMNProjectEditorMenuSessionItems bpmnMenuSessionItems;

    private ArgumentCaptor<Command> commandCaptor;

    private ArgumentCaptor<BPMNMigrateDiagramEvent> migrateDiagramEventCaptor;

    private ArgumentCaptor<ClientSessionCommand.Callback> sessionCommandCallback;

    private ArgumentCaptor<ParameterizedCommand> parameterizedCommandCaptor;

    private ArgumentCaptor<ServiceCallback> serviceCallbackCaptor;

    private BPMNDiagramEditor diagramEditor;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        super.setUp();
        commandCaptor = ArgumentCaptor.forClass(Command.class);
        migrateDiagramEventCaptor = ArgumentCaptor.forClass(BPMNMigrateDiagramEvent.class);
        sessionCommandCallback = ArgumentCaptor.forClass(ClientSessionCommand.Callback.class);
        parameterizedCommandCaptor = ArgumentCaptor.forClass(ParameterizedCommand.class);
        serviceCallbackCaptor = ArgumentCaptor.forClass(ServiceCallback.class);
        when(canvasHandler.getDiagram()).thenReturn(diagram);

        when(translationService.getValue(BPMNClientConstants.EditorMigrateActionTitle)).thenReturn(MIGRATE_ACTION_TITLE);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateActionWarning)).thenReturn(MIGRATE_ACTION_WARNING);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateAction)).thenReturn(MIGRATE_ACTION);
        when(translationService.getValue(BPMNClientConstants.EditorMigrateConfirmAction)).thenReturn(MIGRATE_CONFIRM_ACTION);

        super.setUp();
    }

    @Override
    protected AbstractProjectEditorMenuSessionItems getMenuSessionItems() {
        return bpmnMenuSessionItems;
    }

    @Override
    protected BPMNDiagramResourceType mockResourceType() {
        final BPMNDiagramResourceType resourceType = mock(BPMNDiagramResourceType.class);
        when(resourceType.getSuffix()).thenReturn("bpmn");
        when(resourceType.getShortName()).thenReturn("Business Process");
        return resourceType;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractProjectDiagramEditor createDiagramEditor() {
        //TODO WM, REVIEW THIS TEST
        diagramEditor = spy(new BPMNDiagramEditor(view,
                                                  documentationView,
                                                  placeManager,
                                                  errorPopupPresenter,
                                                  changeTitleNotificationEvent,
                                                  savePopUpPresenter,
                                                  (BPMNDiagramResourceType) getResourceType(),
                                                  clientProjectDiagramService,
                                                  sessionEditorPresenters,
                                                  sessionViewerPresenters,
                                                  bpmnMenuSessionItems,
                                                  onDiagramFocusEvent,
                                                  onDiagramLostFocusEvent,
                                                  projectMessagesListener,
                                                  diagramClientErrorHandler,
                                                  translationService,
                                                  projectDiagramResourceServiceCaller,
                                                  null,
                                                  xmlEditorView) {
            {
                docks = defaultEditorDock;
                perspectiveManager = perspectiveManagerMock;
                fileMenuBuilder = BPMNDiagramEditorTest.this.fileMenuBuilder;
                workbenchContext = BPMNDiagramEditorTest.this.workbenchContext;
                projectController = BPMNDiagramEditorTest.this.projectController;
                versionRecordManager = BPMNDiagramEditorTest.this.versionRecordManager;
                alertsButtonMenuItemBuilder = BPMNDiagramEditorTest.this.alertsButtonMenuItemBuilder;
                place = BPMNDiagramEditorTest.this.currentPlace;
                kieView = BPMNDiagramEditorTest.this.kieView;
                overviewWidget = BPMNDiagramEditorTest.this.overviewWidget;
                notification = BPMNDiagramEditorTest.this.notification;
            }

            @Override
            protected void log(Level level,
                               String message) {
                //avoid GWT log initialization.
            }
        });
        return diagramEditor;
    }
*/
    //TODO WM, check this
    /*
    @Test
    public void testMigrateWhenNotDirty() {
        ObservablePath currentPath = mock(ObservablePath.class);
        when(versionRecordManager.getCurrentPath()).thenReturn(currentPath);
        doReturn(false).when(diagramEditor).isDirty(any(Integer.class));

        diagramEditor.onMigrate();
        verify(popupUtil,
               times(1)).showConfirmPopup(eq(MIGRATE_ACTION_TITLE),
                                          eq(MIGRATE_ACTION_WARNING),
                                          eq(InlineNotification.InlineNotificationType.WARNING),
                                          eq(MIGRATE_ACTION),
                                          eq(Button.ButtonStyleType.PRIMARY),
                                          eq(MIGRATE_CONFIRM_ACTION),
                                          commandCaptor.capture());
        commandCaptor.getValue().execute();
        verify(migrateDiagramEvent,
               times(1)).fire(migrateDiagramEventCaptor.capture());
        assertEquals(currentPath,
                     migrateDiagramEventCaptor.getValue().getSourcePath());
        assertEquals(currentPlace,
                     migrateDiagramEventCaptor.getValue().getSourcePlace());
    }
*/

    /*
    @SuppressWarnings("unchecked")
    @Test
    public void testMigrateWhenDirty() {
        ObservablePath currentPath = mock(ObservablePath.class);
        when(versionRecordManager.getCurrentPath()).thenReturn(currentPath);
        doReturn(true).when(diagramEditor).isDirty(any(Integer.class));
        diagramEditor.onMigrate();
        verify(popupUtil,
               times(1)).showConfirmPopup(eq(MIGRATE_ACTION_TITLE),
                                          eq(MIGRATE_ACTION_WARNING),
                                          eq(InlineNotification.InlineNotificationType.WARNING),
                                          eq(MIGRATE_ACTION),
                                          eq(Button.ButtonStyleType.PRIMARY),
                                          eq(MIGRATE_CONFIRM_ACTION),
                                          commandCaptor.capture());
        commandCaptor.getValue().execute();
        verify(diagramEditor,
               times(1)).saveAndMigrate();
    }
    */
}
