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

package org.jetbrains.jet.lang.resolve.calls.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.CallableDescriptor;
import org.jetbrains.jet.lang.descriptors.ReceiverParameterDescriptor;
import org.jetbrains.jet.lang.descriptors.TypeParameterDescriptor;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.psi.Call;
import org.jetbrains.jet.lang.resolve.calls.tasks.ExplicitReceiverKind;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.jet.lang.types.JetType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverValue.NO_RECEIVER;

public class ThisResolvedCall implements ResolvedCall<CallableDescriptor> {

    private final DataFlowInfoForArgumentsImpl dataFlowInfoForArguments;
    private final ReceiverParameterDescriptor receiverParameterDescriptor;

    public ThisResolvedCall(ReceiverParameterDescriptor receiverParameterDescriptor, Call call) {
        this.receiverParameterDescriptor = receiverParameterDescriptor;
        this.dataFlowInfoForArguments = new DataFlowInfoForArgumentsImpl(call);
    }

    @NotNull
    @Override
    public CallableDescriptor getCandidateDescriptor() {
        return receiverParameterDescriptor;
    }

    @NotNull
    @Override
    public CallableDescriptor getResultingDescriptor() {
        return receiverParameterDescriptor;
    }

    @NotNull
    @Override
    public ReceiverValue getReceiverArgument() {
        return NO_RECEIVER;
    }

    @NotNull
    @Override
    public ReceiverValue getThisObject() {
        return NO_RECEIVER;
    }

    @NotNull
    @Override
    public ExplicitReceiverKind getExplicitReceiverKind() {
        return ExplicitReceiverKind.NO_EXPLICIT_RECEIVER;
    }

    @NotNull
    @Override
    public Map<ValueParameterDescriptor, ResolvedValueArgument> getValueArguments() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public List<ResolvedValueArgument> getValueArgumentsByIndex() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Map<TypeParameterDescriptor, JetType> getTypeArguments() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public DataFlowInfoForArguments getDataFlowInfoForArguments() {
        return dataFlowInfoForArguments;
    }

    @Override
    public boolean isSafeCall() {
        return true;
    }

}
