package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zigbang.smarthome.callkeep.poc.R


class ZigbangImageDialog : DialogFragment() {
    var centerImageUrl: String? = null
    var centerImageWidth: Int = 0
    var centerImageHeight: Int = 0
    var positiveBtnText: String? = null
    var negativeBtnText: String? = null
    var listener: ZigbangAlertDialogListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_zigbang_image_dialog, container, false)
        return view.rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var width = LinearLayout.LayoutParams.MATCH_PARENT
        var height = LinearLayout.LayoutParams.WRAP_CONTENT

        view?.apply {
            if(centerImageUrl != null && centerImageUrl != ""){
                val imageResource = resources.getIdentifier(centerImageUrl, null, this.context.packageName)
                val res = resources.getDrawable(imageResource)

                findViewById<ImageView>(R.id.center_image)?.setImageDrawable(res)
            }
            if(negativeBtnText != null){
                findViewById<Button>(R.id.btn_negative)?.text = negativeBtnText
                findViewById<Button>(R.id.btn_negative)?.setOnClickListener {
                    dismiss()
                    listener?.onClickNegativeBtn()
                }
                findViewById<Button>(R.id.btn_negative)?.visibility = View.VISIBLE
            }
            if(positiveBtnText != null) {
                findViewById<Button>(R.id.btn_positive)?.text = positiveBtnText
                findViewById<Button>(R.id.btn_positive)?.setOnClickListener {
                    dismiss()
                    listener?.onClickPositiveBtn()
                }
                findViewById<Button>(R.id.btn_positive)?.visibility = View.VISIBLE
            }
            if (centerImageWidth > 0) {
                width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, centerImageWidth.toFloat(), resources.displayMetrics).toInt()
            }
            if (centerImageHeight > 0) {
                height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, centerImageHeight.toFloat(), resources.displayMetrics).toInt()
            }

            val imageLayoutParams = LinearLayout.LayoutParams(width, height)
            findViewById<ImageView>(R.id.center_image)?.layoutParams = imageLayoutParams
        }
    }

    class ZigbangImageDialogBuilder {
        private val dialog = ZigbangImageDialog()

        fun setCenterImageUrl(imageUrl: String): ZigbangImageDialogBuilder {
            dialog.centerImageUrl = imageUrl
            return this
        }
        fun setCenterImageWidth(width: Int): ZigbangImageDialogBuilder {
            dialog.centerImageWidth = width
            return this
        }
        fun setCenterImageHeight(height: Int): ZigbangImageDialogBuilder {
            dialog.centerImageHeight = height
            return this
        }
        fun setPositiveBtnText(text: String): ZigbangImageDialogBuilder {
            dialog.positiveBtnText = text
            return this
        }
        fun setNegativeBtnText(text: String): ZigbangImageDialogBuilder {
            dialog.negativeBtnText = text
            return this
        }
        fun setBtnClickListener(listener: ZigbangAlertDialogListener): ZigbangImageDialogBuilder {
            dialog.listener = listener
            return this
        }
        fun create(): ZigbangImageDialog {
            return dialog
        }
    }
}