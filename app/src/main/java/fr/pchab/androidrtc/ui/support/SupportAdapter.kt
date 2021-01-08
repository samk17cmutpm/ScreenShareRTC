package fr.pchab.androidrtc.ui.support

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import fr.pchab.androidrtc.R

class SupportAdapter(
	data: ArrayList<SupportItem>,
	private val ctx: Context?
) : BaseQuickAdapter<SupportItem, BaseViewHolder>(R.layout.support_item, data){
	override fun convert(holder: BaseViewHolder, item: SupportItem) {
		val imIcon: AppCompatImageView = holder.getView(R.id.imIcon)
		ctx?.let { context ->
			item.ic?.let { ic ->
				imIcon.setImageDrawable(ContextCompat.getDrawable(context, ic))
			}
		}
		holder.setText(R.id.tvText, "${item.text}")
	}
}