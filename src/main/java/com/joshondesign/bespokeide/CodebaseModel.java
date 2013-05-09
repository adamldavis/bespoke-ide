package com.joshondesign.bespokeide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: josh
* Date: 6/23/12
* Time: 10:28 PM
* To change this template use File | Settings | File Templates.
*/
public class CodebaseModel {
    List<String> packageList = new ArrayList<>();
    Map<String,PackageModel> packageMap = new HashMap<>();

    public boolean hasPackage(String name) {
        return packageMap.containsKey(name);
    }

    public void addPackage(PackageModel pkg) {
        this.packageList.add(pkg.name);
        this.packageMap.put(pkg.name,pkg);
    }

    public PackageModel getPackage(String name) {
        return this.packageMap.get(name);
    }
}
