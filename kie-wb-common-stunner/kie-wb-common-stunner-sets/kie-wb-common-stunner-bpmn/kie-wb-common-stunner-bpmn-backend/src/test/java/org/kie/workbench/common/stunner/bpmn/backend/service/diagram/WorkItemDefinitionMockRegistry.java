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

package org.kie.workbench.common.stunner.bpmn.backend.service.diagram;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.workbench.common.stunner.bpmn.workitem.WorkItemDefinition;
import org.kie.workbench.common.stunner.bpmn.workitem.WorkItemDefinitionRegistry;

/*
        TODO: Remove
        assignments raw examples:
        -------------------------
        |inputs||outputs|
        |inputs|||
        |input1:String,input2:Float||output1:String,output2:Float|[din]pv1->input1,[din]pv2->input2,[dout]output1->pv1,[dout]output2->pv2
        ||errorOutput_:String||[dout]errorOutput_->var1
        _input1:String||||[din]var1->_input1
        IntermediateMessageEventThrowingInputVar1:String||||[din]var1->IntermediateMessageEventThrowingInputVar1
        EMAIL
        *****
        |From:String,Subject:String,To:String,Body:String|||
        |From:String,Subject:String,To:String,Body:String|||[din]From=vfrom,[din]Subject=vsubject,[din]To=vto,[din]Body=vbody
 */

class WorkItemDefinitionMockRegistry implements WorkItemDefinitionRegistry {

    public static final String WID_EMAIL = "Email";
    public static final String WID_LOG = "Log";
    private static final String EMAIL_ICON_DATA = "data:image/png;base64,R0lGODlhEAAQANUAAChilmd9qW2DrXeMtJiYkZuajqGeiqZrEKehh6m30qyjhK1yErCmgbOpfrZ8FLmter2EFr+wd8HG2ca0ceDq9+Ps+Ojv+Ovx+fL1+vb4+/j5/Pvll/vusPvyufz62/797wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAACAALAAAAAAQABAAAAaAQJBwSCwaJ8ikclLUOJ9QJtEpqVolGekQAsl4v16tEPKBYKpnCSYC4ro/ZYx8/oB47vi7GcDHPBwdgYKBHA4DAgEXDQsbjY6NCxd8ABcMIAeYmI0HFp2eCkUHGwcVCQmlpwihpBUVFK2vBkWtprWmFbJEFK+7rrsUBUUEw8TFBUEAOw==";
    private static final String LOG_ICON_DATA = "data:image/png;base64,R0lGODlhEAAQAMQAAG+Fr3CFr3yRuIOSsYaUroidwIuWrI+ZqJGlx5WdpZugoKGknaeomK6slLKvkL21idSyaNq9fN3o+ODIj+Ps+evx+vP2+/f4+/n6/AAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABkALAAAAAAQABAAAAVlYCaOZEk+aIqa4oO9MPSwLvxGsvlcwYUglwluRnJYjkiko9SwBCy/guDZKDEq2GyWUVpUApXotLIoKSjodFpRSlACFDGAkigdJHg8Gn8oGSQBEnISBiUEeYh4BCUDjY6PAyySIyEAOw==";
    public static final WorkItemDefinition EMAIL =
            new WorkItemDefinition()
                    .setName(WID_EMAIL)
                    .setCategory("Communication")
                    .setDescription("Email task")
                    .setDisplayName("Email")
                    .setDocumentation("index.html")
                    .setIconData(EMAIL_ICON_DATA)
                    .setDefaultHandler("org.jbpm.process.workitem.email.EmailWorkItemHandler")
                    .setParameters("|From:String,Subject:String,To:String,Body:String|||");
    public static final WorkItemDefinition LOG =
            new WorkItemDefinition()
                    .setName(WID_LOG)
                    .setCategory("Log")
                    .setDescription("Log task")
                    .setDisplayName("Log")
                    .setDocumentation("index.html")
                    .setIconData(LOG_ICON_DATA)
                    .setDefaultHandler("org.jbpm.process.workitem.log.LogWorkItemHandler")
                    .setParameters("|Message:String|||");

    public static final Map<String, WorkItemDefinition> MOCK_DEFINITIONS = new HashMap<String, WorkItemDefinition>(2) {{
        put(WID_EMAIL, EMAIL);
        put(WID_LOG, LOG);
    }};

    @Override
    public Collection<WorkItemDefinition> items() {
        return MOCK_DEFINITIONS.values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public WorkItemDefinition get(final String name) {
        return MOCK_DEFINITIONS.get(name);
    }
}
