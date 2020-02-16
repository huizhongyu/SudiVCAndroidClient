package cn.closeli.rtc.widget.popwindow

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v7.widget.GridLayoutManager
import cn.closeli.rtc.R
import cn.closeli.rtc.widget.BasePopupWindow
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_video_setting.view.*
import kotlinx.android.synthetic.main.item_video_setting_layout.view.*

/**
 * 会议布局设置
 */
class VideoLayoutFragment(val context:Context) : BasePopupWindow(context) {
    private lateinit var adapter: LayoutAdapter
    override fun thisLayout(): Int = R.layout.fragment_video_setting

    override fun doInitView() {
        view.restore_default.setOnClickListener {

        }
        view.cancel.setOnClickListener {

        }
        adapter = LayoutAdapter(listOf(LayoutBean(R.drawable.maininterface_sidebaer_icon_layout_four, "1+1"),LayoutBean(R.drawable.maininterface_sidebaer_icon_layout_six, "1+2")))
        view.layouts.layoutManager = GridLayoutManager(mContext, 2)
        view.layouts.adapter = adapter
    }

    override fun doInitData() {

    }
}

class LayoutAdapter(imgId: List<LayoutBean>) : BaseQuickAdapter<LayoutBean, BaseViewHolder>(R.layout.item_video_setting_layout, imgId) {
    override fun convert(helper: BaseViewHolder, item: LayoutBean?) {
        item?.let {
            helper.itemView.layoutImg.setImageResource(it.layoutId)
            helper.itemView.layoutDes.text = it.des
        }
    }
}

data class LayoutBean(@DrawableRes val layoutId: Int, val des: String)
