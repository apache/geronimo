/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.generator;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Vector;


public class JavaGenerator implements Generator {
    protected GenOptions _genOptions;

    public JavaGenerator(GenOptions genOptions) {
        _genOptions = genOptions;
    }

    public GenOptions getGenOptions() {
        return _genOptions;
    }

    public void setGenOptions(GenOptions genOptions) {
        _genOptions = genOptions;
    }

    public void generate(JEntity e) {
        // Meaningless
    }

    public void generate(JPackage p)
            throws GenException {
        if (p == null) {
            return;
        }

        Vector v = p.getClasses();

        if (v != null && v.size() > 0) {
            int i;
            for (i = 0; i < v.size(); i++) {
                generate((JClass) v.elementAt(i));
            }
        }
    }

    public void generate(JClass c)
            throws GenException {
        if (c == null) {
            return;
        }

        String className = c.getName();
        String pkgName = c.getPackage().getName();

        pkgName = pkgName.replace('.', File.separatorChar);

        String fullName = pkgName + "/" + className;

        JavaWriter jw = new JavaWriter(_genOptions, fullName, ".java");

        jw.openFile();
        writeClass(jw, c);
        jw.closeFile();
    }

    protected void writeClass(JavaWriter jw, JClass c) {
        writeClassPackage(jw, c);
        writeClassImports(jw, c);

        writeClassClassDefn(jw, c);
        jw.begin();
        writeClassFields(jw, c);
        writeClassConstructors(jw, c);
        writeClassMethods(jw, c);
        jw.end();
    }

    protected void writeClassPackage(JavaWriter jw, JClass c) {
        if (c.getPackage().getName().length() > 0) {
            jw.newln();
            jw.println("package " + c.getPackage().getName() + ";");
        }
    }

    protected void writeClassImports(JavaWriter jw, JClass c) {
        Vector v = c.getImports();
        if (v != null && v.size() > 0) {
            int i;

            jw.newln();

            for (i = 0; i < v.size(); i++) {
                jw.println("import " + v.elementAt(i) + ";");
            }
        }
    }

    protected void writeClassClassDefn(JavaWriter jw, JClass c) {
        jw.newln();

        writeModifiers(jw, c.getModifiers());
        jw.println("class " + c.getName());

        if (c.getExtends() != null && c.getExtends().length() > 0) {
            jw.indent();
            jw.println("extends " + c.getBaseClassName());
            jw.outdent();
        }

        Vector v = c.getImplements();
        if (v != null && v.size() > 0) {
            int i;

            jw.indent();
            jw.print("implements ");
            jw.outdent();

            for (i = 0; i < v.size(); i++) {
                jw.print("" + v.elementAt(i));

                if (i + 1 != v.size()) {
                    jw.print(", ");
                }
            }
            jw.println("");
        }
    }

    protected void writeClassFields(JavaWriter jw, JClass c) {
        Vector v = c.getFields();
        if (v != null && v.size() > 0) {
            jw.comment("");
            jw.comment("Fields");
            jw.comment("");

            int i;
            JField f;
            for (i = 0; i < v.size(); i++) {
                f = (JField) v.elementAt(i);
                writeClassField(jw, c, f);
            }
        }
    }

    protected void writeClassField(JavaWriter jw, JClass c, JField f) {
        writeModifiers(jw, f.getModifiers());
        jw.print(f.getTypeDecl() + " " + f.getName());

        if (f.getInitExpression() != null) {
            jw.print(" = ");
            writeExpression(jw, f.getInitExpression());
        }

        jw.println(";");
    }

    protected void writeClassConstructors(JavaWriter jw, JClass c) {
        Vector v = c.getConstructors();
        if (v != null && v.size() > 0) {
            int i;
            JMethod m;

            jw.newln();

            jw.comment("");
            jw.comment("Constructors");
            jw.comment("");

            for (i = 0; i < v.size(); i++) {
                m = (JMethod) v.elementAt(i);
                writeClassMethod(jw, c, m);
            }
        }
    }

    protected void writeClassMethods(JavaWriter jw, JClass c) {
        Vector v = c.getMethods();
        if (v != null && v.size() > 0) {
            int i;
            JMethod m;

            jw.newln();

            jw.comment("");
            jw.comment("Methods");
            jw.comment("");

            for (i = 0; i < v.size(); i++) {
                jw.newln();
                m = (JMethod) v.elementAt(i);
                writeClassMethod(jw, c, m);
            }
        }
    }

