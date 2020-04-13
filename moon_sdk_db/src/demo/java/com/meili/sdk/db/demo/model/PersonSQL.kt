package com.meili.sdk.db.demo.model

import com.meili.moon.sdk.db.annotation.Column
import com.meili.moon.sdk.db.annotation.Table

/**
 *Created by jiang
on 2019-08-28
 */

@Table
class PersonSQL {

    @Column
    var idd = "1"

    var name = "kele"

    var age = "26"

    var address = "California"

    var salary = "10000"

}