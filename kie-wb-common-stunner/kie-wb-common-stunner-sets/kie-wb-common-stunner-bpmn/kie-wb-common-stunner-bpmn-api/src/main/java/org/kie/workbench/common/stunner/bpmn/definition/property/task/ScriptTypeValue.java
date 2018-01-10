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

package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
@Bindable
public class ScriptTypeValue {

    private String language = null;

    private String script = null;

    public ScriptTypeValue() {
    }

    public ScriptTypeValue(@MapsTo("language") final String language,
                           @MapsTo("script") final String script) {
        this.language = language;
        this.script = script;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScriptTypeValue that = (ScriptTypeValue) o;

        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        return script != null ? script.equals(that.script) : that.script == null;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(language != null ? language.hashCode() : 0,
                                         script != null ? script.hashCode() : 0);
    }
}
