package cn.closeli.rtc.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.children

class MenuManager {
    //动画执行时长
    private val ANIM_DURATION = 300
    //没有操作3000后执行动画
    private val WAIT_TIME = 3000L
    //持有相关的window
    private val handler = Handler()
    private var view: View? = null
    private var rightMenu: View? = null
    private var bottomMenu: View? = null
    private var animFinish = true
    private var currentState = true //当前的可见状态 true false 不可见
    private var onHoverListener: View.OnHoverListener? = null
    private val hideListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            animFinish = true
            currentState = false
            if (onHoverListener == null) {
                onHoverListener = View.OnHoverListener { _, _ ->
                    showMenu()
                    false
                }
                view?.setOnHoverListener(onHoverListener)
            }
        }
    }
    private val onKeyListener = View.OnKeyListener { v, keyCode, event ->
        reset()
        if (event?.action == MotionEvent.ACTION_UP && currentState) {
            scheduleHide()
        }
        false
    }
    private val showListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            currentState = true
            animFinish = true
            scheduleHide()
        }
    }

    fun showMenu() {
        if (animFinish && !currentState && isC9Z()) {
            reset()
            animMenu(true)
        }
    }

    fun registerWindows(popupWindow: PopupWindow) {
        popupWindow.setOnDismissListener {
            scheduleHide()
        }
    }

    fun registerMain(view: View) {
        this.view = view
        this.view?.setOnKeyListener(onKeyListener)
    }

    fun registerRightMenu(view: View) {
        this.rightMenu = view
        (this.rightMenu as ViewGroup).children.forEach {
            it.setOnKeyListener(onKeyListener)
        }
    }

    fun registerBottomMenu(view: View) {
        this.bottomMenu = view
        (this.bottomMenu as ViewGroup).children.forEach {
            it.setOnKeyListener(onKeyListener)
        }
    }

    fun startSchedule() {
        if (SystemUtil.getSystemModel() == "C9Z") {
            scheduleHide()
        }
    }

    /**
     * 相关弹框弹出的时候调用，取消掉计时任务
     */
    fun reset() {
        handler.removeCallbacksAndMessages(null)
    }

    //延时隐藏
    fun scheduleHide() {
        if (currentState && animFinish && isC9Z()) {
            view?.let {
                handler.postDelayed({
                    reset()
                    animMenu(false)
                }, WAIT_TIME)
            }
        }
    }

    private fun animMenu(show: Boolean) {
        rightMenu?.let {
            rightMenu?.animate()?.translationX(it.translationX + (if (show) -1 else 1) * it.width)?.setDuration(ANIM_DURATION.toLong())?.setListener(if (show) showListener else hideListener)?.start()
        }
        bottomMenu?.let {
            bottomMenu?.animate()?.translationY(it.translationY + (if (show) -1 else 1) * it.height)?.setDuration(ANIM_DURATION.toLong())?.start()
        }
        animFinish = false
    }

    private fun isC9Z() = SystemUtil.getSystemModel() == "C9Z"
}