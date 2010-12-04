/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.constant;

import org.h2.engine.Constants;
import org.h2.message.TraceSystem;
import org.h2.util.MathUtils;

/**
 * The constants defined in this class are initialized from system properties.
 * Some system properties are per machine settings, and others are as a last
 * resort and temporary solution to work around a problem in the application or
 * database engine. Also, there are system properties to enable features that
 * are not yet fully tested or that are not backward compatible.
 * <p>
 * System properties can be set when starting the virtual machine:
 * </p>
 *
 * <pre>
 * java -Dh2.baseDir=/temp
 * </pre>
 *
 * They can be set within the application, but this must be done before loading
 * any classes of this database (before loading the JDBC driver):
 *
 * <pre>
 * System.setProperty(&quot;h2.baseDir&quot;, &quot;/temp&quot;);
 * </pre>
 */
public class SysProperties {

    /**
     * INTERNAL
     */
    public static final String H2_SCRIPT_DIRECTORY = "h2.scriptDirectory";

    /**
     * INTERNAL
     */
    public static final String H2_BROWSER = "h2.browser";

    /**
     * System property <code>file.encoding</code> (default: Cp1252).<br />
     * It is usually set by the system and is the default encoding used for the
     * RunScript and CSV tool.
     */
    public static final String FILE_ENCODING = getStringSetting("file.encoding", "Cp1252");

    /**
     * System property <code>file.separator</code> (default: /).<br />
     * It is usually set by the system, and used to build absolute file names.
     */
    public static final String FILE_SEPARATOR = getStringSetting("file.separator", "/");

    /**
     * System property <code>java.specification.version</code>.<br />
     * It is set by the system. Examples: 1.4, 1.5, 1.6.
     */
    public static final String JAVA_SPECIFICATION_VERSION = getStringSetting("java.specification.version", "1.4");

    /**
     * System property <code>line.separator</code> (default: \n).<br />
     * It is usually set by the system, and used by the script and trace tools.
     */
    public static final String LINE_SEPARATOR = getStringSetting("line.separator", "\n");

    /**
     * System property <code>user.home</code> (empty string if not set).<br />
     * It is usually set by the system, and used as a replacement for ~ in file
     * names.
     */
    public static final String USER_HOME = getStringSetting("user.home", "");

    /**
     * System property <code>h2.allowBigDecimalExtensions</code> (default:
     * false).<br />
     * When enabled, classes that extend BigDecimal are supported in
     * PreparedStatement.setBigDecimal.
     */
    public static final boolean ALLOW_BIG_DECIMAL_EXTENSIONS = getBooleanSetting("h2.allowBigDecimalExtensions", false);

    /**
     * System property <code>h2.allowedClasses</code> (default: *).<br />
     * Comma separated list of class names or prefixes.
     */
    public static final String ALLOWED_CLASSES = getStringSetting("h2.allowedClasses", "*");

    /**
     * System property <code>h2.browser</code> (default: null).<br />
     * The preferred browser to use. If not set, the default browser is used.
     * For Windows, to use the Internet Explorer, set this property to 'explorer'.
     * For Mac OS, if the default browser is not Safari and you want to use Safari,
     * use: <code>java -Dh2.browser="open,-a,Safari,%url" ...</code>.
     */
    public static final String BROWSER = getStringSetting(H2_BROWSER, null);

    /**
     * System property <code>h2.enableAnonymousSSL</code> (default: true).<br />
     * When using SSL connection, the anonymous cipher suite
     * SSL_DH_anon_WITH_RC4_128_MD5 should be enabled.
     */
    public static final boolean ENABLE_ANONYMOUS_SSL = getBooleanSetting("h2.enableAnonymousSSL", true);

    /**
     * System property <code>h2.bindAddress</code> (default: null).<br />
     * Comma separated list of class names or prefixes.
     */
    public static final String BIND_ADDRESS = getStringSetting("h2.bindAddress", null);

    /**
     * System property <code>h2.check</code> (default: true).<br />
     * Assertions in the database engine.
     */
    //## CHECK begin ##
    public static final boolean CHECK = getBooleanSetting("h2.check", true);
    //## CHECK end ##

