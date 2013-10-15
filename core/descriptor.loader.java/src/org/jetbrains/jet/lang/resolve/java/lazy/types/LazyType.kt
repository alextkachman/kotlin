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

package org.jetbrains.jet.lang.resolve.java.lazy.types

import org.jetbrains.jet.lang.types.JetType
import org.jetbrains.jet.storage.StorageManager
import org.jetbrains.jet.lang.types.TypeConstructor
import org.jetbrains.jet.lang.types.TypeProjection
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.jet.lang.resolve.scopes.JetScope
import org.jetbrains.jet.lang.types.TypeUtils

abstract class LazyType(storageManager: StorageManager) : JetType {

    private val _typeConstructor = storageManager.createLazyValue {computeTypeConstructor()}
    override fun getConstructor(): TypeConstructor = _typeConstructor()

    protected abstract fun computeTypeConstructor(): TypeConstructor

    private val _arguments = storageManager.createLazyValue {computeArguments()}
    override fun getArguments(): List<TypeProjection> = _arguments()

    protected abstract fun computeArguments(): List<TypeProjection>

    private val _memberScope = storageManager.createLazyValue {computeMemberScope()}
    override fun getMemberScope() = _memberScope()

    protected abstract fun computeMemberScope(): JetScope

    override fun isNullable() = false

    override fun isError() = false

    override fun getAnnotations(): List<AnnotationDescriptor> = listOf()

    override fun equals(o: Any?): Boolean = TypeUtils.equals(this, o)

    override fun hashCode() = TypeUtils.hashCode(this)

    override fun toString() = TypeUtils.toString(this)
}
