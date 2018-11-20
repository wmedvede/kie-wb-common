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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;

public class ConditionParamPresenter {

    public interface View extends UberElement<ConditionParamPresenter> {

        void setName(String name);

        String getValue();

        void setValue(String value);

        void clear();

        void clearError();

        void setError(String error);
    }

    private Command onChangeCommand;

    private View view;

    @Inject
    public ConditionParamPresenter(View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public View getView() {
        return view;
    }

    public void setName(String name) {
        view.setName(name);
    }

    public String getValue() {
        return view.getValue();
    }

    public void setValue(String value) {
        view.setValue(value);
    }

    public void clear() {
        view.clear();
    }

    public void clearError() {
        view.clearError();
    }

    public void setError(String error) {
        view.setError(error);
    }

    public void setOnChangeCommand(Command onChangeCommand) {
        this.onChangeCommand = onChangeCommand;
    }

    public void onValueChange() {
        if (onChangeCommand != null) {
            onChangeCommand.execute();
        }
    }
}
