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

package org.jetbrains.jet.lang.resolve.java.structure.impl;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.resolve.java.structure.JavaArrayType;
import org.jetbrains.jet.lang.resolve.java.structure.JavaType;

public abstract class JavaTypeImpl<Psi extends PsiType> implements JavaType {
    private final Psi psiType;

    public JavaTypeImpl(@NotNull Psi psiType) {
        this.psiType = psiType;
    }

    @NotNull
    public Psi getPsi() {
        return psiType;
    }

    @NotNull
    public static JavaTypeImpl<?> create(@NotNull PsiType psiType) {
        return psiType.accept(new PsiTypeVisitor<JavaTypeImpl<?>>() {
            @Nullable
            @Override
            public JavaTypeImpl<?> visitType(PsiType type) {
                throw new UnsupportedOperationException("Unsupported PsiType: " + type);
            }

            @Nullable
            @Override
            public JavaTypeImpl<?> visitPrimitiveType(PsiPrimitiveType primitiveType) {
                return new JavaPrimitiveTypeImpl(primitiveType);
            }

            @Nullable
            @Override
            public JavaTypeImpl<?> visitArrayType(PsiArrayType arrayType) {
                return new JavaArrayTypeImpl(arrayType);
            }

            @Nullable
            @Override
            public JavaTypeImpl<?> visitClassType(PsiClassType classType) {
                return new JavaClassifierTypeImpl(classType);
            }

            @Nullable
            @Override
            public JavaTypeImpl<?> visitWildcardType(PsiWildcardType wildcardType) {
                return new JavaWildcardTypeImpl(wildcardType);
            }
        });
    }

    @NotNull
    @Override
    public JavaArrayType createArrayType() {
        return new JavaArrayTypeImpl(getPsi().createArrayType());
    }

    @Override
    public int hashCode() {
        return getPsi().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JavaTypeImpl && getPsi().equals(((JavaTypeImpl) obj).getPsi());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getPsi();
    }
}
