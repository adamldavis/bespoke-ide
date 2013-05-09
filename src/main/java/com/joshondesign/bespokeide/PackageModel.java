package com.joshondesign.bespokeide;

import java.util.*;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 6/23/12
* Time: 10:28 PM
* To change this template use File | Settings | File Templates.
*/
public class PackageModel {
    public String name;
    public final Set<ClassModel> classSet = new HashSet<>();
    public final List<ClassModel> classList = new ArrayList<>();
    public final Map<String,ClassModel> classMap = new HashMap<>();

    public PackageModel(String name) {
        this.name = name;
    }

    public void add(ClassModel cls) {
        this.classSet.add(cls);
        this.classList.add(cls);
        this.classMap.put(cls.name,cls);
    }
    
    @Override
    public String toString() {
    	return name;
    }
}
