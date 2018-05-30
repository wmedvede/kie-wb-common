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

package org.kie.workbench.common.forms.dynamic.client.rendering.renderers.selectors.lsListBox;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TextInput;
import org.uberfire.ext.widgets.common.client.dropdown.InlineCreationEditor;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchEntry;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class LiveSearchListBoxEntryCreationEditor implements InlineCreationEditor<String> {

    @Inject
    private TextInput textInput;

    @Override
    public void clear() {
        textInput.setValue(null);
    }

    @Override
    public HTMLElement getElement() {
        return textInput;
    }

    @Override
    public void init(ParameterizedCommand<LiveSearchEntry<String>> okCommand,
                     Command cancelCommand) {
        //TODO WM
    }
}
