/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.unit;

import java.io.File;
import java.util.Properties;
import org.h2.constant.ErrorCode;
import org.h2.engine.ConnectionInfo;
import org.h2.test.TestBase;

/**
 * Test the ConnectionInfo class.
 *
 * @author Kerry Sainsbury
 */
public class TestConnectionInfo extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String[] a) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
    public void test() throws Exception {
        testConnectInitError();
        testConnectionInfo();
        testName();
    }

    private void testConnectInitError() throws Exception {
        assertThrows(ErrorCode.SYNTAX_ERROR_2, this).
                getConnection("jdbc:h2:mem:;init=error");
        assertThrows(ErrorCode.IO_EXCEPTION_2, this).
                getConnection("jdbc:h2:mem:;init=runscript from 'wrong.file'");
    }

    private void testConnectionInfo() {
        Properties info = new Properties();
        ConnectionInfo connectionInfo = new ConnectionInfo(
                "jdbc:h2:mem:test" +
                        ";LOG=2" +
                        ";ACCESS_MODE_DATA=rws" +
                        ";INIT=CREATE this...\\;INSERT that..." +
                        ";IFEXISTS=TRUE",
                info);

        assertEquals("jdbc:h2:mem:test",
                connectionInfo.getURL());

        assertEquals("2",
                connectionInfo.getProperty("LOG", ""));
        assertEquals("rws",
                connectionInfo.getProperty("ACCESS_MODE_DATA", ""));
        assertEquals("CREATE this...;INSERT that...",
                connectionInfo.getProperty("INIT", ""));
        assertEquals("TRUE",
                connectionInfo.getProperty("IFEXISTS", ""));
        assertEquals("undefined",
                connectionInfo.getProperty("CACHE_TYPE", "undefined"));
    }

    private void testName() throws Exception {
        char differentFileSeparator = File.separatorChar == '/' ? '\\' : '/';
        ConnectionInfo connectionInfo = new ConnectionInfo("test" +
                differentFileSeparator + "subDir");
        File file = new File("test" + File.separatorChar + "subDir");
        assertEquals(file.getCanonicalPath().replace('\\', '/'),
                connectionInfo.getName());
    }

}