    /*## NO_CHECK begin ##
    public static final boolean CHECK = false;
    ## NO_CHECK end ##*/

    /**
     * System property <code>h2.check2</code> (default: true).<br />
     * Additional assertions in the database engine.
     */
    //## CHECK begin ##
    public static final boolean CHECK2 = getBooleanSetting("h2.check2", false);
    //## CHECK end ##

    /*## NO_CHECK begin ##
    public static final boolean CHECK2 = false;
    ## NO_CHECK end ##*/

    /**
     * System property <code>h2.clientTraceDirectory</code> (default:
     * trace.db/).<br />
     * Directory where the trace files of the JDBC client are stored (only for
     * client / server).
     */
    public static final String CLIENT_TRACE_DIRECTORY = getStringSetting("h2.clientTraceDirectory", "trace.db/");

    /**
     * System property <code>h2.collatorCacheSize</code> (default: 32000).<br />
     * The cache size for collation keys (in elements). Used when a collator has
     * been set for the database.
     */
    public static final int COLLATOR_CACHE_SIZE = getIntSetting("h2.collatorCacheSize", 32000);

    /**
     * System property <code>h2.consoleStream</code> (default: true).<br />
     * H2 Console: stream query results.
     */
    public static final boolean CONSOLE_STREAM = getBooleanSetting("h2.consoleStream", true);

    /**
     * System property <code>h2.dataSourceTraceLevel</code> (default: 1).<br />
     * The trace level of the data source implementation. Default is 1 for
     * error.
     */
    public static final int DATASOURCE_TRACE_LEVEL = getIntSetting("h2.dataSourceTraceLevel", TraceSystem.ERROR);

    /**
     * System property <code>h2.delayWrongPasswordMin</code> (default: 250).<br />
     * The minimum delay in milliseconds before an exception is thrown for using
     * the wrong user name or password. This slows down brute force attacks. The
     * delay is reset to this value after a successful login. Unsuccessful
     * logins will double the time until DELAY_WRONG_PASSWORD_MAX.
     * To disable the delay, set this system property to 0.
     */
    public static final int DELAY_WRONG_PASSWORD_MIN = getIntSetting("h2.delayWrongPasswordMin", 250);

    /**
     * System property <code>h2.delayWrongPasswordMax</code> (default: 4000).<br />
     * The maximum delay in milliseconds before an exception is thrown for using
     * the wrong user name or password. This slows down brute force attacks. The
     * delay is reset after a successful login. The value 0 means there is no
     * maximum delay.
     */
    public static final int DELAY_WRONG_PASSWORD_MAX = getIntSetting("h2.delayWrongPasswordMax", 4000);

    /**
     * System property <code>h2.emptyPassword</code> (default: true).<br />
     * Don't use a secure hash if the user name and password are empty or not set.
     */
    public static final boolean EMPTY_PASSWORD = getBooleanSetting("h2.emptyPassword", true);

    /**
     * System property <code>h2.lobCloseBetweenReads</code> (default: false).<br />
     * Close LOB files between read operations.
     */
    public static boolean lobCloseBetweenReads = getBooleanSetting("h2.lobCloseBetweenReads", false);

    /**
     * System property <code>h2.lobFilesPerDirectory</code> (default: 256).<br />
     * Maximum number of LOB files per directory.
     */
    public static final int LOB_FILES_PER_DIRECTORY = getIntSetting("h2.lobFilesPerDirectory", 256);

    /**
     * System property <code>h2.lobInDatabase</code> (default: false).<br />
     * Store LOB files in the database.
     */
    public static final boolean LOB_IN_DATABASE = getBooleanSetting("h2.lobInDatabase", Constants.VERSION_MINOR >= 3);

    /**
     * System property <code>h2.lobClientMaxSizeMemory</code> (default: 65536).<br />
     * The maximum size of a LOB object to keep in memory on the client side
     * when using the server mode.
     */
    public static final int LOB_CLIENT_MAX_SIZE_MEMORY = getIntSetting("h2.lobClientMaxSizeMemory", 65536);

