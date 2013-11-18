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

package org.jetbrains.jet.lang.resolve.calls;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.calls.context.BasicCallResolutionContext;
import org.jetbrains.jet.lang.resolve.calls.model.ExpressionValueArgument;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall;
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedValueArgument;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExpressionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ExtensionReceiver;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import java.util.*;

public class InlineCallResolverExtension implements CallResolverExtension {

    private SimpleFunctionDescriptor descriptor;

    private Set<DeclarationDescriptor> inlinableParameters = new HashSet<DeclarationDescriptor>();

    private final boolean isEffectivelyPublicApiFunction;

    public InlineCallResolverExtension(@NotNull SimpleFunctionDescriptor descriptor) {
        assert descriptor.isInline() : "This extension should be created only for inline functions but not " + descriptor;
        this.descriptor = descriptor;
        this.isEffectivelyPublicApiFunction = isEffectivelyPublicApi(descriptor);

        Iterator<ValueParameterDescriptor> iterator = descriptor.getValueParameters().iterator();
        while (iterator.hasNext()) {
            ValueParameterDescriptor next = iterator.next();
            JetType type = next.getType();
            if (KotlinBuiltIns.getInstance().isExactFunctionOrExtensionFunctionType(type)) {
                //TODO check annotations
                if (!KotlinBuiltIns.getInstance().hasNoinlineAnnotation(next)) {
                    inlinableParameters.add(next);
                }
            }
        }

        //add extension receiver as inlineable
        ReceiverParameterDescriptor receiverParameter = descriptor.getReceiverParameter();
        if (receiverParameter != null) {
            if (isExactFunctionOrExtensionFunctionType(receiverParameter.getType())) {
                inlinableParameters.add(descriptor);
                inlinableParameters.add(receiverParameter);
            }
        }
    }

    @Override
    public <F extends CallableDescriptor> void run(
            @NotNull ResolvedCall<F> resolvedCall, @NotNull BasicCallResolutionContext context
    ) {
        CallableDescriptor targetDescriptor = resolvedCall.getResultingDescriptor();
        JetExpression expression = context.call.getCalleeExpression();

        //checking that only invoke or inlinable extension called on closure
        List<ReceiverValue> thisOrReceivers = new ArrayList<ReceiverValue>();

        if (resolvedCall.getThisObject() != null) {
            thisOrReceivers.add(resolvedCall.getThisObject());
        }
        if (resolvedCall.getReceiverArgument() != null) {
            thisOrReceivers.add(resolvedCall.getReceiverArgument());
        }

        for (ReceiverValue receiver : thisOrReceivers) {
            checkCallWithReceiver(context, targetDescriptor, receiver, expression);
        }

        //test that ///
        boolean isInlinableClosure = inlinableParameters.contains(targetDescriptor);
        if (isInlinableClosure) {
            PsiElement parent = expression.getParent();
            if (parent instanceof JetValueArgument || parent instanceof JetBinaryExpression || parent instanceof JetDotQualifiedExpression || parent instanceof JetCallExpression) {
                //check that it's in inlineable call would be in resolve call of parent
            } else {
                context.trace.report(Errors.USAGE_IS_NOT_INLINABLE.on(expression, expression, descriptor));
            }
        }

        for (Map.Entry<ValueParameterDescriptor, ResolvedValueArgument> entry : resolvedCall.getValueArguments().entrySet()) {
            ResolvedValueArgument value = entry.getValue();
            if (value instanceof ExpressionValueArgument) {
                JetExpression jetExpression = ((ExpressionValueArgument) value).getValueArgument().getArgumentExpression();

                DeclarationDescriptor varDescriptor = getDescriptor(context, jetExpression);

                if (varDescriptor != null && inlinableParameters.contains(varDescriptor)) {
                    checkFunctionCall(context, targetDescriptor, jetExpression);
                }
            }
            //TODO default and vararg
        }

        checkVisibility(targetDescriptor, expression, context);
    }

    private void checkCallWithReceiver(
            @NotNull BasicCallResolutionContext context,
            @NotNull CallableDescriptor targetDescriptor,
            @NotNull ReceiverValue receiver,
            @NotNull JetExpression expression
    ) {

        DeclarationDescriptor varDescriptor = null;
        JetExpression receiverExpresssion = null;
        if (receiver instanceof ExpressionReceiver) {
            receiverExpresssion = ((ExpressionReceiver) receiver).getExpression();
            varDescriptor = getDescriptor(context, receiverExpresssion);
        } else if (receiver instanceof ExtensionReceiver) {
            ExtensionReceiver extensionReceiver = (ExtensionReceiver) receiver;
            varDescriptor = extensionReceiver.getDeclarationDescriptor();
            receiverExpresssion = expression;
        }

        if (varDescriptor != null) {
            if (inlinableParameters.contains(varDescriptor)) {
                //check that it's invoke
                checkFunctionCall(context, targetDescriptor, receiverExpresssion);
            }
        }
    }

    @Nullable
    private DeclarationDescriptor getDescriptor(
            @NotNull BasicCallResolutionContext context,
            @NotNull JetExpression expression
    ) {
        if (expression instanceof JetThisExpression) {
            return context.trace.get(BindingContext.REFERENCE_TARGET, ((JetThisExpression) expression).getInstanceReference());
        } else {
            ResolvedCall<? extends CallableDescriptor> thisCall = context.trace.get(BindingContext.RESOLVED_CALL, expression);
            if (thisCall != null) {
                return thisCall.getResultingDescriptor();
            }
        }
        return null;
    }

    private void checkFunctionCall(
            BasicCallResolutionContext context,
            CallableDescriptor targetDescriptor,
            JetExpression receiverExpresssion
    ) {
        if (!isInvokeOrInlineExtension(targetDescriptor)) {
            context.trace.report(Errors.USAGE_IS_NOT_INLINABLE.on(receiverExpresssion, receiverExpresssion, descriptor));
        }
    }


    private boolean isExactFunctionOrExtensionFunctionType(@NotNull JetType type) {
        return KotlinBuiltIns.getInstance().isExactFunctionOrExtensionFunctionType(type);
    }

    private boolean isInvokeOrInlineExtension(@NotNull CallableDescriptor descriptor) {
        return descriptor.getName().asString().equals("invoke") ||
               //or inline extension
               (descriptor instanceof SimpleFunctionDescriptor) && ((SimpleFunctionDescriptor) descriptor).isInline();
    }

    private void checkVisibility(@NotNull CallableDescriptor declarationDescriptor, @NotNull JetElement expression, @NotNull BasicCallResolutionContext context){
        if (isEffectivelyPublicApiFunction && !isEffectivelyPublicApi(declarationDescriptor)) {
            context.trace.report(Errors.INVISIBLE_MEMBER_FROM_INLINE.on(expression, declarationDescriptor, descriptor));
        }
    }

    private static boolean isEffectivelyPublicApi(DeclarationDescriptorWithVisibility descriptor) {
        DeclarationDescriptorWithVisibility parent = descriptor;
        while (parent != null) {
            Visibility visibility = parent.getVisibility();
            if (visibility != Visibilities.LOCAL && !visibility.isPublicAPI() ) {
                return false;
            }
            parent = DescriptorUtils.getParentOfType(parent, DeclarationDescriptorWithVisibility.class);
        }
        return true;
    }
}