    protected void writeClassMethod(JavaWriter jw, JClass c, JMethod m) {
        writeModifiers(jw, m.getModifiers());

        if (m instanceof JConstructor) {
            jw.print(c.getName());
        } else {
            //jw.print( m.getRCType() + " " + m.getName() );
            jw.print(m.getRT().getTypeName());

            if (m.getRT().isArray()) {
                jw.print("[]");
            }

            jw.print(" " + m.getName());
        }
        jw.print("(");

        JParameter p[] = m.getParms();
        if (p != null && p.length > 0) {
            int i;
            for (i = 0; i < p.length; i++) {
                jw.print(" " + p[i].getTypeDecl() + " " + p[i].getName());

                if (i + 1 != p.length) {
                    jw.print(",");
                }
            }
        }

        jw.print(" )");

        //String s[] = m.getThrownType();
        Class s[] = m.getThrown();
        if (s != null && s.length > 0) {
            int i;

            jw.print(" throws ");

            for (i = 0; i < s.length; i++) {
                jw.print(s[i].getName());

                if (i + 1 != s.length) {
                    jw.print(", ");
                }
            }
        }
        jw.println("");

        jw.begin();
        writeLocalVariables(jw, m.getLocalVariables());
        writeStatements(jw, m.getStatements());

        if (m.getBody() != null && m.getBody().length() > 0) {
            jw.println(m.getBody());
        }
        jw.end();
    }

    protected void writeLocalVariables(JavaWriter jw, Vector lv) {
        if (lv != null && lv.size() > 0) {
            int i;
            for (i = 0; i < lv.size(); i++) {
                writeLocalVariable(jw, (JLocalVariable) lv.elementAt(i));
            }
        }
    }

    protected void writeLocalVariable(JavaWriter jw, JLocalVariable lv) {
        jw.print(lv.getTypeDecl() + " " + lv.getName());

        if (lv.getInitExpression() != null) {
            jw.print(" = ");
            writeExpression(jw, lv.getInitExpression());
        }

        jw.println(";");
    }

    protected void writeStatements(JavaWriter jw, Vector sv) {
        if (sv != null && sv.size() > 0) {
            int i;
            for (i = 0; i < sv.size(); i++) {
                writeStatement(jw, (JStatement) sv.elementAt(i));
            }
        }
    }

    protected void writeModifiers(JavaWriter jw, int m) {
        String s = Modifier.toString(m);

        if (s != null && s.length() > 0) {
            jw.print(s + " ");
        }
    }

    protected void writeStatement(JavaWriter jw, JStatement s) {
        if (s instanceof JCaseStatement) {
            writeCaseStatement(jw, (JCaseStatement) s);
        } else if (s instanceof JCatchStatement) {
            writeCatchStatement(jw, (JCatchStatement) s);
        } else if (s instanceof JCodeStatement) {
            writeCodeStatement(jw, (JCodeStatement) s, true);
        } else if (s instanceof JDeclareStatement) {
            writeDeclareStatement(jw, (JDeclareStatement) s);
        } else if (s instanceof JElseStatement) {
            writeElseStatement(jw, (JElseStatement) s);
        } else if (s instanceof JElseIfStatement) {
            writeElseIfStatement(jw, (JElseIfStatement) s);
        } else if (s instanceof JIfElseIfElseStatement) {
            writeIfElseIfElseStatement(jw, (JIfElseIfElseStatement) s);
        } else if (s instanceof JFinallyStatement) {
            writeFinallyStatement(jw, (JFinallyStatement) s);
        } else if (s instanceof JForStatement) {
            writeForStatement(jw, (JForStatement) s);
        } else if (s instanceof JIfStatement) {
            writeIfStatement(jw, (JIfStatement) s);
        } else if (s instanceof JTryCatchFinallyStatement) {
            writeTryCatchFinallyStatement(jw, (JTryCatchFinallyStatement) s);
        } else if (s instanceof JSwitchStatement) {
            writeSwitchStatement(jw, (JSwitchStatement) s);
        } else if (s instanceof JTryStatement) {
            writeTryStatement(jw, (JTryStatement) s);
        } else if (s instanceof JBlockStatement) {
            // BlockStatemnet should be last since there are other subclasses of it.
            writeBlockStatement(jw, (JBlockStatement) s);
        } else {
            jw.comment("");
            jw.comment("Error: Unknown statement: " + s);
            jw.comment("");
        }
    }