    /**
     * System property <code>h2.maxFileRetry</code> (default: 16).<br />
     * Number of times to retry file delete and rename. in Windows, files can't
     * be deleted if they are open. Waiting a bit can help (sometimes the
     * Windows Explorer opens the files for a short time) may help. Sometimes,
     * running garbage collection may close files if the user forgot to call
     * Connection.close() or InputStream.close().
     */
    public static final int MAX_FILE_RETRY = Math.max(1, getIntSetting("h2.maxFileRetry", 16));

    /**
     * System property <code>h2.maxReconnect</code> (default: 3).<br />
     * The maximum number of tries to reconnect in a row.
     */
    public static final int MAX_RECONNECT = getIntSetting("h2.maxReconnect", 3);

    /**
     * System property <code>h2.maxTraceDataLength</code> (default: 65535).<br />
     * The maximum size of a LOB value that is written as data to the trace system.
     */
    public static final long MAX_TRACE_DATA_LENGTH = getIntSetting("h2.maxTraceDataLength", 65535);

    /**
     * System property <code>h2.minColumnNameMap</code> (default: 3).<br />
     * The minimum number of columns where a hash table is created when result set
     * methods with column name (instead of column index) parameter are called.
     */
    public static final int MIN_COLUMN_NAME_MAP = getIntSetting("h2.minColumnNameMap", 3);

    /**
     * System property <code>h2.nioLoadMapped</code> (default: false).<br />
     * If the mapped buffer should be loaded when the file is opened.
     * This can improve performance.
     */
    public static final boolean NIO_LOAD_MAPPED = getBooleanSetting("h2.nioLoadMapped", false);

    /**
     * System property <code>h2.nioCleanerHack</code> (default: false).<br />
     * If enabled, use the reflection hack to un-map the mapped file if
     * possible. If disabled, System.gc() is called in a loop until the object
     * is garbage collected. See also
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
     */
    public static final boolean NIO_CLEANER_HACK = getBooleanSetting("h2.nioCleanerHack", false);

    /**
     * System property <code>h2.objectCache</code> (default: true).<br />
     * Cache commonly used values (numbers, strings). There is a shared cache
     * for all values.
     */
    public static final boolean OBJECT_CACHE = getBooleanSetting("h2.objectCache", true);

    /**
     * System property <code>h2.objectCacheMaxPerElementSize</code> (default:
     * 4096).<br />
     * The maximum size (precision) of an object in the cache.
     */
    public static final int OBJECT_CACHE_MAX_PER_ELEMENT_SIZE = getIntSetting("h2.objectCacheMaxPerElementSize", 4096);

    /**
     * System property <code>h2.objectCacheSize</code> (default: 1024).<br />
     * The maximum number of objects in the cache.
     * This value must be a power of 2.
     */
    public static final int OBJECT_CACHE_SIZE = MathUtils.nextPowerOf2(getIntSetting("h2.objectCacheSize", 1024));

    /**
     * System property <code>h2.pgClientEncoding</code> (default: UTF-8).<br />
     * Default client encoding for PG server. It is used if the client does not
     * sends his encoding.
     */
    public static final String PG_DEFAULT_CLIENT_ENCODING = getStringSetting("h2.pgClientEncoding", "UTF-8");

    /**
     * System property <code>h2.prefixTempFile</code> (default: h2.temp).<br />
     * The prefix for temporary files in the temp directory.
     */
    public static final String PREFIX_TEMP_FILE = getStringSetting("h2.prefixTempFile", "h2.temp");

    /**
     * System property <code>h2.returnLobObjects</code> (default: true).<br />
     * When true, ResultSet.getObject for CLOB or BLOB will return a
     * java.sql.Clob / java.sql.Blob object. When set to false, it will return a
     * java.io.Reader / java.io.InputStream.
     */
    public static final boolean RETURN_LOB_OBJECTS = getBooleanSetting("h2.returnLobObjects", true);

    /**
     * System property <code>h2.runFinalize</code> (default: true).<br />
     * Run finalizers to detect unclosed connections.
     */
    public static boolean runFinalize = getBooleanSetting("h2.runFinalize", true);

    /**
     * System property <code>h2.serverCachedObjects</code> (default: 64).<br />
     * TCP Server: number of cached objects per session.
     */
    public static final int SERVER_CACHED_OBJECTS = getIntSetting("h2.serverCachedObjects", 64);

