package process;

import javassist.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by liushanchen on 16/5/23.
 * This program
 * 1.compile the target .java file
 * 2.overwrites current target file by adding log statements
 * to print the known field variables and local variables in runtime.
 * Note: the javassist's compiler is not support inner class
 * -get method by method name and parameters
 * -if the local variable is not initialized just log a string that the variable is not initialized
 */
public class ByteCodePMethod extends ByteCodeP {
    private CtClass cc = null;
    static int count = 0;

    public ByteCodePMethod() {
        System.out.println("count:" + count);
        count++;
    }

    public byte[] transformClass(byte[] b) {
        try {
            cc = poolParent.makeClass(new java.io.ByteArrayInputStream(b));
            importLogPack(cc);
            findMethods(cc);
            findNestedClass(cc);
            b = cc.toBytecode();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cc != null) {
                cc.detach();
            }
        }
        return b;
    }

    /**
     * find the method in the target
     *
     * @return
     */
    private void findMethods(CtClass ctClass) {
        for (CtMethod cb : Arrays.asList(ctClass.getDeclaredMethods())) {
            try {
                if (!cb.isEmpty()) {
                    logMethod(cb);
                }
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
        for (CtConstructor cb : Arrays.asList(ctClass.getDeclaredConstructors())) {
            try {
                if (!cb.isEmpty()) {
                    logMethod(cb);
                }
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        }
    }

    private void findNestedClass(CtClass ctClass) {
        try {
            CtClass[] nccs = ctClass.getNestedClasses();
            for (int i = 0; i < nccs.length; i++) {
                declareLogger(nccs[i]);
                findMethods(nccs[i]);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getMethodQualifyName(CtBehavior method){
        StringBuilder sb=new StringBuilder(method.getDeclaringClass().getName());
        sb.append("#");
        sb.append(method.getName());
        sb.append("(");
        try {
            CtClass[] methodParameterTypes=method.getParameterTypes();
            for(int i=0;i<methodParameterTypes.length;i++){
                sb.append(methodParameterTypes[i].getName());
                sb.append(",");
            }
            if(sb.toString().endsWith(",")){
                sb.deleteCharAt(sb.length()-1);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        sb.append(")");
        return sb.toString();
    }
    /**
     * insert log into the method body.
     *
     * @param method
     * @throws CannotCompileException
     */
    private void logMethod(CtBehavior method) throws CannotCompileException {
        method.insertBefore(lo.getMethodName(getMethodQualifyName(method)));
//        method.insertAfter(lo.getMethodEnd(method.getLongName()));
    }

//    private void logMethod(CtMethod method) throws CannotCompileException {
//        method.insertBefore(lo.getMethodName(method.getLongName()));
//        method.insertAfter(lo.getMethodEnd(method.getLongName()));
//    }

}

