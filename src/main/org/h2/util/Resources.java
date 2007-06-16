/*
 * Copyright 2004-2006 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class Resources {

    private static final HashMap FILES = new HashMap();

    static {
        ResourceData.load();
    }

    public static void main(String[] args) throws Exception {
        String inDir = args.length > 0 ? args[0] : null;
        String outDir = args.length > 1 ? args[1] : null;
        new Resources().run(inDir, outDir);
    }

    void run(String outDir, String inDir) throws Exception {
        if(outDir == null) {
            outDir = "bin";
        }
        if(inDir == null) {
            inDir = "src/main";
        }
        if(new File(outDir + "/org/h2/util").exists()) {
            String file = outDir + "/org/h2/util/ResourceData.java";
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println("package org.h2.util;");
            out.println("// Do not change this code manually");
            out.println("// This code is generated by " + getClass().getName());
            out.println("class ResourceData {");
            out.println("    public static void load() {");
            generate(out, inDir+"/org/h2/res", "org.h2");
            generate(out, inDir+"/org/h2/server/web/res", "org.h2.server.web");
            out.println("    }");
            out.println("}");
            out.close();
        }
    }

    void generate(PrintWriter out, String inDir, String packageName) throws Exception {
        File dir = new File(inDir);
        String[] list = dir.list();
        for(int i=0; list != null && i<list.length; i++) {
            File f = new File(dir, list[i]);
            if(!f.isFile()) {
                continue;
            }
            if(list[i].endsWith(".java")) {
                continue;
            }
            String name = "/" + packageName.replace('.', '/') + "/res/" + f.getName();
            // System.out.println(name+": "+f.length());
            InputStream in = new FileInputStream(f);
            byte[] buffer = IOUtils.readBytesAndClose(in, 0);
            String s = ByteUtils.convertToBinString(buffer);
            out.print("        Resources.add(" + StringUtils.quoteJavaString(name) + ", ");
            out.print("new String[]{");
            do {
                String s2;
                if(s.length() < 65000) {
                    s2 = s;
                    s = null;
                } else {
                    s2 = s.substring(0, 65000);
                    s = s.substring(65000);
                }
                out.print(StringUtils.quoteJavaString(s2));
                out.println(", ");
            } while(s != null);
            out.println("});");
        }
    }

    static void add(String name, String[] data) {
        StringBuffer buff = new StringBuffer();
        for(int i=0; i<data.length; i++) {
            buff.append(data[i]);
        }
        FILES.put(name, ByteUtils.convertBinStringToBytes(buff.toString()));
    }

    public static byte[] get(String name) throws IOException {
        byte[] data;
        if(FILES.size() == 0) {
            // TODO web: security (check what happens with files like 'lpt1.txt' on windows)
            InputStream in = Resources.class.getResourceAsStream(name);
            if(in == null) {
                data = null;
            } else {
                data = IOUtils.readBytesAndClose(in, 0);
            }
        } else {
            data = (byte[]) FILES.get(name);
        }
        return data == null ? new byte[0] : data;
    }
}
