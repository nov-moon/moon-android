package com.meili.sdk.db.demo.model

import com.meili.moon.sdk.db.annotation.Column
import com.meili.moon.sdk.db.annotation.Table

/**
 *Created by jiang
on 2019-08-28
 */

@Table
class PersonModel {

    @Column(isId = true)
    var idd: String = ""

    var name: String = ""

    var age: String = ""

    var address: String = ""

    var salary: String = ""

}