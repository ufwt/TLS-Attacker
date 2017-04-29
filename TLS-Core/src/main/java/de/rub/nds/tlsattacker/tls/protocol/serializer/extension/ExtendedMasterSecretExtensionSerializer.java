/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.serializer.extension;

import de.rub.nds.tlsattacker.tls.protocol.message.extension.ExtendedMasterSecretExtensionMessage;

/**
 *
 * @author Matthias Terlinde <matthias.terlinde@rub.de>
 */
public class ExtendedMasterSecretExtensionSerializer extends ExtensionSerializer<ExtendedMasterSecretExtensionMessage> {

    public ExtendedMasterSecretExtensionSerializer(ExtendedMasterSecretExtensionMessage message) {
        super(message);
    }

    /**
     * Serializes the extended master secret extension. There is no data to
     * serialize; it is a "just present" extension.
     *
     * @return Serialized bytes of the extended master secret extension
     */
    @Override
    public byte[] serializeExtensionContent() {
        LOGGER.debug("Serialized the extended master secret extension.");
        return getAlreadySerialized();
    }

}
