/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.parser;

import de.rub.nds.tlsattacker.tls.exceptions.ParserException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class ParserTest {

    private Parser parser;
    private Parser middleParser;

    public ParserTest() {
    }

    @Before
    public void setUp() {
        byte[] bytesToParse = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        parser = new ParserImpl(0, bytesToParse);
        middleParser = new ParserImpl(3, bytesToParse);
    }

    /**
     * Test of parseByteArrayField method, of class Parser.
     */
    @Test
    public void testParseByteField() {
        byte[] result = parser.parseByteArrayField(1);
        assertArrayEquals(result, new byte[]{0});
        result = parser.parseByteArrayField(2);
        assertArrayEquals(result, new byte[]{1, 2});
        result = middleParser.parseByteArrayField(1);
        assertArrayEquals(result, new byte[]{3});
        result = middleParser.parseByteArrayField(2);
        assertArrayEquals(result, new byte[]{4, 5});
    }
    
    /**
     * Test of parseSingleByteField method, of class Parser.
     */
    @Test
    public void testParseSingleByteField() {
        byte result = parser.parseByteField(1);
        assertEquals(result, 0);
        result = middleParser.parseByteField(1);
        assertEquals(result, 3);
    }

    /**
     * Test of parseIntField method, of class Parser.
     */
    @Test
    public void testParseIntField() {
        int result = parser.parseIntField(1);
        assertSame(result, 0);
        result = parser.parseIntField(2);
        assertTrue(result == 0x0102);
        result = middleParser.parseIntField(1);
        assertSame(result, 3);
        result = middleParser.parseIntField(2);
        assertTrue(result == 0x0405);
    }

    @Test(expected = ParserException.class)
    public void testParseIntFieldNegative() {
        parser.parseIntField(-123);
    }

    @Test(expected = ParserException.class)
    public void testParseIntFieldZero() {
        parser.parseIntField(0);
    }

    @Test(expected = ParserException.class)
    public void testParseByteFieldZero() {
        parser.parseByteArrayField(0);
    }

    @Test(expected = ParserException.class)
    public void testParseByteFieldNegative() {
        parser.parseByteArrayField(-123);
    }

    @Test(expected = ParserException.class)
    public void testParseSingleByteFieldNegative() {
        parser.parseByteArrayField(-123);
    }

    @Test(expected = ParserException.class)
    public void testParseSingleByteFieldZero() {
        parser.parseByteArrayField(0);
    }
    
    @Test
    public void testAlreadyParsed()
    {
        assertArrayEquals(parser.getAlreadyParsed(), new byte[0]);
        parser.parseIntField(1);
        assertArrayEquals(parser.getAlreadyParsed(), new byte[]{0});
        parser.parseIntField(3);
        assertArrayEquals(parser.getAlreadyParsed(), new byte[]{0,1,2,3});
    }
    
    @Test
    public void testAlreadyParsedMiddle()
    {
        assertArrayEquals(middleParser.getAlreadyParsed(), new byte[0]);
        middleParser.parseIntField(1);
        assertArrayEquals(middleParser.getAlreadyParsed(), new byte[]{3});
        middleParser.parseIntField(3);
        assertArrayEquals(middleParser.getAlreadyParsed(), new byte[]{3,4,5,6});
    }
    
    @Test(expected = ParserException.class)
    public void testConstructorException()
    {
        byte[] base = new byte[]{0,1};
        ParserImpl parser2 = new ParserImpl(3, base);
    }

    public class ParserImpl extends Parser {

        public ParserImpl(int i, byte[] a) {
            super(i, a);
        }

        @Override
        public Object parse() {
            return null;
        }
    }

}