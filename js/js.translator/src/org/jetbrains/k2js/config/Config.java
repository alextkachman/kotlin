/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.k2js.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.translate.test.JSTester;
import org.jetbrains.k2js.translate.test.QUnitTester;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class representing a configuration of translator.
 */
public abstract class Config {
    @NotNull
    public static Config getEmptyConfig(@NotNull Project project, @NotNull EcmaVersion ecmaVersion) {
        return new Config(project, "main", ecmaVersion) {
            @NotNull
            @Override
            protected List<JetFile> generateLibFiles() {
                return Collections.emptyList();
            }
        };
    }

    //NOTE: used by mvn build
    @SuppressWarnings("UnusedDeclaration")
    @NotNull
    public static Config getEmptyConfig(@NotNull Project project) {
        return getEmptyConfig(project, EcmaVersion.defaultVersion());
    }

    @NotNull
    private final Project project;
    @Nullable
    private List<JetFile> libFiles = null;
    @NotNull
    private final EcmaVersion target;

    @NotNull
    private final String moduleId;

    private final boolean sourcemap;

    public Config(@NotNull Project project, @NotNull String moduleId, @NotNull EcmaVersion ecmaVersion) {
        this(project, moduleId, ecmaVersion, false);
    }

    public Config(@NotNull Project project, @NotNull String moduleId, @NotNull EcmaVersion ecmaVersion, boolean sourcemap) {
        this.project = project;
        this.target = ecmaVersion;
        this.moduleId = moduleId;
        this.sourcemap = sourcemap;
    }

    public boolean isSourcemap() {
        return sourcemap;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    @NotNull
    public EcmaVersion getTarget() {
        return target;
    }

    @NotNull
    public String getModuleId() {
        return moduleId;
    }

    @NotNull
    protected abstract List<JetFile> generateLibFiles();

    @NotNull
    public final List<JetFile> getLibFiles() {
        if (libFiles == null) {
            libFiles = generateLibFiles();
        }
        return libFiles;
    }

    @Nullable
    public BindingContext getLibraryBindingContext() {
        return null;
    }

    @NotNull
    public static Collection<JetFile> withJsLibAdded(@NotNull Collection<JetFile> files, @NotNull Config config) {
        Collection<JetFile> allFiles = Lists.newArrayList();
        allFiles.addAll(files);
        allFiles.addAll(config.getLibFiles());
        return allFiles;
    }

    //TODO: should be null by default I suppose but we can't communicate it to K2JSCompiler atm
    @Nullable
    public JSTester getTester() {
        return new QUnitTester();
    }
}
