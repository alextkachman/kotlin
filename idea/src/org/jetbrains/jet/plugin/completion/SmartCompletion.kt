package org.jetbrains.jet.plugin.completion

import org.jetbrains.jet.lang.descriptors.*
import org.jetbrains.jet.lang.resolve.*
import org.jetbrains.jet.lang.psi.*
import org.jetbrains.jet.lexer.JetTokens
import org.jetbrains.jet.plugin.project.CancelableResolveSession
import org.jetbrains.jet.lang.types.*
import org.jetbrains.jet.lang.types.checker.JetTypeChecker
import com.intellij.codeInsight.lookup.*
import java.util.ArrayList
import org.jetbrains.jet.renderer.DescriptorRenderer
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.InsertHandler
import org.jetbrains.jet.plugin.completion.handlers.*
import com.intellij.codeInsight.completion.JavaPsiClassReferenceElement
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowValue
import com.google.common.collect.SetMultimap
import java.util.HashMap
import org.jetbrains.jet.lang.resolve.calls.autocasts.Nullability
import java.util.HashSet

trait SmartCompletionData{
    fun accepts(descriptor: DeclarationDescriptor): Boolean
    val additionalElements: Iterable<LookupElement>
}

fun buildSmartCompletionData(expression: JetSimpleNameExpression, resolveSession: CancelableResolveSession): SmartCompletionData? {
    val parent = expression.getParent()
    val expressionWithType = if (parent is JetQualifiedExpression) parent else expression
    val hasQualifier = expression != expressionWithType
    val bindingContext = resolveSession.resolveToElement(expressionWithType)
    val expectedType: JetType? = bindingContext.get(BindingContext.EXPECTED_EXPRESSION_TYPE, expressionWithType)
    if (expectedType == null) return null

    val itemsToSkip = calcItemsToSkip(expressionWithType, resolveSession)

    val additionalElements = ArrayList<LookupElement>()

    if (!hasQualifier) {
        additionalElements.addAll(typeInstantiationItems(expectedType, resolveSession, bindingContext))
    }

    val variableToType = HashMap<VariableDescriptor, Collection<JetType>>()
    val notNullVariables = HashSet<VariableDescriptor>()
    if (!hasQualifier) { //TODO: support case with qualifier too
        val dataFlowInfo = bindingContext.get(BindingContext.EXPRESSION_DATA_FLOW_INFO, expression)
        if (dataFlowInfo != null){
            val typeInfo: SetMultimap<DataFlowValue, JetType> = dataFlowInfo.getCompleteTypeInfo()
            for(entry : Map.Entry<DataFlowValue, Collection<JetType>> in typeInfo.asMap()!!.entrySet()) {
                val id = entry.getKey().getId()
                if (id is VariableDescriptor) {
                    variableToType[id] = entry.getValue()
                }
            }

            val nullabilityInfo: Map<DataFlowValue, Nullability> = dataFlowInfo.getCompleteNullabilityInfo()
            for(entry in nullabilityInfo){
                val id = entry.getKey().getId()
                if (id is VariableDescriptor && entry.getValue() == Nullability.NOT_NULL) {
                    notNullVariables.add(id)
                }
            }
        }
    }

    fun typesOf(descriptor: DeclarationDescriptor) : Iterable<JetType> {
        if (descriptor is CallableDescriptor) {
            var returnType = descriptor.getReturnType()
            if (descriptor is VariableDescriptor){
                if (notNullVariables.contains(descriptor) && returnType != null) {
                    returnType = TypeUtils.makeNotNullable(returnType!!)
                }

                val additionalTypes = variableToType[descriptor]
                if (additionalTypes != null && !additionalTypes.isEmpty()) {
                    return additionalTypes + returnType.toList()
                }
            }
            return returnType.toList()
        }
        else {
            return listOf() //TODO: can we have it without new object each time?
        }
    }

    return object: SmartCompletionData{
        override fun accepts(descriptor: DeclarationDescriptor)
                = !itemsToSkip.contains(descriptor) && typesOf(descriptor).any { JetTypeChecker.INSTANCE.isSubtypeOf(it, expectedType) }

        override val additionalElements = additionalElements
    }
}

private fun typeInstantiationItems(expectedType: JetType, resolveSession: CancelableResolveSession, bindingContext: BindingContext) : Collection<LookupElement> {
    val typeConstructor: TypeConstructor = expectedType.getConstructor()
    val classifier: ClassifierDescriptor? = typeConstructor.getDeclarationDescriptor()
    if (!(classifier is ClassDescriptor)) return listOf()
    if (classifier.getModality() == Modality.ABSTRACT) return listOf()

    //TODO: check for constructor's visibility

    val lookupElement = DescriptorLookupConverter.createLookupElement(resolveSession, bindingContext, classifier)

    val typeArgs = expectedType.getArguments()
    //TODO: shouldn't be method in DescriptorRenderer to render type arguments?
    val typeArgsText =
            if (typeArgs.isEmpty())
                ""
            else
                typeArgs.map { DescriptorRenderer.TEXT.renderType(it.getType()) }.makeString(", ", "<", ">")
    val presentableText = lookupElement.getLookupString() + typeArgsText + "()"

    val constructors: Collection<ConstructorDescriptor> = classifier.getConstructors()
    val caretPosition =
            if (constructors.size == 0)
                CaretPosition.AFTER_BRACKETS
            else if (constructors.size == 1)
                if (constructors.first().getValueParameters().isEmpty()) CaretPosition.AFTER_BRACKETS else CaretPosition.IN_BRACKETS
            else
                CaretPosition.IN_BRACKETS
    val insertHandler = JetFunctionInsertHandler(caretPosition, BracketType.PARENTHESIS)

    //TODO: very bad code
    if (lookupElement is LookupElementBuilder) {
        return listOf(lookupElement.withPresentableText(presentableText).withInsertHandler(insertHandler))
    }
    else if (lookupElement is JavaPsiClassReferenceElement) {
        return listOf(lookupElement.setPresentableText(presentableText).setInsertHandler(insertHandler))
    }

    return listOf()
}

private fun <T : Any> T?.toList(): List<T> = if (this != null) listOf(this) else listOf()

private fun calcItemsToSkip(expression: JetExpression, resolveSession: CancelableResolveSession): Collection<DeclarationDescriptor> {
    val parent = expression.getParent()
    when(parent) {
        is JetProperty -> {
            //TODO: this can be filtered out by ordinary completion
            if (expression == parent.getInitializer()) {
                return resolveSession.resolveToElement(parent).get(BindingContext.DECLARATION_TO_DESCRIPTOR, parent).toList()
            }
        }

        is JetBinaryExpression -> {
            if (parent.getRight() == expression && parent.getOperationToken() == JetTokens.EQ) {
                val left = parent.getLeft()
                if (left is JetReferenceExpression) {
                    return resolveSession.resolveToElement(left).get(BindingContext.REFERENCE_TARGET, left).toList()
                }
            }
        }
    }
    return listOf()
}