    protected void writeBlockStatement(JavaWriter jw, JBlockStatement bs) {
        jw.begin();
        writeLocalVariables(jw, bs.getLocalVariables());
        writeStatements(jw, bs.getStatements());
        jw.end();
    }

    protected void writeCaseStatement(JavaWriter jw, JCaseStatement cs) {
        jw.print("case ");
        writeExpression(jw, cs.getExpression());
        jw.println(":");
        writeStatement(jw, cs.getStatement());
        jw.println("break;");
    }

    protected void writeCatchStatement(JavaWriter jw, JCatchStatement cs) {
        jw.println("catch( " + cs.getVariable().getTypeDecl() + " " + cs.getVariable().getName() + " )");
        writeBlockStatement(jw, cs);
        //writeStatement( jw, cs.getStatement() );
    }

    protected void writeCodeStatement(JavaWriter jw, JCodeStatement cs, boolean newLine) {
        jw.print(cs.getCode());

        if (newLine) {
            jw.newln();
        }

        //jw.print( cs.getCode() );
        //jw.println( ";" );
    }

    protected void writeDeclareStatement(JavaWriter jw, JDeclareStatement ds) {
        JVariable v = ds.getVariable();
        jw.print(v.getTypeDecl() + " " + v.getName());

        JExpression e = ds.getInitExpression();
        if (e != null) {
            jw.print(" = ");
            writeExpression(jw, e);
        }

        jw.println(";");
    }

    protected void writeElseStatement(JavaWriter jw, JElseStatement es) {
        if (es.hasStatements()) {
            jw.println("else");
            writeBlockStatement(jw, es);
        }
    }

    protected void writeElseIfStatement(JavaWriter jw, JElseIfStatement eis) {
        if (eis.hasStatements()) {
            jw.print("else ");
            writeIfStatement(jw, eis);
        }
    }

    protected void writeIfElseIfElseStatement(JavaWriter jw, JIfElseIfElseStatement ies) {
        writeIfStatement(jw, ies.getIfStatement());
    }

    protected void writeExpression(JavaWriter jw, JExpression e) {
        // TODO: not sure how I am going to do this but...

        if (e.getStatement() instanceof JCodeStatement) {
            JCodeStatement cs = (JCodeStatement) e.getStatement();
            writeCodeStatement(jw, cs, false);
            //jw.print( cs.getCode() );
        } else {
            writeStatement(jw, e.getStatement());
        }
    }

    protected void writeFinallyStatement(JavaWriter jw, JFinallyStatement fs) {
        if (fs.hasStatements()) {
            jw.println("finally");
            writeStatement(jw, fs.getStatement());
        }
    }

    protected void writeForStatement(JavaWriter jw, JForStatement fs) {
        jw.newln();
        jw.print("for (");
        writeStatement(jw, fs.getInitStatement());
        jw.print(";");
        writeExpression(jw, fs.getLoopExpression());
        writeStatement(jw, fs.getIterStatement());
        jw.println(")");
        writeBlockStatement(jw, fs);
        //writeBlockStatement( jw, fs.getStatement() );
    }

    protected void writeIfStatement(JavaWriter jw, JIfStatement is) {
        jw.newln();
        jw.print("if (");
        writeExpression(jw, is.getExpression());
        jw.println(")");
        writeBlockStatement(jw, is);
    }

    protected void writeSwitchStatement(JavaWriter jw, JSwitchStatement ss) {
        jw.newln();
        jw.print("switch (");
        writeExpression(jw, ss.getExpression());
        jw.println(")");
        jw.begin();
        writeStatements(jw, ss.getCases());
        jw.end();
    }

    protected void writeTryCatchFinallyStatement(JavaWriter jw, JTryCatchFinallyStatement tcfs) {
        writeStatement(jw, tcfs.getTryStatement());
        writeStatements(jw, tcfs.getCatches());
        writeStatement(jw, tcfs.getFinallyStatement());
    }

    protected void writeTryStatement(JavaWriter jw, JTryStatement ts) {
        jw.println("");
        jw.println("try");
        writeBlockStatement(jw, ts);
        //writeStatement( jw, ts.getStatement() );
    }

}
