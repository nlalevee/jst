/*
 *  Copyright 2012 JST contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.jst.jvmmodel

import com.google.inject.Inject
import java.io.Writer
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.hibnet.jst.jst.Field
import org.hibnet.jst.jst.JstFile
import org.hibnet.jst.jst.Method
import org.hibnet.jst.jst.RichStringRender
import org.eclipse.xtext.xbase.XbaseFactory
import org.hibnet.jst.jst.RichStringTemplateRender
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.TypesFactory
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.hibnet.jst.jst.AbstractRenderer
import java.io.IOException
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.xbase.XStringLiteral

/**
 * <p>Infers a JVM model from the source model.</p> 
 */
class JstJvmModelInferrer extends AbstractModelInferrer {

	@Inject extension JvmTypesBuilder

    @Inject
    private TypesFactory typesFactory;

   	def dispatch void infer(JstFile element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {

   		val simpleName = element.eResource.URI.trimFileExtension.lastSegment.toFirstUpper + "JstTemplate"
   		element.simpleName = simpleName

   		acceptor.accept(element).initializeLater [
   		    element.setVisibility(JvmVisibility::PUBLIC)
   		    for (member : element.members) {
   		        if (member instanceof Field) {
   		            val field = (member as Field)
   		            if (field.initialValue != null) {
   		                field.setInitializer(field.initialValue)
   		            }
   		        } else if (member instanceof Method) {
                    val method = (member as Method)
                    method.body = method.getExpression()
   		        }
   		    }
   		    for (renderer : element.renderers) {
   		        element.members += element.toMethod(renderer.simpleName, element.newTypeRef(Void::TYPE)) [
                    visibility = JvmVisibility::PUBLIC
                    parameters += element.toParameter("out", renderer.newTypeRef(typeof(Writer)))
                    for (parameter : renderer.parameters) {
                        parameters += element.toParameter(parameter.name, parameter.parameterType)
                    }
                    exceptions += element.newTypeRef(typeof(IOException))
                    if (renderer instanceof AbstractRenderer) {
                        setAbstract(true)                    
                    } else {
                        body = renderer.getExpression()                        
                    }
                ]
   		    }
            element.members += buildWriteMethod(element, "unescape", null)
            element.members += buildWriteMethod(element, "escape_xml", "org.apache.commons.lang.StringEscapeUtils.escapeXml(")
            element.members += buildWriteMethod(element, "escape_html", "org.apache.commons.lang.StringEscapeUtils.escapeHtml(")
            element.members += buildWriteMethod(element, "escape_js", "org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(")
            element.members += buildWriteMethod(element, "escape_java", "org.apache.commons.lang.StringEscapeUtils.escapeJava(")
            element.members += buildWriteMethod(element, "escape_csv", "org.apache.commons.lang.StringEscapeUtils.escapeCsv(")
            element.members += buildWriteMethod(element, "escape_sql", "org.apache.commons.lang.StringEscapeUtils.escapeSql(")

            element.members +=  element.toMethod("_jst_write_escape", element.newTypeRef(Void::TYPE)) [
                visibility = JvmVisibility::PRIVATE
                parameters += element.toParameter("out", element.newTypeRef(typeof(Writer)))
                parameters += element.toParameter("object", element.newTypeRef(typeof(Object)))
                parameters += element.toParameter("elvis", element.newTypeRef(Boolean::TYPE))
                exceptions += element.newTypeRef(typeof(IOException))
                body = [
                    var escape = getEscapeOption(element)
                    if (escape == null) {
                        escape = 'unescape'
                    }
                    append('_jst_write_escape_')
                    append(escape)
                    append('(out, object, elvis);')
                ]
            ]
		]
	}

    def private JvmOperation buildWriteMethod(JstFile element, String escapeName, String escapeFunction) {
        return element.toMethod("_jst_write_" + escapeName, element.newTypeRef(Void::TYPE)) [
            visibility = JvmVisibility::PRIVATE
            parameters += element.toParameter("out", element.newTypeRef(typeof(Writer)))
            parameters += element.toParameter("object", element.newTypeRef(typeof(Object)))
            parameters += element.toParameter("elvis", element.newTypeRef(Boolean::TYPE))
            exceptions += element.newTypeRef(typeof(IOException))
            body = [
                append('if (object != null) {').increaseIndentation.newLine
                append('out.append(')
                if (escapeFunction != null) {
                    append(escapeFunction)
                }
                append('object.toString()')
                if (escapeFunction != null) {
                    append(')')
                }
                append(');')
                decreaseIndentation.newLine.append('} else if (!elvis) {').increaseIndentation.newLine
                append('out.append("null");')
                decreaseIndentation.newLine.append('}')
            ]
        ]
    } 

    def private String getEscapeOption(JstFile element) {
        for (option : element.options) {
            if (option.key.equals('escape') && option.value instanceof XStringLiteral) {
                return (option.value as XStringLiteral).value
            }
        }
        return null;
    }

    def dispatch void infer(RichStringRender render, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
        val call = XbaseFactory::eINSTANCE.createXFeatureCall
        call.feature = render.feature
        call.featureCallArguments += getOutParam(render)
        call.featureCallArguments += render.featureCallArguments
    }

    def dispatch void infer(RichStringTemplateRender render, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
        val call = XbaseFactory::eINSTANCE.createXMemberFeatureCall
        call.memberCallTarget = render.memberCallTarget
        call.feature = render.feature
        call.memberCallArguments += getOutParam(render)
        call.memberCallArguments += render.memberCallArguments
    }
    
    def private XExpression getOutParam(EObject element) {
        val featureCall = XbaseFactory::eINSTANCE.createXFeatureCall();
        val JvmFormalParameter out = typesFactory.createJvmFormalParameter();
        out.setName("out");
        out.setParameterType(cloneWithProxies(element.newTypeRef(typeof(Writer))));
        featureCall.setFeature(out)
        return featureCall;
    }
    
}
