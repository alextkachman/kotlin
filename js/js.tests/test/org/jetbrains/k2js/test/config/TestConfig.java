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

package org.jetbrains.k2js.test.config;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.config.EcmaVersion;

import java.util.Arrays;
import java.util.List;

public class TestConfig extends Config {
    //NOTE: a hacky solution to be able to rerun code samples with lib loaded only once: used by tests and web demo
    @NotNull
    public static final String REWRITABLE_MODULE_NAME = "JS_TESTS";

    @NotNull
    public static final List<String> LIB_FILES_WITH_DECLARATIONS = Arrays.asList(
            "/core/annotations.kt",
            "/core/core.kt",
            "/core/date.kt",
            "/core/dom.kt",
            "/core/javaio.kt",
            "/core/javalang.kt",
            "/core/javautil.kt",
            "/core/javautilCollections.kt",
            "/core/json.kt",
            "/core/kotlin.kt",
            "/core/math.kt",
            "/core/string.kt",
            "/core/htmlDom.kt",
            "/html5/canvas.kt",
            "/jquery/common.kt",
            "/jquery/ui.kt",
            "/junit/core.kt",
            "/qunit/core.kt",
            "/stdlib/browser.kt"
    );

    @NotNull
    public static final List<String> LIB_FILES_WITH_CODE = Arrays.asList(
            "/stdlib/TuplesCode.kt",
            "/core/javautilCollectionsCode.kt"
    );

    @NotNull
    public static final List<String> LIB_FILE_NAMES = Lists.newArrayList();

    static {
        LIB_FILE_NAMES.addAll(LIB_FILES_WITH_DECLARATIONS);
        LIB_FILE_NAMES.addAll(LIB_FILES_WITH_CODE);
    }

    /**
     * the library files which depend on the STDLIB files to be able to compile
     */
    @NotNull
    public static final List<String> LIB_FILE_NAMES_DEPENDENT_ON_STDLIB = Arrays.asList(
            "/core/stringsCode.kt",
            "/stdlib/domCode.kt",
            "/stdlib/jutilCode.kt",
            "/stdlib/JUMapsCode.kt",
            "/stdlib/testCode.kt"
    );

    public static final String LIBRARIES_LOCATION = "js/js.libraries/src";

    /**
     * The file names in the standard library to compile
     */
    @NotNull
    public static final List<String> STDLIB_FILE_NAMES = Arrays.asList(
            "/kotlin/Preconditions.kt",
            "/kotlin/Iterators.kt",
            "/kotlin/JUtil.kt",
            "/kotlin/Arrays.kt",
            "/kotlin/Lists.kt",
            "/kotlin/Maps.kt",
            "/kotlin/Exceptions.kt",
            "/kotlin/IterablesSpecial.kt",
            "/generated/_Arrays.kt",
            "/generated/_Collections.kt",
            "/generated/_Iterables.kt",
            "/generated/_Iterators.kt",
            "/kotlin/support/AbstractIterator.kt",
            "/kotlin/Standard.kt",
            "/kotlin/Strings.kt",
            "/kotlin/dom/Dom.kt",
            "/kotlin/test/Test.kt"
    );

    /**
     * The location of the stdlib sources
     */
    public static final String STDLIB_LOCATION = "libraries/stdlib/src";

    @NotNull
    public static TestConfigFactory FACTORY_WITHOUT_SOURCEMAP = new TestConfigFactory() {
        @Override
        public TestConfig create(@NotNull Project project,
                @NotNull EcmaVersion version,
                @NotNull List<JetFile> files,
                @NotNull BindingContext context) {
            return new TestConfig(project, version, files, context, false);
        }
    };

    public static TestConfigFactory FACTORY_WITH_SOURCEMAP = new TestConfigFactory() {
        @Override
        public TestConfig create(@NotNull Project project,
                @NotNull EcmaVersion version,
                @NotNull List<JetFile> files,
                @NotNull BindingContext context) {
            return new TestConfig(project, version, files, context, true);
        }
    };

    @NotNull
    private final List<JetFile> jsLibFiles;
    @NotNull
    private final BindingContext libraryContext;

    public TestConfig(@NotNull Project project, @NotNull EcmaVersion version,
            @NotNull List<JetFile> files, @NotNull BindingContext context, boolean sourcemap) {
        super(project, REWRITABLE_MODULE_NAME, version, sourcemap);
        jsLibFiles = files;
        libraryContext = context;
    }

    @Override
    public BindingContext getLibraryBindingContext() {
        return libraryContext;
    }

    @Override
    @NotNull
    public List<JetFile> generateLibFiles() {
        return jsLibFiles;
    }
}
