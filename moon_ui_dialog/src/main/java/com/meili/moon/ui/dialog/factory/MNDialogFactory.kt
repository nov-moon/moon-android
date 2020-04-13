package com.meili.moon.ui.dialog.factory

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.meili.moon.ui.dialog.R
import com.meili.moon.ui.dialog.config.MNDialogConfig
import com.meili.moon.ui.dialog.model.DialogModel
import com.meili.moon.ui.dialog.widget.MNDialog
import org.jetbrains.annotations.NotNull

/**
 * Author： fanyafeng
 * Date： 18/3/13 下午3:21
 * Email: fanyafeng@live.cn
 */
object MNDialogFactory : Factory {

    override fun creator(context: Context, dialogModel: DialogModel): MNDialog {
        return this.creator(context, dialogModel, null)
    }

    override fun creator(context: Context, dialogModel: DialogModel, onConfirmClick: View.OnClickListener?): MNDialog {
        return this.creator(context, dialogModel, onConfirmClick, null)
    }

    override fun creator(context: Context, dialogModel: DialogModel, onConfirmClick: View.OnClickListener?, onCancelClick: View.OnClickListener?): MNDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.mn_dialog_layout_center_dialog, null)
        return this.creator(MNDialogConfig.Builder()
                .setContext(context)
                .setGravity(Gravity.CENTER)
                .setContentView(view)
                .setCancel(false)
                .setThemeResId(R.style.MNDialogStyle)
                .build(), dialogModel, onConfirmClick, onCancelClick)
    }

    override fun creator(mnDialogConfig: MNDialogConfig, dialogModel: DialogModel, onConfirmClick: View.OnClickListener?, onCancelClick: View.OnClickListener?): MNDialog {
        val mnDialog = MNDialog(mnDialogConfig)
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.text_dialog_title).text = dialogModel.title
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.text_dialog_desc).text = dialogModel.desc
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancle).text = dialogModel.cancel
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_confirm).text = dialogModel.confirm
        if (onCancelClick != null) {
            mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancle).setOnClickListener(onCancelClick)
        } else {
            mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancle).setOnClickListener(View.OnClickListener { mnDialog.dismiss() })
        }
        if (onConfirmClick != null) {
            mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_confirm).setOnClickListener(onConfirmClick)
        } else {
            mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_confirm).setOnClickListener(View.OnClickListener { mnDialog.dismiss() })
        }
        return mnDialog
    }

    override fun creator(context: Context, dialogModel: DialogModel, @NotNull vararg onClickListeners: View.OnClickListener?): MNDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.mn_dialog_layout_vertical_dialog, null)
        val mnDialogConfig = MNDialogConfig.Builder()
                .setContext(context)
                .setGravity(Gravity.CENTER)
                .setContentView(view)
                .setCancel(false)
                .setThemeResId(R.style.MNDialogStyle)
                .build()
        val mnDialog = MNDialog(mnDialogConfig)
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.text_dialog_title).text = dialogModel.title
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.text_dialog_desc).text = dialogModel.desc
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action1).text = dialogModel.tag as String
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action2).text = dialogModel.confirm
        mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancel).text = dialogModel.cancel
        if (onClickListeners.isNotEmpty()) {
            if (onClickListeners.size > 1 && null != onClickListeners[1]) {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action1).setOnClickListener(onClickListeners[1])
            } else {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.action1_line).visibility = View.GONE
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action1).visibility = View.GONE
            }
            if (onClickListeners.size > 2 && null != onClickListeners[2]) {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action2).setOnClickListener(onClickListeners[2])
            } else {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.action2_line).visibility = View.GONE
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_action2).visibility = View.GONE
            }
            if (null != onClickListeners[0]) {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancel).setOnClickListener(onClickListeners[0])
            } else {
                mnDialogConfig.contentView!!.findViewById<TextView>(R.id.btn_dialog_cancel).setOnClickListener(View.OnClickListener { mnDialog.dismiss() })
            }
        }
        return mnDialog
    }
}