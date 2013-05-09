package com.joshondesign.bespokeide;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import java.util.Locale;
import javax.swing.JTextArea;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 7/2/12
* Time: 1:43 PM
* To change this template use File | Settings | File Templates.
*/
class ConsoleHandler implements DiagnosticListener<JavaFileObject>, TaskListener {
    private JTextArea textarea;

    public ConsoleHandler(JTextArea main) {
        this.textarea = main;
    }

    public void report(Diagnostic<? extends JavaFileObject> d) {
        String msg = d.getKind() + " : "
                + d.getMessage(Locale.getDefault());
        textarea.append(msg+"\n");
    }

    public void started(TaskEvent e) {
        if(e.getKind() == TaskEvent.Kind.PARSE) {
            textarea.append("Parsing: "
                    + e.getSourceFile().getName()+"\n");
        }
        if(e.getKind() == TaskEvent.Kind.ANALYZE) {
            textarea.append("Analyzing: "
                    + e.getTypeElement().getQualifiedName()+"\n");
        }
        if(e.getKind() == TaskEvent.Kind.GENERATE) {
            textarea.append("Generating: "
                    + e.getTypeElement().getQualifiedName()+"\n");
        }
    }

    public void finished(TaskEvent e) {
    }
}
