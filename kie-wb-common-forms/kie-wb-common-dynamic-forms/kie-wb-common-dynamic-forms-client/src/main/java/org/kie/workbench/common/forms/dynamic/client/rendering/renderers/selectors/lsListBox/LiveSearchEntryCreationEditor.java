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

import javax.inject.Inject;

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.uberfire.ext.widgets.common.client.dropdown.InlineCreationEditor;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchEntry;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

public class LiveSearchEntryCreationEditor implements InlineCreationEditor<String>,
                                                      LiveSearchEntryCreationEditorView.Presenter {

    private TranslationService translationService;
    private LiveSearchEntryCreationEditorView view;

    private ParameterizedCommand<LiveSearchEntry<String>> okCommand;
    private Command cancelCommand;

    private ParameterizedCommand<String> customEntryCommand;

    @Inject
    public LiveSearchEntryCreationEditor(LiveSearchEntryCreationEditorView view, TranslationService translationService) {
        this.view = view;
        this.translationService = translationService;

        view.init(this);
    }

    public void setCustomEntryCommand(ParameterizedCommand<String> customEntryCommand) {
        this.customEntryCommand = customEntryCommand;
    }

    @Override
    public void init(ParameterizedCommand<LiveSearchEntry<String>> okCommand, Command cancelCommand) {
        this.okCommand = okCommand;
        this.cancelCommand = cancelCommand;
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void onAccept() {
        String value = view.getValue();
        if (isValid(value)) {
            customEntryCommand.execute(value);
            okCommand.execute(new LiveSearchEntry<>(value, value));
        }
    }

    @Override
    public void onCancel() {
        view.clear();
        cancelCommand.execute();
    }

    private boolean isValid(String value) {
        view.clearErrors();

        if (value == null || value.isEmpty()) {
            //TODO WM se the proper constant here
            view.showError(translationService.getTranslation("StunnerBPMNConstants.ASSIGNEE_CANNOT_BE_EMPTY"));
            return false;
        }

        return true;
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    @Override
    public String getFieldLabel() {
        //TODO WM set the proper constant here
        return translationService.getTranslation("StunnerBPMNConstants.ASSIGNEE_LABEL");
    }
}

