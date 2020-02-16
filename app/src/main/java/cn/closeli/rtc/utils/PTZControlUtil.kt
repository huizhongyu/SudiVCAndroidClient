package cn.closeli.rtc.utils

import cn.closeli.rtc.constract.GlobalValue
import cn.closeli.rtc.constract.GlobalValue.*
import cn.closeli.rtc.model.ControlModel
import cn.closeli.rtc.room.RoomEventAdapter

/**
 * 云台控制
 */
enum class Operation(val code: Int) {
    /**
     * 上移
     */
    UP(1),
    /**
     * 下移
     */
    DOWN(2),
    /**
     * 左移
     */
    LEFT(3),
    /**
     * 右移
     */
    RIGHT(4),
    /**
     * 放大
     */
    LARGE(5),
    /**
     * 缩小
     */
    SMALL(6)
}

object PTZControlUtil : RoomEventAdapter() {

    private const val MAX_DURATION = 1500L
    private var enable = false
    private var connectionId: String? = null

    fun start(connectionId: String) {
        if (!RoomManager.get().isHost) {
            return
        }
        this.enable = true
        this.connectionId = connectionId
    }

    fun operate(operation: Operation) {
        if (!enable || connectionId?.isEmpty() != false) return
        RoomControl.get().startPtzControl(connectionId, operation.code, MAX_DURATION)
    }

    //operate的回复信息
    override fun startPtzControl() {
        super.startPtzControl()
    }

    //主持人的控制信息
    override fun startPtzControlNotify(controlModel: ControlModel?) {
        DeviceSettingManager.getInstance().setCameraControl(KEY_ROLE_CONTROL, ROLE_INDIRECT)
        when (controlModel?.operateCode) {
            Operation.UP.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_UP)
            }
            Operation.DOWN.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_DOWN)
            }
            Operation.LEFT.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_LEFT)
            }
            Operation.RIGHT.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_RIGHT)
            }
            Operation.LARGE.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_ZOOM_CONTROL, ZOOM_WIDE)
            }
            Operation.SMALL.code -> {
                DeviceSettingManager.getInstance().setCameraControl(KEY_ZOOM_CONTROL, ZOOM_TELE)
            }
        }
    }

    fun stop() {
        if (connectionId?.isEmpty() != false) return
        connectionId?.let { RoomControl.get().stopPtzControl(it) }
    }

    fun release() {
        if (connectionId?.isEmpty() != false) {
            connectionId = null
            enable = false
        }
    }

    //stop的回复信息
    override fun stopPtzControl() {
        DeviceSettingManager.getInstance().endCameraControl()
    }

    //主持人的停止控制信息
    override fun stopPtzControlNotify(controlModel: ControlModel?) {
        DeviceSettingManager.getInstance().setCameraControl(KEY_PANTILT_CONTROL, PANTILT_STOP)
        DeviceSettingManager.getInstance().setCameraControl(KEY_ZOOM_CONTROL, ZOOM_STOP)
        L.d(javaClass.name, "stopPtzControlNotify")
    }

}






























