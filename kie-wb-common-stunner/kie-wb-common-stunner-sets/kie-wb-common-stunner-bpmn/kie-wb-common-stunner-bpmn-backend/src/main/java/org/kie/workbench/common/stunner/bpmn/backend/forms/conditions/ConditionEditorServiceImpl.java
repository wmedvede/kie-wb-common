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

package org.kie.workbench.common.stunner.bpmn.backend.forms.conditions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.workbench.common.services.backend.project.ModuleClassLoaderHelper;
import org.kie.workbench.common.services.shared.project.KieModule;
import org.kie.workbench.common.services.shared.project.KieModuleService;
import org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionGenerator;
import org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.ConditionParser;
import org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.FunctionsRegistry;
import org.kie.workbench.common.stunner.bpmn.backend.forms.conditions.parser.GenerateConditionException;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ConditionEditorService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.GenerateConditionResult;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParseConditionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.vfs.Path;

@Service
@ApplicationScoped
public class ConditionEditorServiceImpl implements ConditionEditorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEditorServiceImpl.class);

    private KieModuleService moduleService;

    private ModuleClassLoaderHelper moduleClassLoaderHelper;

    private ConditionEditorServiceImpl() {
        //Empty constructor for proxying
    }

    @Inject
    public ConditionEditorServiceImpl(KieModuleService moduleService,
                                      ModuleClassLoaderHelper moduleClassLoaderHelper) {
        this.moduleService = moduleService;
        this.moduleClassLoaderHelper = moduleClassLoaderHelper;
    }

    @Override
    public List<FunctionDef> getAvailableFunctions(Path path, String clazz) {
        //TODO
        //OJO!!! aca tengo q poner un ClassLoaderResolver a nivel de kie-wb-common-stunner-project
        //y otro a nivel standalone, asi esto funciona tanto en modo standalone como en modo PROJECT
        KieModule module = moduleService.resolveModule(path);
        ClassLoader classLoader = moduleClassLoaderHelper.getModuleClassLoader(module);
        Class resolvedClazz = null;
        try {
            resolvedClazz = classLoader.loadClass(clazz);
        } catch (ClassNotFoundException e) {
            resolvedClazz = Object.class;
            LOGGER.warn("Class: " + clazz + " was not properly resolved for module: " + module + " only java.lang.Object functions will be returned instead");
        }
        return getAvailableFunctions(resolvedClazz, classLoader);
    }

    private List<FunctionDef> getAvailableFunctions(Class<?> clazz, ClassLoader classLoader) {
        List<FunctionDef> result = new ArrayList<>();
        Class<?> paramClass;
        for (FunctionDef functionDef : FunctionsRegistry.getInstance().getFunctions()) {
            try {
                paramClass = classLoader.loadClass(functionDef.getParams().get(0).getType());
                if (paramClass.isAssignableFrom(clazz)) {
                    result.add(functionDef);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Uncommon error, internal function param type was not resolved: " + functionDef.getParams().get(0).getType());
            }
        }
        return result;
    }

    @Override
    public ParseConditionResult parseCondition(String conditionStr) {
        try {
            ConditionParser parser = new ConditionParser(conditionStr);
            return new ParseConditionResult(parser.parse());
        } catch (ParseException e) {
            return new ParseConditionResult(e.getMessage());
        }
    }

    @Override
    public GenerateConditionResult generateCondition(Condition condition) {
        ConditionGenerator generator = new ConditionGenerator();
        try {
            return new GenerateConditionResult(generator.generateScript(condition));
        } catch (GenerateConditionException e) {
            return new GenerateConditionResult(null, e.getMessage());
        }
    }
}
