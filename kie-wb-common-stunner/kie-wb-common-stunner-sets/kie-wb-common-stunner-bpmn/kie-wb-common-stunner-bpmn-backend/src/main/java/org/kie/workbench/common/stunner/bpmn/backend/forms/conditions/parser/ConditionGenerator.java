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

package org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser;

import java.lang.reflect.Method;

import org.drools.core.util.KieFunctions;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;

public class ConditionGenerator {

    public String generateScript(Condition condition) throws GenerateConditionException {
        final StringBuilder script = new StringBuilder();

        if (condition == null) {
            throw new GenerateConditionException(ConditionEditorErrors.MISSING_FUNCTION_ERROR);
        }

        if (!isValidFunction(condition.getFunction())) {
            throw new GenerateConditionException("Invalid function: " + condition.getFunction());
        }

        String function = condition.getFunction().trim();
        script.append(ConditionParser.KIE_FUNCTIONS + function);
        script.append("(");
        boolean first = true;
        for (String param : condition.getParameters()) {
            if (first) {
                //first parameter is always a process variable name.
                script.append(param);
                first = false;
            } else {
                //the other parameters are always string parameters.
                script.append(", ");
                script.append("\"" + escapeStringParam(param) + "\"");
            }
            if (param == null || param.isEmpty()) {
                //WM TODO ver si hago esto en realidad dejo poner null...
                throw new GenerateConditionException(ConditionEditorErrors.PARAMETER_NULL_EMPTY);
            }
        }
        script.append(")");
        return script.toString();
    }

    private String escapeStringParam(String param) {
        if (param == null) {
            return null;
        }
        StringBuilder escapedParam = new StringBuilder(param.length() * 2);
        char c;
        for (int i = 0; i < param.length(); i++) {
            c = param.charAt(i);
            switch (c) {
                case '"':
                    escapedParam.append('\\');
                    escapedParam.append('"');
                    break;
                case '\n':
                    escapedParam.append('\\');
                    escapedParam.append('n');
                    break;
                case '\\':
                    escapedParam.append('\\');
                    escapedParam.append('\\');
                    break;
                default:
                    escapedParam.append(c);
            }
        }
        return escapedParam.toString();
    }

    private boolean isValidFunction(String function) {
        if (function == null) {
            return false;
        }

        if (function.trim().isEmpty()) {
            return false;
        }

        function = function.trim();

        for (Method method : KieFunctions.class.getMethods()) {
            if (function.equals(method.getName())) {
                return true;
            }
        }

        return false;
    }
}
