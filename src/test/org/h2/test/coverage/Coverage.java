/*
 * Copyright 2004-2006 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.coverage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Tool to instrument java files with profiler calls. The tool can be used for
 * profiling an application and for coverage testing. This class is not used at
 * runtime of the tested application.
 */
public class Coverage {
    static final String IMPORT = "import " + Coverage.class.getPackage().getName() + ".Profile";
    ArrayList files = new ArrayList();
    ArrayList exclude = new ArrayList();
    Tokenizer tokenizer;
    Writer writer;
    Writer data;
    String token = "";
    String add = "";
    String file;
    int index;
    int indent;
    int line;
    String last;
    String word, function;
    boolean perClass;
    boolean perFunction = true;

    void printUsage() {
        System.out
                .println("Usage:\n"
                        + "- copy all your source files to another directory\n"
                        + "  (be careful, they will be modified - don't take originals!)\n"
                        + "- java " + getClass().getName() + " <directory>\n"
                        + "  this will modified the source code and create 'profile.txt'\n"
                        + "- compile the modified source files\n"
                        + "- run your main application\n"
                        + "- after the application exits, a file 'notCovered.txt' is created,\n"
                        + "  which contains the class names, function names and line numbers\n"
                        + "  of code that has not been covered\n\n"
                        + "Options:\n" 
                        + "-r     recurse all subdirectories\n"
                        + "-e     exclude files\n"
                        + "-c     coverage on a per-class basis\n"
                        + "-f     coverage on a per-function basis\n"
                        + "<dir>  directory name (. for current directory)");
    }

    public static void main(String[] arg) {
        (new Coverage()).run(arg);
    }

