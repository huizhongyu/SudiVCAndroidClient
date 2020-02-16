package cn.closeli.rtc.utils

import android.annotation.SuppressLint
import android.os.*
import android.text.TextUtils
import cn.closeli.rtc.App
import cn.closeli.rtc.model.http.*
import cn.closeli.rtc.net.SudiHttpClient
import cn.closeli.rtc.room.RoomClient
import cn.closeli.rtc.utils.net.NetworkUtils

object AccountUtil : NetWatchdog.NetConnectedListener {

    /**
     * 延迟重新登录时间
     */
    private var delayStep1 = 1L
    private var delayStep2 = 1L
    private var delayStep3 = 1L
    private const val STEP_1 = 0
    private const val STEP_2 = 1
    private const val STEP_3 = 2
    private val netWatchdog = NetWatchdog(App.getInstance())
    private var handler: Handler
    private val handlerThread = HandlerThread("login")


    init {
        handlerThread.start()
        handler = @SuppressLint("HandlerLeak")
        object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message?) {
                when (msg?.what) {
                    STEP_1 -> tryLogin()
                    STEP_2 -> getAddrAndAccessIn()
                    STEP_3 -> {
                        with(msg.data) {
                            val account = getString("account", "")
                            val pwd = getString("password", "")
                            getToken(account, pwd)
                        }
                    }
                }
            }
        }
        if (!NetworkUtils.isNetworkAvailable()) {
            netWatchdog.setNetConnectedListener(this)
            netWatchdog.startWatch()
        }
    }

    fun login() = handler.sendEmptyMessage(STEP_1)

    /**
     * 尝试自动登录
     */
    fun tryLogin() {
        handler.removeCallbacksAndMessages(null)
        val userId = SPEditor.instance().userId
        val token = SPEditor.instance().token
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
            val serialNum = SystemUtil.getSerial()
            SudiHttpClient.get().getAccount(serialNum, object : SudiHttpCallback<AccountModel> {
                override fun onSuccess(response: AccountModel) {
                    SPEditor.instance().account = response.account
                    SPEditor.instance().pwd = response.password
                    getToken(response.account, response.password)
                }

                override fun onFailed(e: Throwable) {
                    delayStep1 = delayStep1.shl(1)
                    handler.sendEmptyMessageDelayed(STEP_1, delayStep1)
                    L.e("delayStep1 = $delayStep1")
                }
            })
        } else {
            getAddrAndAccessIn()
        }
    }

    /**
     * 请求获取token
     * @param account 账户 userid 设备id
     * @param password 密码
     */
    private fun getToken(account: String, password: String) {
        handler.removeCallbacksAndMessages(null)
        SudiHttpClient.get().getToken(account, password, object : SudiHttpCallback<TokenResp> {
            override fun onSuccess(response: TokenResp) {
                SPEditor.instance().saveTokenResp(response)
                getAddrAndAccessIn()
            }

            override fun onFailed(e: Throwable) {
                val message = handler.obtainMessage()
                message.what = STEP_3
                val b = Bundle()
                b.putString("account", account)
                b.putString("password", password)
                message.data = b
                delayStep3 = delayStep3.shl(1)
                handler.sendMessageDelayed(message, delayStep3)
            }
        })
    }

    /**
     * 获取连接地址并登录
     */
    private fun getAddrAndAccessIn() {
        handler.removeCallbacksAndMessages(null)
        SudiHttpClient.get().getAddr(object : SudiHttpCallback<AddrResp> {
            override fun onSuccess(response: AddrResp) {
                SPEditor.instance().saveAddrs(response)
                RoomClient.get().prepare(App.getInstance(), response.signalAddrList[0])
            }

            override fun onFailed(e: Throwable) {
                //token失效重新获取
//                if (e is SudiException && e.code == 12003) {
//                    SPEditor.instance().token = ""
//                    handler.sendEmptyMessageDelayed(STEP_1, delayStep1)
//                    return
//                }
//                App.post { UIUtils.toastMessage(e.message) }
                delayStep1 = delayStep1.shl(1)
                handler.sendEmptyMessageDelayed(STEP_1, delayStep1)
            }
        })
    }

    override fun onReNetConnected(isReconnect: Boolean) {
        if (!RoomClient.get().isAccessInSuccess) {
            tryLogin()
        }
    }

    override fun onNetUnConnected() {

    }

}