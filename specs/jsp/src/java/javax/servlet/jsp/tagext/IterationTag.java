/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package javax.servlet.jsp.tagext;

import javax.servlet.jsp.*;

/**
 * The IterationTag interface extends Tag by defining one additional
 * method that controls the reevaluation of its body.
 *
 * <p> A tag handler that implements IterationTag is treated as one that
 * implements Tag regarding  the doStartTag() and doEndTag() methods.
 * IterationTag provides a new method: <code>doAfterBody()</code>.
 *
 * <p> The doAfterBody() method is invoked after every body evaluation
 * to control whether the body will be reevaluated or not.  If doAfterBody()
 * returns IterationTag.EVAL_BODY_AGAIN, then the body will be reevaluated.
 * If doAfterBody() returns Tag.SKIP_BODY, then the body will be skipped
 * and doEndTag() will be evaluated instead.
 *
 * <p><B>Properties</B>
 * There are no new properties in addition to those in Tag.
 *
 * <p><B>Methods</B>
 * There is one new methods: doAfterBody().
 *
 * <p><B>Lifecycle</B>
 *
 * <p> Lifecycle details are described by the transition diagram
 * below.  Exceptions that are thrown during the computation of
 * doStartTag(), BODY and doAfterBody() interrupt the execution
 * sequence and are propagated up the stack, unless the tag handler
 * implements the TryCatchFinally interface; see that interface for
 * details.
 *
 * <p>
 * <IMG src="doc-files/IterationTagProtocol.gif"
 *      alt="Lifecycle Details Transition Diagram for IterationTag"/>
 *
 * <p><B>Empty and Non-Empty Action</B>
 * <p> If the TagLibraryDescriptor file indicates that the action must
 * always have an empty element body, by a &lt;body-content&gt; entry of 
 * "empty", then the doStartTag() method must return SKIP_BODY.
 *
 * <p>Note that which methods are invoked after the doStartTag() depends on
 * both the return value and on if the custom action element is empty
 * or not in the JSP page, not on how it's declared in the TLD.
 *
 * <p>
 * If SKIP_BODY is returned the body is not evaluated, and then doEndTag()
 * is invoked.
 *
 * <p>
 * If EVAL_BODY_INCLUDE is returned, and the custom action element is not
 * empty, the body is evaluated and "passed through" to the current out, 
 * then doAfterBody() is invoked and, after zero or more iterations, 
 * doEndTag() is invoked.
 */

public interface IterationTag extends Tag {

    /**
     * Request the reevaluation of some body.
     * Returned from doAfterBody.
     *
     * For compatibility with JSP 1.1, the value is carefully selected
     * to be the same as the, now deprecated, BodyTag.EVAL_BODY_TAG,
     * 
     */
 
    public final static int EVAL_BODY_AGAIN = 2;

    /**
     * Process body (re)evaluation.  This method is invoked by the
     * JSP Page implementation object after every evaluation of
     * the body into the BodyEvaluation object. The method is
     * not invoked if there is no body evaluation.
     *
     * <p>
     * If doAfterBody returns EVAL_BODY_AGAIN, a new evaluation of the
     * body will happen (followed by another invocation of doAfterBody).
     * If doAfterBody returns SKIP_BODY, no more body evaluations will occur,
     * and the doEndTag method will be invoked.
     *
     * <p>
     * If this tag handler implements BodyTag and doAfterBody returns
     * SKIP_BODY, the value of out will be restored using the popBody 
     * method in pageContext prior to invoking doEndTag.
     *
     * <p>
     * The method re-invocations may be lead to different actions because
     * there might have been some changes to shared state, or because
     * of external computation.
     *
     * <p>
     * The JSP container will resynchronize the values of any AT_BEGIN and
     * NESTED variables (defined by the associated TagExtraInfo or TLD) after
     * the invocation of doAfterBody().
     *
     * @return whether additional evaluations of the body are desired
     * @throws JspException if an error occurred while processing this tag
     */

    int doAfterBody() throws JspException;
}
