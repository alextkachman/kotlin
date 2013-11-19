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

package org.jetbrains.jet.lang.resolve.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.FunctionAnalyzerExtension;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import java.util.List;

public class InlineAnalyzerExtension implements FunctionAnalyzerExtension.AnalyzerExtension {

    public InlineAnalyzerExtension() {

    }

    @Override
    public void process(
            @NotNull final SimpleFunctionDescriptor descriptor, @NotNull JetNamedFunction function, @NotNull final BindingTrace trace
    ) {

        checkDefaults(descriptor, function, trace);
        checkModality(descriptor, function, trace);
        checkHashInlinable(descriptor, function, trace);

        JetVisitorVoid visitor = new JetVisitorVoid() {

            @Override
            public void visitJetElement(JetElement element) {
                super.visitJetElement(element);
                element.acceptChildren(this);
            }

            @Override
            public void visitClass(JetClass klass) {
                trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(klass, klass, descriptor));
            }

            @Override
            public void visitObjectDeclaration(JetObjectDeclaration declaration) {
                trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(declaration, declaration, descriptor));
            }

            @Override
            public void visitNamedFunction(JetNamedFunction function) {
                trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(function, function, descriptor));
            }
        };

        function.acceptChildren(visitor);
    }

    private void checkDefaults(@NotNull SimpleFunctionDescriptor functionDescriptor, @NotNull JetFunction function, @NotNull BindingTrace trace) {
        List<JetParameter> parameters = function.getValueParameters();
        for (int i = 0; i < parameters.size(); i++) {
            JetParameter parameter =  parameters.get(i);
            if (parameter.getDefaultValue() != null) {
                trace.report(Errors.NOT_YET_SUPPORTED_IN_INLINE.on(parameter, parameter, functionDescriptor));
            }
        }
    }

    private void checkModality(
            @NotNull SimpleFunctionDescriptor functionDescriptor,
            @NotNull JetFunction function,
            @NotNull BindingTrace trace
    ) {
        if (functionDescriptor.getVisibility() == Visibilities.PRIVATE || functionDescriptor.getModality() == Modality.FINAL) {
            return;
        }

        DeclarationDescriptor declaration = functionDescriptor.getContainingDeclaration();
        if (declaration instanceof NamespaceDescriptor) {
            return;
        }

        trace.report(Errors.WRONG_MEMBER_MODALITY_FOR_INLINE.on(function));
    }

    private void checkHashInlinable(
            @NotNull SimpleFunctionDescriptor functionDescriptor,
            @NotNull JetFunction function,
            @NotNull BindingTrace trace
    ) {
        List<ValueParameterDescriptor> parameters = functionDescriptor.getValueParameters();
        for (ValueParameterDescriptor parameter : parameters) {
            KotlinBuiltIns builtIns = KotlinBuiltIns.getInstance();
            if (builtIns.isExactFunctionOrExtensionFunctionType(parameter.getType())) {
                if (!builtIns.hasNoinlineAnnotation(parameter)) {
                    return;
                }
            }
        }
        ReceiverParameterDescriptor receiverParameter = functionDescriptor.getReceiverParameter();
        if (receiverParameter != null) {
            if (KotlinBuiltIns.getInstance().isExactFunctionOrExtensionFunctionType(receiverParameter.getType())) {
                //or extension on inlinable function
                return;
            }
        }

        trace.report(Errors.NOTHING_TO_INLINE.on(function, functionDescriptor));
    }
}
