package cn.closeli.rtc.service;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 辅助功能
 * @author Administrator
 */
public class AutoInstallService extends AccessibilityService {
    // 页面切换时间
    private static final int DELAY_PAGE = 320;
    private final Handler mHandler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !event.getPackageName().toString().contains("packageinstaller")) {
            return;
        }
        AccessibilityNodeInfo eventNode = event.getSource();
        if (eventNode == null) {
            // 打开最近页面
            performGlobalAction(GLOBAL_ACTION_RECENTS);
            mHandler.postDelayed(() -> {
                performGlobalAction(GLOBAL_ACTION_BACK); // 返回安装页面
            }, DELAY_PAGE);
            return;
        }
        //当前窗口根节点
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }
        //一起执行：安装->下一步->打开,以防意外漏掉节点
        findTxtClick(rootNode, "安装");
        findTxtClick(rootNode, "继续安装");
        findTxtClick(rootNode, "下一步");
        findTxtClick(rootNode, "打开");
        // 回收节点实例来重用
        eventNode.recycle();
        rootNode.recycle();
    }

    /**
     * 查找安装,并模拟点击(findAccessibilityNodeInfosByText判断逻辑是contains而非equals)
     *
     * @param nodeInfo
     * @param txt
     */
    private void findTxtClick(AccessibilityNodeInfo nodeInfo, String txt) {
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(txt);
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isEnabled() && node.isClickable() && (node.getClassName().equals("android.widget.Button")
                    || node.getClassName().equals("android.widget.CheckBox") // 兼容华为安装界面的复选框
            )) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        // 服务开启，模拟两次返回键，退出系统设置界面（实际上还应该检查当前UI是否为系统设置界面，但一想到有些厂商可能篡改设置界面，懒得适配了...）
        performGlobalAction(GLOBAL_ACTION_BACK);
        mHandler.postDelayed(() -> performGlobalAction(GLOBAL_ACTION_BACK), DELAY_PAGE);
    }

    @Override
    public void onInterrupt() {

    }
}
