/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties;

import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.AssociationDeclaration;

public class OutputAssignmentReader {

    private final AssociationDeclaration associationDeclaration;

    public static OutputAssignmentReader fromAssociation(DataOutputAssociation out) {
        String sourceName = ((DataOutput) out.getSourceRef().get(0)).getName();
        if (out.getTargetRef() instanceof Property) {
            return new OutputAssignmentReader(sourceName, (Property) out.getTargetRef());
        } else if (out.getTargetRef().getId() != null) {
            //OJO, eso lo inventé para que funcionara bien el proceso de BPMN_LEGACY_MI generado en jBPM Designer
            //que basicamente NO me leia el nombre de la variable de salida xq basicamente no existe la property
            //PERO OJO, tengo q ver que no me joda otra cosa.......
            //tengo que jugar un poco con los otros casos, UserTaskMI, ReusableSubprocess MI, etc.
            return new OutputAssignmentReader(sourceName, out.getTargetRef().getId());
        }
        return null;
    }

    OutputAssignmentReader(String sourceName, Property target) {
        String propertyName = getPropertyName(target);
        this.associationDeclaration = new AssociationDeclaration(
                AssociationDeclaration.Direction.Output,
                AssociationDeclaration.Type.SourceTarget,
                sourceName,
                propertyName);
    }

    OutputAssignmentReader(String sourceName, String target) {
        this.associationDeclaration = new AssociationDeclaration(
                AssociationDeclaration.Direction.Output,
                AssociationDeclaration.Type.SourceTarget,
                sourceName,
                target);
    }

    public AssociationDeclaration getAssociationDeclaration() {
        return associationDeclaration;
    }

    // fallback to ID for https://issues.jboss.org/browse/JBPM-6708
    private static String getPropertyName(Property prop) {
        return prop.getName() == null ? prop.getId() : prop.getName();
    }
}
