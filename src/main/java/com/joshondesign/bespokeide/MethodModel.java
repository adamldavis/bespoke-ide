package com.joshondesign.bespokeide;

import com.sun.source.tree.CompilationUnitTree;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 6/23/12
* Time: 10:29 PM
* To change this template use File | Settings | File Templates.
*/
public class MethodModel implements Comparable<MethodModel> {
    public String name = "";
    public CharSequence body = "";
    public CompilationUnitTree compUnit;
    public long start;
    public long end;
    private final List<String> varTypes = new ArrayList<>();
    private final List<String> varNames = new ArrayList<>();

    public MethodModel(String name) {
        this.name = name;
    }

    public int compareTo(MethodModel methodModel) {
        return this.name.compareTo(methodModel.name);
    }

    public String getUniqueName() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("(");
        for(String name : varTypes) {
            sb.append(" ");
            sb.append(name);
        }
        sb.append(")");
        return sb.toString();
    }

    public void addVariable(String name, String typeString) {
        this.varTypes.add(typeString);
        this.varNames.add(name);
    }
    
    @Override
    public String toString() {
    	return name;
    }

}
