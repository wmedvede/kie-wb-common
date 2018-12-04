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

import java.util.Arrays;
import java.util.List;

public class ConditionTestCommons {

    public static final List<String> stringParams = Arrays.asList("_a",
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

    public static final List<String> unaryFunctions = Arrays.asList("isNull",
                                                                    "isEmpty",
                                                                    "isTrue",
                                                                    "isFalse");

    public static final List<String> binaryFunctions = Arrays.asList("equalsTo",
                                                                     "contains",
                                                                     "startsWith",
                                                                     "endsWith",
                                                                     "greaterThan",
                                                                     "greaterOrEqualThan",
                                                                     "lessThan",
                                                                     "lessOrEqualThan");

    public static final List<String> ternaryFunctions = Arrays.asList("between");

    public static final List<String> variableParams = Arrays.asList("_a",
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
}
