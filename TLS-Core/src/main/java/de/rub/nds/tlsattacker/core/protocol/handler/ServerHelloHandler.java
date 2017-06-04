/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.CompressionMethod;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.parser.Parser;
import de.rub.nds.tlsattacker.core.protocol.parser.ServerHelloParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.Preparator;
import de.rub.nds.tlsattacker.core.protocol.preparator.ServerHelloMessagePreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.Serializer;
import de.rub.nds.tlsattacker.core.protocol.serializer.ServerHelloMessageSerializer;
import de.rub.nds.tlsattacker.core.workflow.TlsContext;
import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.record.cipher.RecordCipher;
import de.rub.nds.tlsattacker.core.record.cipher.RecordCipherFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 * @author Philip Riese <philip.riese@rub.de>
 */
public class ServerHelloHandler extends HandshakeMessageHandler<ServerHelloMessage> {

    public ServerHelloHandler(TlsContext tlsContext) {
        super(tlsContext);
    }

    @Override
    public ServerHelloMessagePreparator getPreparator(ServerHelloMessage message) {
        return new ServerHelloMessagePreparator(tlsContext, message);
    }

    @Override
    public ServerHelloMessageSerializer getSerializer(ServerHelloMessage message) {
        return new ServerHelloMessageSerializer(message, tlsContext.getSelectedProtocolVersion());
    }

    @Override
    public ServerHelloParser getParser(byte[] message, int pointer) {
        return new ServerHelloParser(pointer, message, tlsContext.getLastRecordVersion());
    }

    @Override
    protected void adjustTLSContext(ServerHelloMessage message) {
        // Use the right Protocol Version ?
        adjustSelectedProtocolVersion(message);
        if (tlsContext.getSelectedProtocolVersion() != ProtocolVersion.TLS13) {
            adjustSelectedCompression(message);
            adjustSelectedSessionID(message);
        }
        adjustSelectedCiphersuite(message);
        adjustServerRandom(message);
        adjustLastRecordVersion(message);
        if (message.getExtensions() != null) {
            for (ExtensionMessage extension : message.getExtensions()) {
                extension.getHandler(tlsContext).adjustTLSContext(extension);
            }
        }
        if (tlsContext.getSelectedProtocolVersion() == ProtocolVersion.TLS13) {
            setRecordCipher();
        }
        if (tlsContext.getSelectedProtocolVersion() == ProtocolVersion.TLS13
                && tlsContext.getTalkingConnectionEnd() != tlsContext.getConfig().getConnectionEnd()) {
            setRecordCipher();
            // System.out.println("Starting to decrypt");
            tlsContext.getRecordLayer().updateDecryptionCipher();
            // System.out.println("Starting to encrypt");
            tlsContext.getRecordLayer().updateEncryptionCipher();
            tlsContext.setEncryptActive(true);
        }
    }

    private void adjustSelectedCiphersuite(ServerHelloMessage message) {
        CipherSuite suite = CipherSuite.getCipherSuite(message.getSelectedCipherSuite().getValue());
        tlsContext.setSelectedCipherSuite(suite);
        LOGGER.debug("Set SelectedCipherSuite in Context to " + suite.name());
    }

    private void adjustServerRandom(ServerHelloMessage message) {
        // Use the right Protocol Version ?
        if (tlsContext.getSelectedProtocolVersion() != ProtocolVersion.TLS13) {
            setServerRandomContext(message.getUnixTime().getValue(), message.getRandom().getValue());
        } else {
            tlsContext.setServerRandom(message.getRandom().getValue());
        }
        LOGGER.debug("Set ServerRandom in Context to " + ArrayConverter.bytesToHexString(tlsContext.getServerRandom()));
    }

    private void setServerRandomContext(byte[] unixTime, byte[] random) {
        tlsContext.setServerRandom(ArrayConverter.concatenate(unixTime, random));
    }

    private void adjustSelectedCompression(ServerHelloMessage message) {
        CompressionMethod method = CompressionMethod.getCompressionMethod(message.getSelectedCompressionMethod()
                .getValue());
        tlsContext.setSelectedCompressionMethod(method);
        LOGGER.debug("Set SelectedCompressionMethod in Context to " + method.name());
    }

    private void adjustSelectedSessionID(ServerHelloMessage message) {
        byte[] sessionID = message.getSessionId().getValue();
        tlsContext.setSessionID(sessionID);
        LOGGER.debug("Set SessionID in Context to " + ArrayConverter.bytesToHexString(sessionID, false));

    }

    private void adjustSelectedProtocolVersion(ServerHelloMessage message) {
        ProtocolVersion version = ProtocolVersion.getProtocolVersion(message.getProtocolVersion().getValue());
        tlsContext.setSelectedProtocolVersion(version);
        LOGGER.debug("Set SelectedProtocolVersion in Context to " + version.name());
    }

    private void adjustLastRecordVersion(ServerHelloMessage message) {
        ProtocolVersion version = ProtocolVersion.getProtocolVersion(message.getProtocolVersion().getValue());
        tlsContext.setLastRecordVersion(version);
        LOGGER.debug("Set LastRecordVersion in Context to " + version.name());
    }

    private void setRecordCipher() {
        LOGGER.debug("Setting new Cipher in RecordLayer");
        RecordCipher recordCipher = RecordCipherFactory.getRecordCipher(tlsContext);
        tlsContext.getRecordLayer().setRecordCipher(recordCipher);
    }
}
