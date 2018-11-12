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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionExpression;

import static org.junit.Assert.assertEquals;

public class ConditionParserTest {

    int _emroe;
    int $asdfs;
    int äwsome;

    private static final List<String> unaryFunctions = Arrays.asList("isNull",
                                                                     "isEmpty",
                                                                     "isTrue",
                                                                     "isFalse");

    private static final List<String> binaryFunctions = Arrays.asList("equalsTo",
                                                                      "contains",
                                                                      "startsWith",
                                                                      "endsWith",
                                                                      "greaterThan",
                                                                      "greaterOrEqualThan",
                                                                      "lessThan",
                                                                      "lessOrEqualThan");

    private static final List<String> ternaryFunctions = Arrays.asList("between");


    private static final List<String> variableParams = Arrays.asList("_a",
                                                                     "_a.getA()",
                                                                     "_ä",
                                                                     "_ä.getÄ()",
                                                                     "$a",
                                                                     "$a.getA()",
                                                                     "_someVar",
                                                                     "_someVar.getValue()",
                                                                     "_äwsomeVar1",
                                                                     "_äwsomeVar1.anotherMethod()",
                                                                     "$asdfs",
                                                                     "$asdfs.isMember()");

    private static final List<String> variableExpectedValues = variableParams.stream().map(value -> {
        if (value.endsWith("()")) {
            return value.substring(0,
                                   value.length() - 2);
        } else {
            return value;
        }
    }).collect(Collectors.toList());


    private static final List<String> stringParams = Arrays.asList("_a",
                                                                   "_a.getA()",
                                                                   "_ä",
                                                                   "_ä.getÄ()",
                                                                   "$a",
                                                                   "$a.getA()",
                                                                   "_someVar",
                                                                   "_someVar.getValue()",
                                                                   "_äwsomeVar1",
                                                                   "_äwsomeVar1.anotherMethod()",
                                                                   "$asdfs",
                                                                   "$asdfs.isMember()");

    @Test
    public void testUnaryFunctionsParsing() throws Exception {
        for (String function : unaryFunctions) {
            testUnaryFunctionParsing(function);
        }
    }

    @Test
    public void testBinaryFunctionsParsing() throws Exception {
        for (String function : binaryFunctions) {
            testBinaryFunctionParsing(function);
        }
    }

    @Test
    public void testTernaryFunctionsParsing() throws Exception {
        for (String function : ternaryFunctions) {
            testTernaryFunctionParsing(function);
        }
    }

    private void testUnaryFunctionParsing(String function) throws Exception {
        List<String> conditions = new ArrayList<>();
        for (String variableParam : variableParams) {
            conditions.add("return KieFunctions." + function + "(" + variableParam + ");");
        }

        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            ConditionExpression conditionExpression = parser.parse();
            assertEquals(function, conditionExpression.getConditions().get(0).getFunction());
            assertEquals(1, conditionExpression.getConditions().size());
            assertEquals(1, conditionExpression.getConditions().get(0).getParameters().size());
            assertEquals(variableExpectedValues.get(i), conditionExpression.getConditions().get(0).getParameters().get(0));
        }
    }

    private void testBinaryFunctionParsing(String function) throws Exception {
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < variableParams.size(); i++) {
            conditions.add("return KieFunctions." + function + "(" + variableParams.get(i) + ",\"" + stringParams.get(i) + "\");");
        }

        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            ConditionExpression conditionExpression = parser.parse();
            assertEquals(function, conditionExpression.getConditions().get(0).getFunction());
            assertEquals(1, conditionExpression.getConditions().size());
            assertEquals(2, conditionExpression.getConditions().get(0).getParameters().size());
            assertEquals(variableExpectedValues.get(i), conditionExpression.getConditions().get(0).getParameters().get(0));
            assertEquals(stringParams.get(i), conditionExpression.getConditions().get(0).getParameters().get(1));

        }
    }

    private void testTernaryFunctionParsing(String function) throws Exception {
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < variableParams.size(); i++) {
            conditions.add("return KieFunctions." + function + "(" + variableParams.get(i) + ",\"" + stringParams.get(i) + "\",\"" + stringParams.get(i) + "\");");
        }

        for (int i = 0; i < conditions.size(); i++) {
            ConditionParser parser = new ConditionParser(conditions.get(i));
            ConditionExpression conditionExpression = parser.parse();
            assertEquals(function, conditionExpression.getConditions().get(0).getFunction());
            assertEquals(1, conditionExpression.getConditions().size());
            assertEquals(3, conditionExpression.getConditions().get(0).getParameters().size());
            assertEquals(variableExpectedValues.get(i), conditionExpression.getConditions().get(0).getParameters().get(0));
            assertEquals(stringParams.get(i), conditionExpression.getConditions().get(0).getParameters().get(1));
            assertEquals(stringParams.get(i), conditionExpression.getConditions().get(0).getParameters().get(2));
        }
    }
}
