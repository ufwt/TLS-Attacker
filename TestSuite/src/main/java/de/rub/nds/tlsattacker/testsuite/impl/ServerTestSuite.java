/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.testsuite.impl;

import de.rub.nds.tlsattacker.attacks.config.BleichenbacherCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.HeartbleedCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.InvalidCurveAttackConfig;
import de.rub.nds.tlsattacker.attacks.config.PaddingOracleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.PoodleCommandConfig;
import de.rub.nds.tlsattacker.attacks.impl.BleichenbacherAttacker;
import de.rub.nds.tlsattacker.attacks.impl.HeartbleedAttacker;
import de.rub.nds.tlsattacker.attacks.impl.InvalidCurveAttacker;
import de.rub.nds.tlsattacker.attacks.impl.PaddingOracleAttacker;
import de.rub.nds.tlsattacker.attacks.impl.PoodleAttacker;
import de.rub.nds.tlsattacker.modifiablevariable.ModifiableVariable;
import de.rub.nds.tlsattacker.modifiablevariable.util.ModifiableVariableAnalyzer;
import de.rub.nds.tlsattacker.modifiablevariable.util.ModifiableVariableField;
import de.rub.nds.tlsattacker.testsuite.config.ServerTestSuiteConfig;
import de.rub.nds.tlsattacker.tls.Attacker;
import de.rub.nds.tlsattacker.tls.config.TLSDelegateConfig;
import de.rub.nds.tlsattacker.tls.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.tls.config.delegate.Delegate;
import de.rub.nds.tlsattacker.tls.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.tls.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.tls.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.tls.protocol.message.ArbitraryMessage;
import de.rub.nds.tlsattacker.tls.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.tls.util.LogLevel;
import de.rub.nds.tlsattacker.tls.workflow.TlsConfig;
import de.rub.nds.tlsattacker.tls.workflow.TlsContext;
import de.rub.nds.tlsattacker.tls.workflow.WorkflowExecutor;
import de.rub.nds.tlsattacker.tls.workflow.WorkflowExecutorFactory;
import de.rub.nds.tlsattacker.tls.workflow.WorkflowTrace;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 */
public class ServerTestSuite extends TestSuite {

    public static Logger LOGGER = LogManager.getLogger(ServerTestSuite.class);

    private final ServerTestSuiteConfig testConfig;

    public ServerTestSuite(ServerTestSuiteConfig serverTestConfig) {
        super();
        this.testConfig = serverTestConfig;
    }

    @Override
    public boolean startTests() {
        this.startAttackTests();
        this.startTestFromFiles();
        return failedTests.isEmpty();
    }

    private void startAttackTests() {
        Attacker<? extends TLSDelegateConfig> attacker;
        BleichenbacherCommandConfig bb = new BleichenbacherCommandConfig(testConfig.getGeneralDelegate());
        setHost(bb);
        attacker = new BleichenbacherAttacker(bb);
        attacker.executeAttack();
        if (attacker.isVulnerable()) {
            failedTests.add(BleichenbacherCommandConfig.ATTACK_COMMAND);
        } else {
            successfulTests.add(BleichenbacherCommandConfig.ATTACK_COMMAND);
        }
        InvalidCurveAttackConfig icea = new InvalidCurveAttackConfig(testConfig.getGeneralDelegate());
        setHost(icea);
        attacker = new InvalidCurveAttacker(icea);
        attacker.executeAttack();
        if (attacker.isVulnerable()) {
            failedTests.add(InvalidCurveAttackConfig.ATTACK_COMMAND);
        } else {
            successfulTests.add(InvalidCurveAttackConfig.ATTACK_COMMAND);
        }
        HeartbleedCommandConfig heartbleed = new HeartbleedCommandConfig(testConfig.getGeneralDelegate());
        setHost(heartbleed);
        attacker = new HeartbleedAttacker(heartbleed);
        attacker.executeAttack();
        if (attacker.isVulnerable()) {
            failedTests.add(HeartbleedCommandConfig.ATTACK_COMMAND);
        } else {
            successfulTests.add(HeartbleedCommandConfig.ATTACK_COMMAND);
        }
        PoodleCommandConfig poodle = new PoodleCommandConfig(testConfig.getGeneralDelegate());
        setHost(poodle);
        attacker = new PoodleAttacker(poodle);
        attacker.executeAttack();
        if (attacker.isVulnerable()) {
            failedTests.add(PoodleCommandConfig.ATTACK_COMMAND);
        } else {
            successfulTests.add(PoodleCommandConfig.ATTACK_COMMAND);
        }
        PaddingOracleCommandConfig po = new PaddingOracleCommandConfig(testConfig.getGeneralDelegate());
        setHost(po);
        attacker = new PaddingOracleAttacker(po);
        attacker.executeAttack();
        if (attacker.isVulnerable()) {
            failedTests.add(PaddingOracleCommandConfig.ATTACK_COMMAND);
        } else {
            successfulTests.add(PaddingOracleCommandConfig.ATTACK_COMMAND);
        }
    }