    /**
     * System property <code>h2.serverResultSetFetchSize</code>
     * (default: 100).<br />
     * The default result set fetch size when using the server mode.
     */
    public static final int SERVER_RESULT_SET_FETCH_SIZE = getIntSetting("h2.serverResultSetFetchSize", 100);

    /**
     * System property <code>h2.socketConnectRetry</code> (default: 16).<br />
     * The number of times to retry opening a socket. Windows sometimes fails
     * to open a socket, see bug
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6213296
     */
    public static final int SOCKET_CONNECT_RETRY = getIntSetting("h2.socketConnectRetry", 16);

    /**
     * System property <code>h2.socketConnectTimeout</code> (default: 2000).<br />
     * The timeout in milliseconds to connect to a server.
     */
    public static final int SOCKET_CONNECT_TIMEOUT = getIntSetting("h2.socketConnectTimeout", 2000);

    /**
     * System property <code>h2.sortNullsHigh</code> (default: false).<br />
     * Invert the default sorting behavior for NULL, such that NULL
     * is at the end of a result set in an ascending sort and at
     * the beginning of a result set in a descending sort.
     */
    public static final boolean SORT_NULLS_HIGH = getBooleanSetting("h2.sortNullsHigh", false);

    /**
     * System property <code>h2.splitFileSizeShift</code> (default: 30).<br />
     * The maximum file size of a split file is 1L &lt;&lt; x.
     */
    public static final long SPLIT_FILE_SIZE_SHIFT = getIntSetting("h2.splitFileSizeShift", 30);

    /**
     * System property <code>h2.syncMethod</code> (default: sync).<br />
     * What method to call when closing the database, on checkpoint, and on
     * CHECKPOINT SYNC. The following options are supported:
     * "sync" (default): RandomAccessFile.getFD().sync();
     * "force": RandomAccessFile.getChannel().force(true);
     * "forceFalse": RandomAccessFile.getChannel().force(false);
     * "": do not call a method (fast but there is a risk of data loss
     * on power failure).
     */
    public static final String SYNC_METHOD = getStringSetting("h2.syncMethod", "sync");

    /**
     * System property <code>h2.traceIO</code> (default: false).<br />
     * Trace all I/O operations.
     */
    public static final boolean TRACE_IO = getBooleanSetting("h2.traceIO", false);

    /**
     * System property <code>h2.webMaxValueLength</code> (default: 100000).<br />
     * The H2 Console will abbreviate (truncate) result values larger than this size.
     * The data in the database is not truncated, it is only to avoid out of memory
     * in the H2 Console application.
     */
    public static final int WEB_MAX_VALUE_LENGTH = getIntSetting("h2.webMaxValueLength", 100000);

    private static final String H2_BASE_DIR = "h2.baseDir";

    private SysProperties() {
        // utility class
    }

    private static boolean getBooleanSetting(String name, boolean defaultValue) {
        String s = getProperty(name);
        if (s != null) {
            try {
                return Boolean.valueOf(s).booleanValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    private static String getProperty(String name) {
        try {
            return System.getProperty(name);
        } catch (Exception e) {
            // SecurityException
            // applets may not do that - ignore
            return null;
        }
    }

    /**
     * INTERNAL
     */
    public static String getStringSetting(String name, String defaultValue) {
        String s = getProperty(name);
        return s == null ? defaultValue : s;
    }

    /**
     * INTERNAL
     */
    public static int getIntSetting(String name, int defaultValue) {
        String s = getProperty(name);
        if (s != null) {
            try {
                return Integer.decode(s).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    /**
     * INTERNAL
     */
    public static void setBaseDir(String dir) {
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        System.setProperty(H2_BASE_DIR, dir);
    }

    /**
     * INTERNAL
     */
    public static String getBaseDir() {
        return getStringSetting(H2_BASE_DIR, null);
    }

    /**
     * System property <code>h2.scriptDirectory</code> (default: empty
     * string).<br />
     * Relative or absolute directory where the script files are stored to or
     * read from.
     *
     * @return the current value
     */
    public static String getScriptDirectory() {
        return getStringSetting(H2_SCRIPT_DIRECTORY, "");
    }

}
