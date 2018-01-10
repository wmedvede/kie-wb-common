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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.scriptEditor;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Option;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.common.client.dom.TextArea;
import org.jboss.errai.common.client.dom.Window;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.commons.data.Pair;

@Templated
public class ScriptTypeFieldEditorView
        implements IsElement,
                   ScriptTypeFieldEditorPresenter.View {

    private static final String NOT_SELECTED = "NOT_SELECTED";

    @Inject
    @DataField("language")
    private Select language;

    @Inject
    @DataField("script")
    private TextArea script;

    private ScriptTypeFieldEditorPresenter presenter;

    @Override
    public void init(ScriptTypeFieldEditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setScript(String script) {
        this.script.setValue(script);
    }

    @Override
    public String getScript() {
        return script.getValue();
    }

    @Override
    public void setLanguage(String language) {
        if (language != null) {
            this.language.setValue(language);
        } else {
            this.language.setValue(NOT_SELECTED);
        }
    }

    @Override
    public String getLanguage() {
        String value = language.getValue();
        return NOT_SELECTED.equals(value) ? null : value;
    }

    @Override
    public void setLanguageOptions(List<Pair<String, String>> options,
                                   String selectedValue) {
        clearSelect(language);
        options.forEach(option ->
                                language.add(newOption(option.getK1(),
                                                       option.getK2())));
        setLanguage(selectedValue);
    }

    private Option newOption(final String text,
                             final String value) {
        final Option option = (Option) Window.getDocument().createElement("option");
        option.setTextContent(text);
        option.setValue(value);
        return option;
    }

    private void clearSelect(Select select) {
        int options = select.getOptions().getLength();
        for (int i = 0; i < options; i++) {
            select.remove(0);
        }
        select.add(defaultOption());
    }

    private HTMLElement defaultOption() {
        final HTMLElement option = Window.getDocument().createElement("option");
        option.setAttribute("value",
                            NOT_SELECTED);
        option.setAttribute("disabled",
                            "");
        option.setAttribute("selected",
                            "");
        option.setTextContent("Select the script language");
        return option;
    }

    @EventHandler("language")
    private void onLanguageChange(@ForEvent("change") final Event event) {
        presenter.onLanguageChange();
    }

    @EventHandler("script")
    private void onScriptChange(@ForEvent("change") final Event event) {
        presenter.onScriptChange();
    }
}
