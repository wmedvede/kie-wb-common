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

package org.kie.workbench.common.stunner.bpmn.client.forms.util;

import java.util.List;

import org.jboss.errai.common.client.dom.Option;
import org.jboss.errai.common.client.dom.Select;
import org.jboss.errai.common.client.dom.Window;
import org.uberfire.commons.data.Pair;

public class SelectUtils {

    public static Option newOption(final String text,
                                   final String value) {
        final Option option = (Option) Window.getDocument().createElement("option");
        option.setTextContent(text);
        option.setValue(value);
        return option;
    }

    public static void setOptions(final Select select, final List<Pair<String, String>> options) {
        clear(select);
        options.forEach(option -> select.add(newOption(option.getK1(),
                                                       option.getK2())));
    }

    public static void clear(final Select select) {
        int options = select.getOptions().getLength();
        for (int i = 0; i < options; i++) {
            select.remove(0);
        }
    }
}
