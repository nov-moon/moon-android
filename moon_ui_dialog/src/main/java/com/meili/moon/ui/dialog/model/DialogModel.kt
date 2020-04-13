package com.meili.moon.ui.dialog.model

/**
 * Author： fanyafeng
 * Date： 18/3/13 下午3:08
 * Email: fanyafeng@live.cn
 */
class DialogModel : BaseModel {

    //标题
    var title: String? = ""
    //描述
    var desc: String? = ""
    //取消
    var cancel: String? = ""
    //确认
    var confirm: String? = ""

    var tag: Any? = ""


    constructor() : super()

    constructor(title: String?, desc: String?, cancel: String?, confirm: String?) {
        this.title = title
        this.desc = desc
        this.cancel = cancel
        this.confirm = confirm
    }

    constructor(title: String?, desc: String?, cancel: String?, confirm: String?, tag: Any?) : super() {
        this.title = title
        this.desc = desc
        this.cancel = cancel
        this.confirm = confirm
        this.tag = tag
    }


}