    // TODO Ugly probably better to have a client/server delegate config for
    // this
    public void setHost(TLSDelegateConfig delegateConfig) {
        String host = null;
        for (Delegate delegate : testConfig.getDelegateList()) {
            if (delegate instanceof ClientDelegate) {
                host = ((ClientDelegate) delegate).getHost();
            }
        }
        for (Delegate delegate : delegateConfig.getDelegateList()) {
            if (delegate instanceof ClientDelegate) {
                ((ClientDelegate) delegate).setHost(host);
                return;
            }
        }
        throw new IllegalArgumentException("Provided Config did not contain ClientDelegate");
    }

    private void startTestFromFiles() {
        File folder = new File(testConfig.getFolder());
        File[] testsuites = folder.listFiles(new DirectoryFilter());
        if (null == testsuites) {
            testsuites = new File[0];
        }
        for (File testsuite : testsuites) {
            LOGGER.log(LogLevel.CONSOLE_OUTPUT, "Starting {} Test Suite", testsuite.getName());
            File[] tests = testsuite.listFiles(new DirectoryFilter());
            if (null == tests) {
                tests = new File[0];
            }
            for (File test : tests) {
                LOGGER.info("Testing {} (one of these has to be succesful)", test.getName());
                File[] testCases = test.listFiles(new DirectoryFilter());
                if (null == testCases) {
                    testCases = new File[0];
                }
                boolean successfulTest = false;
                for (File testCase : testCases) {
                    LOGGER.info("  Running {}", testCase.getName());
                    if (startTestCase(testCase)) {
                        // one of our test cases was successful
                        successfulTest = true;
                    }
                }
                if (successfulTest) {
                    LOGGER.log(LogLevel.CONSOLE_OUTPUT, "{} SUCCESSFUL ", test.getName());
                    successfulTests.add(test.getName());
                } else {
                    LOGGER.log(LogLevel.CONSOLE_OUTPUT, "{} FAILED ", test.getName());
                    failedTests.add(test.getName());
                }
            }
        }
        LOGGER.log(LogLevel.CONSOLE_OUTPUT, "Summary of successful tests");
        for (String s : successfulTests) {
            LOGGER.log(LogLevel.CONSOLE_OUTPUT, "  {}", s);
        }
        LOGGER.log(LogLevel.CONSOLE_OUTPUT, "Summary of failed tests");
        for (String s : failedTests) {
            LOGGER.log(LogLevel.CONSOLE_OUTPUT, "  {}", s);
        }
        LOGGER.log(LogLevel.CONSOLE_OUTPUT, "Successful tests: {}", successfulTests.size());
        LOGGER.log(LogLevel.CONSOLE_OUTPUT, "Failed tests: {}", failedTests.size());
    }

    private boolean startTestCase(File testFolder) {
        boolean succesful = true;

        File[] xmlFiles = testFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });

        if (null == xmlFiles) {
            xmlFiles = new File[0];
        }

        for (File xmlFile : xmlFiles) {
            try {
                TlsConfig tlsConfig = testConfig.createConfig();
                tlsConfig.setWorkflowInput(xmlFile.getAbsolutePath());

                TlsContext tlsContext = new TlsContext(tlsConfig);
                WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.createWorkflowExecutor(
                        tlsConfig.getExecutorType(), tlsContext);
                workflowExecutor.executeWorkflow();
                if (isWorkflowTraceReasonable(tlsContext.getWorkflowTrace())) {
                    LOGGER.info("    {} passed", xmlFile.getName());
                    List<ModifiableVariableField> mvfs = ModifiableVariableAnalyzer
                            .getAllModifiableVariableFieldsRecursively(tlsContext.getWorkflowTrace());
                    for (ModifiableVariableField mvf : mvfs) {
                        ModifiableVariable mv = mvf.getModifiableVariable();
                        if (mv != null && mv.containsAssertion()) {
                            if (mv.validateAssertions()) {
                                LOGGER.info("    Assertion in {}.{} succesfully validated", mvf.getObject().getClass()
                                        .getSimpleName(), mvf.getField().getName());
                            } else {
                                LOGGER.info("    Assertion in {}.{} invalid", mvf.getObject().getClass()
                                        .getSimpleName(), mvf.getField().getName());
                                succesful = false;
                            }
                        }
                    }
                } else {
                    LOGGER.info("    {} failed", xmlFile.getName());
                    succesful = false;
                }
            } catch (WorkflowExecutionException | ConfigurationException | IllegalArgumentException
                    | IllegalAccessException ex) {
                LOGGER.info("    {} failed", xmlFile.getName());
                LOGGER.info(ex);
                succesful = false;
            }
        }

        return succesful;
    }

    // TODO duplicate code
    private boolean isWorkflowTraceReasonable(WorkflowTrace trace) {
        int counter = 0;
        for (ProtocolMessage configuredMessage : trace.getAllConfiguredMessages()) {
            if (counter >= trace.getAllExecutedMessages().size()) {
                return false;
            }
            ProtocolMessage receivedMessage = trace.getAllExecutedMessages().get(counter);
            if (configuredMessage.getClass().equals(ArbitraryMessage.class)) {
                break;
            }
            if (configuredMessage.getClass() != receivedMessage.getClass()) {
                if (configuredMessage.isRequired()) {
                    return false;
                }
            } else {
                counter++;
            }
        }
        return (!trace.getActuallyRecievedHandshakeMessagesOfType(HandshakeMessageType.FINISHED).isEmpty());
    }

    class DirectoryFilter implements FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory();
        }

    };

}
