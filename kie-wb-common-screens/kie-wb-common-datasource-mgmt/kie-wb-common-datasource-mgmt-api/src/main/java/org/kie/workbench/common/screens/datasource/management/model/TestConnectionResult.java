/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datasource.management.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TestConnectionResult {

    private boolean testPassed;

    private String message;

    public TestConnectionResult() {
    }

    public TestConnectionResult( boolean testPassed, String message ) {
        this.testPassed = testPassed;
        this.message = message;
    }

    public boolean isTestPassed() {
        return testPassed;
    }

    public void setTestPassed( boolean testPassed ) {
        this.testPassed = testPassed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }
}
