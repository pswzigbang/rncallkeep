package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zigbang.smarthome.callkeep.poc.R


interface ZigbangAlertDialogListener {
    fun onClickPositiveBtn()
    fun onClickNegativeBtn()
}

class ZigbangAlertDialog : DialogFragment() {
    var title: String? = null
    var message: String? = null
    var positiveBtnText: String? = null
    var negativeBtnText: String? = null
    var listener: ZigbangAlertDialogListener? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.zigbang_alert_dialog, container, false)
        return view.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            if (title != null) {
                findViewById<TextView>(R.id.text_title)?.apply {
                    text = title
                    visibility = View.VISIBLE
                }
            }

            if (negativeBtnText != null) {
                findViewById<Button>(R.id.btn_negative)?.apply {
                    text = negativeBtnText
                    setOnClickListener {
                        dismiss()
                        listener?.onClickNegativeBtn()
                    }
                    visibility = View.VISIBLE
                }
            }

            if (message != null) {
                findViewById<TextView>(R.id.text_message)?.apply {
                    text = message
                    visibility = View.VISIBLE
                    if (title != null) {
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                    }
                }
            }

            if (positiveBtnText != null) {
                findViewById<Button>(R.id.btn_positive)?.apply {
                    text = positiveBtnText
                    setOnClickListener {
                        dismiss()
                        listener?.onClickPositiveBtn()
                    }
                    visibility = View.VISIBLE
                }
            }
        }
    }

    class ZigbangAlertDialogBuilder {
        private val dialog = ZigbangAlertDialog()

        fun setTitle(title: String): ZigbangAlertDialogBuilder {
            dialog.title = title
            return this
        }

        fun setMessage(message: String): ZigbangAlertDialogBuilder {
            dialog.message = message
            return this
        }

        fun setPositiveBtnText(text: String): ZigbangAlertDialogBuilder {
            dialog.positiveBtnText = text
            return this
        }

        fun setNegativeBtnText(text: String): ZigbangAlertDialogBuilder {
            dialog.negativeBtnText = text
            return this
        }

        fun setBtnClickListener(listener: ZigbangAlertDialogListener): ZigbangAlertDialogBuilder {
            dialog.listener = listener
            return this
        }

        fun create(): ZigbangAlertDialog {
            return dialog
        }
    }
}
