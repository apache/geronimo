/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.deployment.task.DeploymentTask;


/**
 * Performs unit tests on {@link DeploymentPlanner} by using various dummy
 * {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask}s
 * @version $Revision: 1.1 $ $Date: 2003/12/07 03:32:42 $
 */
public class DeploymentPlanTest extends TestCase {

    private DeploymentPlan plan = null;
    private DummyTask goodTask1 = null;
    private DummyTask badTask1 = null;
    private DummyTask evilTask1 = null;

    /**
     * Tests the addition of a task to a {@link DeploymentPlan} by querying
     * the execution status of a
     * {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask} after
     * plan execution.
     */
    public void testAddTask() {
        // Add a dummy task, testing its addition by its performed status after
        // calling plan.execute()
        plan.addTask(goodTask1);
        try {
            plan.execute();
            assertTrue("Added task not executed", goodTask1.isPerformed());
        } catch (DeploymentException de) {
            fail("Added task produced deployment exception " +
                 de.getMessage());
        }
    }

    /**
     * Tests the plan query capability by adding various simple
     * {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask}s which
     * can always be run, be run at some point in the future or never run.
     */
    public void testCanRun() {
        // Add a dummy task which always returns true for canRun().
        plan.addTask(goodTask1);
        try {
            assertTrue("Expected runnable plan", plan.canRun());
        } catch (DeploymentException de) {
            fail("Unexpected DeploymentException " + de.getMessage());
        }

        // Add a dummy task which always returns false for canRun().
        plan.addTask(badTask1);
        try {
            assertFalse("Expected non-runnable plan", plan.canRun());
        } catch (DeploymentException de) {
            fail("Unexpected DeplymentException " + de.getMessage());
        }

        // Add a dummy task which always throws for canRun().
        plan.addTask(evilTask1);
        try {
            plan.canRun();
            // Shouldn't get this far!
            fail("Expected DeploymentException");
        } catch (DeploymentException de) {
            // Expected!
        }
    }

    /**
     * Tests the execution of a set of {@link
     * org.apache.geronimo.kernel.deployment.task.DeploymentTask}s.
     */
    public void testExecute() {
        // Add a couple of harmless tasks and query their performed status.
        plan.addTask(goodTask1);
        plan.addTask(badTask1);
        try {
            plan.execute();
            assertTrue("Task not performed", goodTask1.isPerformed());
            assertTrue("Task not performed", badTask1.isPerformed());
        } catch (DeploymentException de) {
            fail("Unexpected DeploymentException " + de.getMessage());
        }

        // Add a harmful task and query the undone status of previous tasks.
        goodTask1.reset();
        badTask1.reset();
        plan.addTask(evilTask1);
        try {
            plan.execute();
            // Shouldn't get this far!
            fail("Expected DeploymentException");
        } catch (DeploymentException de) {
            // Expected!
            assertTrue("Task not undone", goodTask1.isUndone());
            assertTrue("Task not undone", badTask1.isUndone());
        }
    }

    /**
     * Sets up this test case by constructing a {@link DeploymentPlan} and
     * various {@link
     * org.apache.geronimo.kernel.deployment.task.DeploymentTask}s for use in
     * all tests.
     */
    protected void setUp() {
        plan = new DeploymentPlan();
        goodTask1 = new DummyGoodTask();
        badTask1 = new DummyBadTask();
        evilTask1 = new DummyEvilTask();
    }

    /**
     * Dereferences the common objects created by {@link #setUp()}.
     */
    protected void tearDown() {
        plan = null;
        goodTask1 = null;
        badTask1 = null;
        evilTask1 = null;
    }

    /**
     * Common superclass of the dummy {@link
     * org.apache.geronimo.kernel.deployment.task.DeploymentTask}s. Instances
     * can be queried for their performed and undone status by using the
     * {@link #isPerformed()} and {@link #isUndone()} methods.
     */
    private abstract class DummyTask implements DeploymentTask {

        private boolean isPerformed = false;
        private boolean isUndone = false;

        /**
         * Updates the performed status to <code>true</code> without throwing
         * a {@link DeploymentException}
         */
        public void perform() throws DeploymentException {
            isPerformed = true;
        }

        /**
         * Updates the undone status to <code>true</code>
         */
        public void undo() {
            isUndone = true;
        }

        /**
         * Queries the performed status.
         */
        protected boolean isPerformed() {
            return isPerformed;
        }

        /**
         * Queries the undone status.
         */
        protected boolean isUndone() {
            return isUndone;
        }

        /**
         * Resets the performed and undone status.
         */
        protected void reset() {
            isPerformed = false;
            isUndone = false;
        }
    }

    /**
     * Trivial {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask}
     * whose {@link #canRun()} method always returns <code>true</code>.
     */
    private class DummyGoodTask extends DummyTask {

        /**
         * For testing purposes, always returns <code>true</code>.
         *
         * @return <code>true</code>, always.
         */
        public boolean canRun() throws DeploymentException {
            return true;
        }
    }

    /**
     * Trivial {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask}
     * whose {@link #canRun()} method always returns <code>false</code>.
     */
    private class DummyBadTask extends DummyTask {

        /**
         * For testing purposes, always returns <code>false</code>.
         *
         * @return <code>false</code>, always.
         */
        public boolean canRun() throws DeploymentException {
            return false;
        }
    }

    /**
     * Trivial {@link org.apache.geronimo.kernel.deployment.task.DeploymentTask}
     * whose {@link #canRun()} and {@link #perform()} methods always throws a
     * {@link DeploymentException}.
     */
    private class DummyEvilTask extends DummyTask {

        /**
         * For testing purposes, always throws a {@link DeploymentException}
         *
         * @exception DeploymentException always.
         */
        public boolean canRun() throws DeploymentException {
            throw new DeploymentException("Expected");
        }

        /**
         * For testing purposes, always throws a {@link DeploymentException}
         *
         * @exception DeploymentException always.
         */
        public void perform() throws DeploymentException {
            throw new DeploymentException("Expected");
        }
    }
}
