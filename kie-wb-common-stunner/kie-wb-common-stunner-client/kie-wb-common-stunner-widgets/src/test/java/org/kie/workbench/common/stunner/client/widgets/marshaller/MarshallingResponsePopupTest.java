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

package org.kie.workbench.common.stunner.client.widgets.marshaller;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.ListDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.marshaller.MarshallingMessage;
import org.kie.workbench.common.stunner.core.validation.Violation;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.views.pfly.widgets.InlineNotification;
import org.uberfire.mvp.Command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MarshallingResponsePopupTest {

    private static final int MESSAGE_COUNT = 10;
    private static final String MESSAGE_KEY = "MESSAGE_KEY";
    private static final String TITLE = "TITLE";
    private static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    private static final InlineNotification.InlineNotificationType NOTIFICATION_TYPE = InlineNotification.InlineNotificationType.INFO;
    private static final String OK_COMMAND_LABEL = "OK_COMMAND_LABEL";
    private static final String ERROR_TRANSLATION = "ERROR_TRANSLATION";
    private static final String INFO_TRANSLATION = "INFO_TRANSLATION";
    private static final String WARNING_TRANSLATION = "WARNING_TRANSLATION";
    private static final String MESSAGE_TRANSLATION = "MESSAGE_TRANSLATION";

    @Mock
    private MarshallingResponsePopup.View view;

    @Mock
    private ListDataProvider<MarshallingResponsePopup.Row> listDataProvider;

    @Mock
    private Command okCommand;

    private List<MarshallingResponsePopup.Row> rows;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private MarshallingResponsePopup popup;

    @Before
    public void setUp() {
        rows = spy(new ArrayList<>());
        when(listDataProvider.getList()).thenReturn(rows);
        when(view.getMessagesTableProvider()).thenReturn(listDataProvider);
        when(view.displayableValue(Violation.Type.ERROR)).thenReturn(ERROR_TRANSLATION);
        when(view.displayableValue(Violation.Type.INFO)).thenReturn(INFO_TRANSLATION);
        when(view.displayableValue(Violation.Type.WARNING)).thenReturn(WARNING_TRANSLATION);
        when(view.displayableValue(any(MarshallingMessage.class))).thenReturn(MESSAGE_TRANSLATION);

        popup = new MarshallingResponsePopup(view);
        popup.init();
        verify(view).init(popup);
    }

    @Test
    public void testShowWithCommand() {
        testShow(okCommand);
    }

    @Test
    public void testShowWithoutCommand() {
        testShow(null);
    }

    private void testShow(Command okCommand) {
        List<MarshallingMessage> messages = mockMessages();
        popup.show(TITLE, NOTIFICATION_MESSAGE, NOTIFICATION_TYPE, messages, OK_COMMAND_LABEL, okCommand);
        verify(view).setTitle(TITLE);
        verify(view).setInlineNotification(NOTIFICATION_MESSAGE, NOTIFICATION_TYPE);
        verify(rows).clear();
        verify(listDataProvider).flush();
        verify(view).setOkActionLabel(OK_COMMAND_LABEL);

        if (okCommand != null) {
            verify(view).setOkActionEnabled(true);
            verify(view).show(okCommand);
        } else {
            verify(view).setOkActionEnabled(false);
            verify(view).show(any(Command.class));
        }

        assertEquals(MESSAGE_COUNT, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            String expectedTypeTranslation = null;
            switch (i % 3) {
                case 0:
                    expectedTypeTranslation = ERROR_TRANSLATION;
                    break;
                case 1:
                    expectedTypeTranslation = WARNING_TRANSLATION;
                    break;
                case 2:
                    expectedTypeTranslation = INFO_TRANSLATION;
                    break;
            }
            assertEquals(expectedTypeTranslation, rows.get(i).getLevel());
            assertEquals(MESSAGE_TRANSLATION, rows.get(i).getMessage());
        }
    }

    @Test
    public void testOnCopyToClipboard() {
        List<MarshallingMessage> messages = mockMessages();
        popup.show(TITLE, NOTIFICATION_MESSAGE, NOTIFICATION_TYPE, messages, OK_COMMAND_LABEL, okCommand);
        StringBuilder expectedMessage = new StringBuilder();
        rows.forEach(row -> {
            if (expectedMessage.length() > 0) {
                expectedMessage.append("\n");
            }
            expectedMessage.append(row.getLevel());
            expectedMessage.append(", ");
            expectedMessage.append(row.getMessage());
        });
        popup.onCopyToClipboard();
        verify(view).copyToClipboard(stringCaptor.capture());
        assertEquals(expectedMessage.toString(), stringCaptor.getValue());
    }

    private List<MarshallingMessage> mockMessages() {
        List<MarshallingMessage> result = new ArrayList<>();
        Violation.Type type;
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            type = Violation.Type.values()[i % 3];
            result.add(new MarshallingMessage.MarshallingMessageBuilder()
                               .type(type)
                               .message(MESSAGE_KEY + i)
                               .messageArguments(new ArrayList<>())
                               .build());
        }
        return result;
    }
}
