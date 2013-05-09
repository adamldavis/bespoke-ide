package com.joshondesign.bespokeide;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class CurrentMethodScanner extends TreePathScanner<Void,Void> {
    private int pos;
    private Trees trees;
    private MethodModel currentMethod;
    public String deepestPath;
    private Main main;
    private MethodModel invokedMethod;
    private Elements elements;
    private Types types;
    private ProcessingEnvironment env;

    public CurrentMethodScanner(int caretPosition, MethodModel currentMethod, Trees trees, Main main, ProcessingEnvironment env) {
        this.pos = caretPosition;
        this.trees = trees;
        this.currentMethod = currentMethod;
        this.main = main;
        this.env = env;
        this.elements = env.getElementUtils();
        this.types = env.getTypeUtils();
    }


    @Override
    public Void scan(Tree elem, Void aVoid) {
        if(elem != null) {
            if(containsPosition(elem, pos)) {
                saveLocation(elem);
            }
        }
        return super.scan(elem, aVoid);
    }

    private boolean containsPosition(Tree tree, long pos) {
        TreePath pth = getCurrentPath();
        if(pth == null) return false;
        CompilationUnitTree unit = pth.getCompilationUnit();
        if(unit.getSourceFile().equals(currentMethod.compUnit.getSourceFile())) {
            SourcePositions sp = trees.getSourcePositions();
            long realpos = pos + currentMethod.start;
            long start = sp.getStartPosition(unit,tree);
            long end =   sp.getEndPosition(unit,tree);
            if(realpos >= start && realpos < end) {
                return true;
            }
        }
        return false;
    }
    
    private void saveLocation(Tree tree) {
        if(tree.getKind() == Tree.Kind.NEW_CLASS) {
            //can't make this work yet
        }
        if(tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree methInvo = (MethodInvocationTree) tree;
            if(methInvo.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                findMemberSelect((MemberSelectTree)methInvo.getMethodSelect());
            }
            //if(methInvo.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                //IdentifierTree idt = (IdentifierTree) methInvo.getMethodSelect();
                //TypeMirror mirror = getMirror(methInvo.getMethodSelect());
                //ClassModel clazz = findClassModel(mirror);
                //if(mirror.getKind() == TypeKind.EXECUTABLE) {
                    //ExecutableType ex = (ExecutableType) mirror;
                    //String methName = calculateMethodName(ex,idt.getName());
                    //p("meth name = " + methName);
                    //p("clazz = " + clazz);
                //}
            //}
        }
        if(tree.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree memberSelectTree = (MemberSelectTree) tree;
            findMemberSelect(memberSelectTree);
        }

        deepestPath = printPath(getCurrentPath(),tree);
    }

/*
    private TypeMirror getMirror(Tree idt) {
        TreePath curr = getCurrentPath();
        TreePath pth = TreePath.getPath(curr.getCompilationUnit(),idt);
        return trees.getTypeMirror(pth);
    }
  */
    private String calculateMethodName(ExecutableType ex, Name id) {
        StringBuffer sb = new StringBuffer();
        sb.append(id.toString());
        sb.append("(");
        for(TypeMirror tp : ex.getParameterTypes()){
            if(tp.getKind() == TypeKind.DECLARED) {
                DeclaredType tp2 = (DeclaredType) tp;
                sb.append(" ");
                sb.append(tp2.asElement().getSimpleName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private void findMemberSelect(MemberSelectTree memberSelectTree) {
        ClassModel clazz = findClassModel(memberSelectTree);
        if(clazz != null) {
            TreePath curr = getCurrentPath();
            TreePath pth = TreePath.getPath(curr.getCompilationUnit(),memberSelectTree);
            TypeMirror mirror = trees.getTypeMirror(pth);
            if(mirror.getKind() != TypeKind.EXECUTABLE) return;
            ExecutableType ex = (ExecutableType) mirror;

            String methName = calculateMethodName(ex,memberSelectTree.getIdentifier());
            MethodModel methodModel = clazz.getMethod(methName);
            if(methodModel != null) {
                invokedMethod = methodModel;
            }
        }        
    }


    private ClassModel findClassModel(MemberSelectTree tt) {
        ExpressionTree ex = tt.getExpression();
        if(ex.getKind() != Tree.Kind.IDENTIFIER) return null;

        IdentifierTree id = (IdentifierTree) ex;
        TreePath curr = getCurrentPath();
        TypeMirror type2 = trees.getTypeMirror(curr.getPath(curr.getCompilationUnit(), id));
        if(type2.getKind() != TypeKind.DECLARED) return null;
        DeclaredType type = (DeclaredType) type2;
        TypeElement e = (TypeElement) type.asElement();

        if(e.getNestingKind() == NestingKind.TOP_LEVEL) {
            String pkgname = elements.getPackageOf(e).getQualifiedName().toString();
            String className = e.getSimpleName().toString();
            return findClassModel(pkgname,className);
        }
        return null;
    }

    /*
    private ClassModel findClassModel(TypeMirror mirror) {
        p("elem = " + types.asElement(mirror));
        Element e = types.asElement(mirror);
        if(e == null) return null;
        p("kind = " + e.getKind());
        String pkgname = elements.getPackageOf(e).getQualifiedName().toString();
        p("pkg name = " + pkgname);
        String classname = e.getSimpleName().toString();
        p("classname = " + classname);
        return findClassModel(pkgname,classname);
    }
    */
    private ClassModel findClassModel(String pkgname, String className) {
        if(main.packages.hasPackage(pkgname)) {
            PackageModel pkgmodel = main.packages.getPackage(pkgname);
            if(pkgmodel.classMap.containsKey(className)) {
                ClassModel clssmodel = pkgmodel.classMap.get(className);
                return clssmodel;
            }
        }
        return null;
    }

    private void p(TreePath pth) {
        p("type = " + trees.getTypeMirror(pth));
        for(Tree tree : pth) {
            p("   path = " + tree.getKind());
            if(tree.getKind() == Tree.Kind.VARIABLE) {
                VariableTree t = (VariableTree) tree;
                //p("init = " + t.getInitializer());
            }
        }
    }

    private void p(String s) {
        System.out.println(s);
    }

    private String printPath(TreePath pth, Tree tree) {
        String spath = tree.getKind().toString();
        if(pth.getParentPath() == null) return spath;

        return printPath(pth.getParentPath(),pth.getLeaf()) + " > " + spath;
    }

    public String getDeepestPath() {
        return deepestPath;
    }

    public MethodModel getInvokedMethod() {
        return invokedMethod;
    }
}
