package com.gos.monitor.client.transformer;


import com.gos.monitor.common.MonitorSettings;
import com.gos.monitor.common.io.SIO;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.ListIterator;

/**
 * 给指定类的每个方法织入代码,用来采集每个方法的执行时间.
 * ---代码织入 用try-catch 来　模拟  try-finally
 */
class TryFinallyTransformService {
    protected String className;
    static final String Exe = "com/gos/monitor/client/execute/InvokeStackService";

    TryFinallyTransformService(String className) {
        this.className = className.replaceAll("/", ".");
    }

    public void transform(ClassNode cn) {
        if (cn == null) {
            return;
        }

        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            String methodName = getMethodFullName(className, mn.name, mn.desc);
            SIO.info("正在检查方法:" + methodName);
            if ("<init>".equals(mn.name)) {
                SIO.info("跳过构造方法:" + methodName);
                continue;
            }
            if ("<clinit>".equals(mn.name)) {
                SIO.info("跳过静态代码块:" + methodName);
                continue;
            }
            if ("main".equals(mn.name) && mn.access == (mn.access | Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC)) {
                SIO.info("跳过主方法:" + methodName);
                continue;
            }
            if ("run".equals(mn.name) && (mn.access == (Opcodes.ACC_PUBLIC & mn.access))) {
                SIO.info("跳过run方法:" + methodName);
                continue;
            }
            if (Opcodes.ACC_ABSTRACT == (Opcodes.ACC_ABSTRACT & mn.access)) {
                SIO.info("跳过抽象方法:" + methodName);
                continue;
            }
            if (Opcodes.ACC_NATIVE == (Opcodes.ACC_NATIVE & mn.access)) {
                SIO.info("跳过本地方法:" + methodName);
                continue;
            }

            if (MonitorSettings.Client.ExcludePackages != null && MonitorSettings.Client.ExcludePackages.matcher(methodName).find()) {
                SIO.info("因匹配排除表达式故跳过:" + cn);
                continue;
            } else if (MonitorSettings.Client.IncludePackages != null && !MonitorSettings.Client.IncludePackages.matcher(methodName).find()) {
                SIO.info("因不匹配采集表达式故跳过:" + cn);
                continue;
            }

            //step 1.拿到方法的指令集
            InsnList sourceCommands = mn.instructions;
            if (sourceCommands == null) {
                continue;
            } else if (sourceCommands.size() == 0) {
                continue;
            }

            ListIterator<AbstractInsnNode> iterator = sourceCommands.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode instrument = iterator.next();
                int op = instrument.getOpcode();
                if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) {
                    AbstractInsnNode prev = instrument.getPrevious();
                    sourceCommands.insert(prev, getFinishNoneException(methodName));
                }
            }

            LabelNode tryLabelNode = new LabelNode();
            LabelNode catchLabelNode = new LabelNode();
            LabelNode catchEndLabel = new LabelNode();
            LabelNode endLabelNode = new LabelNode();
            String type = null;
            TryCatchBlockNode tryCatchBlock = new TryCatchBlockNode(tryLabelNode, catchLabelNode, catchLabelNode, type);

            //用try-catch 来模拟 try-finally

            InsnList weaveCommands = new InsnList();

            weaveCommands.add(tryLabelNode);
            //try{
            weaveCommands.add(getStartInsns(methodName));
            weaveCommands.add(sourceCommands);
            //}catch(Exception){
            weaveCommands.add(catchLabelNode);

            weaveCommands.add(getFinishHasException(methodName));
            weaveCommands.add(new InsnNode(Opcodes.ATHROW));

            //}
            weaveCommands.add(catchEndLabel);

            weaveCommands.add(endLabelNode);

            mn.instructions = weaveCommands;
            mn.tryCatchBlocks.add(tryCatchBlock);

        }
    }

    protected String getMethodFullName(String className, String methodShortName, String methodDesc) {
        StringBuilder method = new StringBuilder(128);
        method.append(className).append(".").append(methodShortName).append("(");
        Type[] types = Type.getArgumentTypes(methodDesc);
        if (types != null) {
            int i = 0;
            for (; i < types.length - 1; i++) {
                //参数类型名
                Type type = types[i];
                String name = getParameterType(type);
                method.append(name).append(",");
            }
            if (i < types.length) {
                //参数类型名
                Type type = types[i];
                String name = getParameterType(type);
                method.append(name);
            }
        }
        method.append(")");
        return method.toString();
    }

    protected String getParameterType(Type type) {
        String name = type.getClassName();
        int ix = name.lastIndexOf('.') + 1;//remove package
        return name.substring(ix);
    }

    protected InsnList getStartInsns(String methodName) {
        //方法开始处插入的指定列表
        InsnList startCommands = new InsnList();
        startCommands.add(new LdcInsnNode(methodName));
        startCommands.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Exe, "start", "(Ljava/lang/String;)V"));

        return startCommands;
    }

    protected InsnList getFinishNoneException(String methodName) {
        //方法结束或方法抛出异常时插入的指定列表
        InsnList finishCommands = new InsnList();
        finishCommands.add(new LdcInsnNode(methodName));
        finishCommands.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Exe, "finishNoneException", "(Ljava/lang/String;)V"));
        return finishCommands;
    }

    protected InsnList getFinishHasException(String methodName) {
        //方法结束或方法抛出异常时插入的指定列表
        InsnList finishCommands = new InsnList();
        finishCommands.add(new LdcInsnNode(methodName));
        finishCommands.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Exe, "finishHasException", "(Ljava/lang/String;)V"));
        return finishCommands;
    }
}
//---用try-catch 来　模拟  try-finally