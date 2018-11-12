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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParsingUtilsTest {

    @Test
    public void testParseJavaNameSuccessfulWithStopCharacters() throws Exception {
        char[] stopCharacters = {' ', '.', '(' };
        String[] expectedValues = {"_", "$", "_name", "_näme", "näme", "näme1"};
        String[] inputs = {"_    blabla", "$. more things", "_name(", "_näme(other stuff", "näme.ABCD", "näme1"};
        testParseJavaNameSuccessful(expectedValues, inputs, stopCharacters);
    }

    private void testParseJavaNameSuccessful(String[] expectedValues, String[] inputs, char[] stopCharacters) throws Exception {
        for(int i = 0; i < inputs.length; i++) {
            assertEquals(expectedValues[i], ParsingUtils.parseJavaName(inputs[i], 0, stopCharacters));
        }
    }
}
