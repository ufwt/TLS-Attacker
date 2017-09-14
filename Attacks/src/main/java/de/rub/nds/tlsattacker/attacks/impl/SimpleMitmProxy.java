/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.impl;

import de.rub.nds.tlsattacker.attacks.config.SimpleMitmProxyCommandConfig;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowExecutorFactory;
import de.rub.nds.tlsattacker.core.workflow.action.executor.WorkflowExecutorType;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;

/**
 *
 * @author Lucas Hartmann <lucas.hartmann@rub.de>
 */
public class SimpleMitmProxy extends Attacker<SimpleMitmProxyCommandConfig> {

    public SimpleMitmProxy(SimpleMitmProxyCommandConfig config) {
        super(config, false);

    }

    @Override
    public void executeAttack() {
        State state = new State(config.createConfig());
        state.getConfig().setWorkflowTraceType(WorkflowTraceType.SIMPLE_MITM_PROXY);
        WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.createWorkflowExecutor(
                WorkflowExecutorType.DEFAULT, state);
        workflowExecutor.executeWorkflow();
    }

    @Override
    public Boolean isVulnerable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
