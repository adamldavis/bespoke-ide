package com.joshondesign.bespokeide;

import java.util.*;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 6/23/12
* Time: 10:28 PM
* To change this template use File | Settings | File Templates.
*/
public  class ClassModel implements Comparable<ClassModel> {
    public String name;
    private final List<MethodModel> methods = new ArrayList<>();
    private final Map<String,MethodModel> methodMap = new HashMap<>();

    public void add(MethodModel meth) {
        this.methodMap.put(meth.getUniqueName(),meth);
        this.methods.add(meth);
    }

    public int compareTo(ClassModel classModel) {
        return this.name.compareTo(classModel.name);
    }

    public int getMethodCount() {
        return this.methods.size();
    }

    public MethodModel getMethod(int i) {
        return this.methods.get(i);
    }

    public void sort() {
        Collections.sort(methods);
    }

    public MethodModel getMethod(String methname) {
        return this.methodMap.get(methname);
    }
    
    @Override
    public String toString() {
    	return name;
    }
}