    void run(String[] arg) {
        if (arg.length == 0 || arg[0].equals("-?")) {
            printUsage();
            return;
        }
        Coverage c = new Coverage();
        int recurse = 1;
        for (int i = 0; i < arg.length; i++) {
            String s = arg[i];      
            if (s.equals("-r")) {
                // maximum recurse is 100 subdirectories, that should be enough
                recurse = 100;
            } else if (s.equals("-c")) {
                c.perClass = true;
            } else if (s.equals("-f")) {
                c.perFunction = true;
            } else if (s.equals("-e")) {
                c.addExclude(arg[++i]);
            } else {
                c.addDir(s, recurse);
            }
        }
        try {
            c.data = new BufferedWriter(new FileWriter("profile.txt"));
            c.processAll();
            c.data.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void addExclude(String file) {
        exclude.add(file);
    }
    
    boolean isExcluded(String s) {
        for(int i=0; i<exclude.size(); i++) {
            if(s.startsWith(exclude.get(i).toString())) {
                return true;
            }
        }
        return false;
    }

    void addDir(String path, int recurse) {
        File f = new File(path);
        if (f.isFile() && path.endsWith(".java")) {
            if(!isExcluded(path)) {
                files.add(path);
            }
        } else if (f.isDirectory() && recurse > 0) {
            String[] list = f.list();
            for (int i = 0; i < list.length; i++) {
                addDir(path + "/" + list[i], recurse - 1);
            }
        }
    }

    void processAll() {
        int len = files.size();
        long time = System.currentTimeMillis();
        for (int i = 0; i < len; i++) {
            long t2 = System.currentTimeMillis();
            if (t2 - time > 1000 || i >= len - 1) {
                System.out.println((i + 1) + " of " + len + " " + (100 * i / len) + "%");
                time = t2;
            }
            String fileName = (String) files.get(i);
            processFile(fileName);
        }
    }

    void processFile(String name) {
        file = name;
        int i;
        i = file.lastIndexOf('.');
        if (i != -1) {
            file = file.substring(0, i);
        }
        while(true) {
            i = file.indexOf('/');
            if(i < 0) {
                i = file.indexOf('\\');
            }
            if(i<0) {
                break;
            }
            file = file.substring(0, i) + "." +file.substring(i+1);
        }
        if (name.endsWith("Coverage.java") || name.endsWith("Tokenizer.java")
                || name.endsWith("Profile.java")) {
            return;
        }
        File f = new File(name);
        File fileNew = new File(name + ".new");
        try {
            writer = new BufferedWriter(new FileWriter(fileNew));
            Reader r = new BufferedReader(new FileReader(f));
            tokenizer = new Tokenizer(r);
            indent = 0;
            try {
                process();
            } catch (Exception e) {
                r.close();
                writer.close();
                e.printStackTrace();
                printError(e.getMessage());
                throw e;
            }
            r.close();
            writer.close();
            File backup = new File(name + ".bak");
            backup.delete();
            f.renameTo(backup);
            File copy = new File(name);
            fileNew.renameTo(copy);
            if (perClass) {
                nextDebug();
            }
        } catch (Exception e) {
            e.printStackTrace();
            printError(e.getMessage());
        }
    }

    void read() throws Exception {
        last = token;
        String write = token;
        token = null;
        tokenizer.initToken();
        int i = tokenizer.nextToken();
        if (i != Tokenizer.TYPE_EOF) {
            token = tokenizer.getString();
            if (token == null) {
                token = "" + ((char) i);
            } else if (i == '\'') {
                //mToken="'"+getEscape(mToken)+"'";
                token = tokenizer.getToken();
            } else if (i == '\"') {
                //mToken="\""+getEscape(mToken)+"\"";
                token = tokenizer.getToken();
            } else {
                if (write == null) {
                    write = "";
                } else {
                    write = write + " ";
                }
            }
        }
        if (write == null
                || (!write.equals("else ") && !write.equals("else")
                        && !write.equals("super ") && !write.equals("super")
                        && !write.equals("this ") && !write.equals("this")
                        && !write.equals("} ") && !write.equals("}"))) {
            if (add != null && !add.equals("")) {
                writeLine();
                write(add);
                if (!perClass) {
                    nextDebug();
                }
            }
        }
        add = "";
        if (write != null) {
            write(write);
        }
    }

    void readThis(String s) throws Exception {
        if (!token.equals(s)) {
            throw new Exception("Expected: " + s + " got:" + token);
        }
        read();
    }

    void process() throws Exception {
        boolean imp = false;
        read();
        do {
            while (true) {
                if (token == null || token.equals("{")) {
                    break;
                } else if (token.equals(";")) {
                    if (!imp) {
                        write(";" + IMPORT);
                        imp = true;
                    }
                }
                read();
            }
            processClass();
        } while (token != null);
    }

    void processInit() throws Exception {
        do {
            if (token.equals("{")) {
                read();
                processInit();
            } else if (token.equals("}")) {
                read();
                return;
            } else {
                read();
            }
        } while (true);
    }

    void processClass() throws Exception {
        int type = 0;
        while (true) {
            if (token == null) {
                break;
            } else if (token.equals("class")) {
                read();
                type = 1;
            } else if (token.equals("=")) {
                read();
                type = 2;
            } else if (token.equals("static")) {
                word = "static";
                read();
                type = 3;
            } else if (token.equals("(")) {
                word = last + "(";
                read();
                if (!token.equals(")")) {
                    word = word + token;
                }
                type = 3;
            } else if (token.equals(",")) {
                read();
                word = word + "," + token;
            } else if (token.equals(")")) {
                word = word + ")";
                read();
            } else if (token.equals(";")) {
                read();
                type = 0;
            } else if (token.equals("{")) {
                read();
                if (type == 1) {
                    processClass();
                } else if (type == 2) {
                    processInit();
                } else if (type == 3) {
                    writeLine();
                    setLine();
                    processFunction();
                    writeLine();
                }
            } else if (token.equals("}")) {
                read();
                break;
            } else {
                read();
            }
        }
    }

    void processBracket() throws Exception {
        do {
            if (token.equals("(")) {
                read();
                processBracket();
            } else if (token.equals(")")) {
                read();
                return;
            } else {
                read();
            }
        } while (true);
    }

    void processFunction() throws Exception {
        function = word;
        writeLine();
        do {
            processStatement();
        } while (!token.equals("}"));
        read();
        writeLine();
    }

    void processBlockOrStatement() throws Exception {
        if (!token.equals("{")) {
            write("{ //++");
            writeLine();
            setLine();
            processStatement();
            write("} //++");
            writeLine();
        } else {
            read();
            setLine();
            processFunction();
        }
    }

    void processStatement() throws Exception {
        while (true) {
            if (token.equals("while") || token.equals("for")
                    || token.equals("synchronized")) {
                read();
                readThis("(");
                processBracket();
                indent++;
                processBlockOrStatement();
                indent--;
                return;
            } else if (token.equals("if")) {
                read();
                readThis("(");
                processBracket();
                indent++;
                processBlockOrStatement();
                indent--;
                if (token.equals("else")) {
                    read();
                    indent++;
                    processBlockOrStatement();
                    indent--;
                }
                return;
            } else if (token.equals("try")) {
                read();
                indent++;
                processBlockOrStatement();
                indent--;
                while (true) {
                    if (token.equals("catch")) {
                        read();
                        readThis("(");
                        processBracket();
                        indent++;
                        processBlockOrStatement();
                        indent--;
                    } else if (token.equals("finally")) {
                        read();
                        indent++;
                        processBlockOrStatement();
                        indent--;
                    } else {
                        break;
                    }
                }
                return;
            } else if (token.equals("{")) {
                if (last.equals(")")) {
                    // process anonymous inner classes (this is a hack)
                    read();
                    processClass();
                    return;
                } else if (last.equals("]")) {
                    // process object array initialization (another hack)
                    while (!token.equals("}")) {
                        read();
                    }
                    read();
                    return;
                }
                indent++;
                processBlockOrStatement();
                indent--;
                return;
            } else if (token.equals("do")) {
                read();
                indent++;
                processBlockOrStatement();
                readThis("while");
                readThis("(");
                processBracket();
                readThis(";");
                setLine();
                indent--;
                return;
            } else if (token.equals("case")) {
                add = "";
                read();
                while (!token.equals(":")) {
                    read();
                }
                read();
                setLine();
            } else if (token.equals("default")) {
                add = "";
                read();
                readThis(":");
                setLine();
            } else if (token.equals("switch")) {
                read();
                readThis("(");
                processBracket();
                indent++;
                processBlockOrStatement();
                indent--;
                return;
            } else if (token.equals("class")) {
                read();
                processClass();
                return;
            } else if (token.equals("(")) {
                read();
                processBracket();
            } else if (token.equals("=")) {
                read();
                if (token.equals("{")) {
                    read();
                    processInit();
                }
            } else if (token.equals(";")) {
                read();
                setLine();
                return;
            } else if (token.equals("}")) {
                return;
            } else {
                read();
            }
        }
    }

    void setLine() throws Exception {
        add += "Profile.visit(" + index + ");";
        line = tokenizer.getLine();
    }

    void nextDebug() throws Exception {
        if (perFunction) {
            int i = function.indexOf("(");
            String func = i<0 ? function : function.substring(0, i);
            String fileLine = file + "." + func +"(";
            i = file.lastIndexOf('.');
            String className = i<0 ? file : file.substring(i+1);
            fileLine += className + ".java:" + line + ")";
            data.write(fileLine + " " + last + "\r\n");
        } else {
            data.write(file + " " + line + "\r\n");
        }
        index++;
    }

    void writeLine() throws Exception {
        write("\r\n");
        for (int i = 0; i < indent; i++) {
            writer.write(' ');
        }
    }

    void write(String s) throws Exception {
        writer.write(s);
        //System.out.print(s);
    }

    void printError(String error) {
        System.out.println("");
        System.out.println("File:" + file);
        System.out.println("ERROR: " + error);
    }
}

