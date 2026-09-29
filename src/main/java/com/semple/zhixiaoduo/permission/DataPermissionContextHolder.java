package com.semple.zhixiaoduo.permission;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 当前线程的数据权限上下文。
 * <p>使用栈支持权限方法嵌套调用，退出内层方法后能够恢复外层权限。</p>
 */
public final class DataPermissionContextHolder {

    private static final ThreadLocal<Deque<DataPermissionDecision>> CONTEXT =
            ThreadLocal.withInitial(ArrayDeque::new);

    private DataPermissionContextHolder() {
    }

    /**
     * 压入当前数据权限决定。
     *
     * @param decision decision 参数。
     */
    public static void push(DataPermissionDecision decision) {
        if (decision != null) {
            CONTEXT.get().push(decision);
        }
    }

    /**
     * 获取当前数据权限决定。
     *
     * @return 处理结果。
     */
    public static DataPermissionDecision get() {
        Deque<DataPermissionDecision> stack = CONTEXT.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * 弹出当前决定，并在线程不再使用时释放 ThreadLocal。
     */
    public static void pop() {
        Deque<DataPermissionDecision> stack = CONTEXT.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            CONTEXT.remove();
        }
    }

    /**
     * 强制清理线程中的全部数据权限。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
