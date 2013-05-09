package com.joshondesign.bespokeide;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("*")
public class FullCodeProcessor extends AbstractProcessor {
    private Trees trees;
    private final CodebaseModel codebase = new CodebaseModel();
    private PackageModel currentPackage;
    private ClassModel currrentClass;
    private ProcessingEnvironment env;
    Types types;

    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        trees = Trees.instance(processingEnvironment);
        this.env = processingEnvironment;
        this.types = this.env.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> types, RoundEnvironment env) {
        if(env.processingOver()) return false;

        final TreePathScanner<Void, Void> scanner = new CodeScanner();

        for(Element elem : env.getRootElements()) {
        	final PackageElement pkg = (PackageElement) elem.getEnclosingElement();
        	
            if(!codebase.hasPackage(pkg.getQualifiedName().toString())) {
                codebase.addPackage(new PackageModel(pkg.getQualifiedName().toString()));
            }
            currentPackage = codebase.getPackage(pkg.getQualifiedName().toString());
            TreePath path = trees.getPath(elem);
            scanner.scan(path,null);
        }

        //sort everything
        Collections.sort(codebase.packageList);
        for(PackageModel pkg : codebase.packageMap.values()) {
            Collections.sort(pkg.classList);
            for(ClassModel cls : pkg.classList) {
                cls.sort();
            }
        }


        return false;
    }

    public CodebaseModel getPackages() {
        return codebase;
    }

    public ProcessingEnvironment getEnv() {
        return env;
    }

    private final class CodeScanner extends TreePathScanner<Void, Void> {

        @Override
        public Void visitClass(ClassTree classTree, Void aVoid) {
            String clsName = classTree.getSimpleName().toString();
            currrentClass = new ClassModel();
            currrentClass.name = clsName;
            currentPackage.add(currrentClass);
            return super.visitClass(classTree, aVoid);
        }

        @Override
        public Void visitMethod(MethodTree methodTree, Void aVoid) {
        	final MethodModel meth = new MethodModel(methodTree.getName().toString());
        	
            for(VariableTree var : methodTree.getParameters()) {
                String typeString = "";
                if(var.getType().getKind() == Tree.Kind.IDENTIFIER) {
                    IdentifierTree identifierTree = (IdentifierTree) var.getType();
                    typeString = identifierTree.getName().toString();
                }
                if(var.getType().getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                    PrimitiveTypeTree primitiveTypeTree = (PrimitiveTypeTree) var.getType();
                    typeString = primitiveTypeTree.getPrimitiveTypeKind().name();
                }
                meth.addVariable(var.getName().toString(),typeString);
            }

            final TreePath pth = getCurrentPath();
            try {
            	final SourcePositions sp = trees.getSourcePositions();
            	final CompilationUnitTree unit = pth.getCompilationUnit();
            	final long start = sp.getStartPosition(unit, methodTree);
            	final long end = sp.getEndPosition(unit, methodTree);
            	final CharSequence content = unit.getSourceFile().getCharContent(true);
                
                if(start >= 0 && end > start) {
                    CharSequence body = content.subSequence((int) start, (int) end);
                    meth.body = Util.codeToTextArray(body);
                } else {
                    meth.body = "-- generated --";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            meth.compUnit = pth.getCompilationUnit();
            SourcePositions pos = trees.getSourcePositions();
            meth.start = pos.getStartPosition(meth.compUnit,methodTree);
            meth.end = pos.getEndPosition(meth.compUnit, methodTree);
            currrentClass.add(meth);
            return super.visitMethod(methodTree, aVoid);
        }

    }
}
