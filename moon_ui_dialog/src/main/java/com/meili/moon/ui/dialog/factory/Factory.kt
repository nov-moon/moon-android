package com.meili.moon.ui.dialog.factory

import android.content.Context
import android.view.View
import com.meili.moon.ui.dialog.config.MNDialogConfig
import com.meili.moon.ui.dialog.model.DialogModel
import com.meili.moon.ui.dialog.widget.MNDialog

/**
 * Author： fanyafeng
 * Date： 18/3/13 下午2:52
 * Email: fanyafeng@live.cn
 */

interface Factory {
    fun creator(context: Context, dialogModel: DialogModel): MNDialog

    fun creator(context: Context, dialogModel: DialogModel, onConfirmClick: View.OnClickListener?): MNDialog

     fun creator(context: Context, dialogModel: DialogModel, onConfirmClick: View.OnClickListener? = null, onCancelClick: View.OnClickListener?): MNDialog

     fun creator(mnDialogConfig: MNDialogConfig, dialogModel: DialogModel, onConfirmClick: View.OnClickListener?, onCancelClick: View.OnClickListener?): MNDialog

     fun creator(context: Context, dialogModel: DialogModel, vararg onClickListeners: View.OnClickListener?): MNDialog
}