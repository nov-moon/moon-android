package com.meili.sdk.db.demo

import android.app.Activity
import android.os.Bundle
import com.meili.moon.sdk.db.DBImpl
import com.meili.moon.sdk.db.IDB
import com.meili.moon.sdk.log.Logcat
import com.meili.sdk.db.demo.model.PersonModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = IDB.Config()
//        /**数据库名称*/
//        config.dbName = "mljr.db"
//        /**数据库的位置*/
//        config.dbDir = File("")
//        /**数据库是否允许使用事务*/
//        config.allowTransaction = true
        /**数据库升级监听*/
        config.dbUpgradeListener = object : IDB.DbUpgradeListener {
            override fun onUpgrade(db: IDB, oldVersion: Int, newVersion: Int) {
                Logcat.e("数据库升级", "数据库升级")
            }
        }
        /**数据库创建监听*/
        config.tableCreateListener = object : IDB.TableCreateListener {
            override fun onTableCreated(db: IDB, table: String) {
                Logcat.e("数据库创建", "数据库创建")

            }
        }
        /**数据库打开监听*/
        config.dbOpenListener = object : IDB.DbOpenListener {
            override fun onDbOpened(db: IDB) {
                Logcat.e("数据库打开", "数据库打开")

            }
        }
        val mDBImpl = DBImpl.getInstance(this, config)


        mTxtNewTable.setOnClickListener {


            val user = PersonModel()
            user.idd = "2"
            user.name = "Paul"
            user.age = "32"
            user.address = "California"
            user.salary = "20000.0"
            mDBImpl.table.save(user)
            val user2 = PersonModel()
            user2.idd = "3"
            user2.name = "Allen"
            user2.age = "25"
            user2.address = "Texas"
            user2.salary = "15000.0"
            mDBImpl.table.save(user2)
            val user3 = PersonModel()
            user3.idd = "4"
            user3.name = "Teddy"
            user3.age = "32"
            user3.address = "Norway"
            user3.salary = "20000.0"
            mDBImpl.table.save(user3)
            mTxtNewTableMessage.text = "添加成功"
        }
        mTxtGetData.setOnClickListener {
            val pp = mDBImpl.table.get("3", PersonModel::class)
            mTxtGetDataMessage.text = pp?.name


        }
        mTxtWhereData.setOnClickListener {
            val cadb = mDBImpl.table.get(PersonModel::class) {
                and("age", "=", "32")
            }

            for (cad: PersonModel in cadb) {
                Logcat.e("first" + cad.name)
                mTxtWhereDataMessage.text = cad.name
            }
        }

        var startTime = 0L
        var endTime = 0L
        mTxtOtherData.setOnClickListener {
            startTime = System.currentTimeMillis()

            val selector = mDBImpl.table.selector(PersonModel::class) {
                limit = 1
                offset = 0
                where {
                    and("age", "=", "32")
                }

            }
            val aaa = selector.findAll()
            for (cad: PersonModel in aaa) {
                Logcat.e("first" + cad.name)
                mTxtOtherDataMessage.text = cad.name
            }
//            val column = listOf("name")
//            val List = mDBImpl.table.selector(PersonModel::class) {
//                select(listOf("name"))
//            }
//                    .groupBy("Name") { build() }
//                    .orderBy(ISelector.OrderBy("SALARY", false))
//                    .findFirst()
//            LogUtil.d(List)
//            val List2 = mDBImpl.table.selector(PersonModel::class) {
//                select(listOf("name"))
//            }
//                    .findFirst()
//
//            selector.findAll()
//            val List1 = mDBImpl.table.selector(PersonModel::class)
//                    .groupBy("Name") {
//                        having {
//                            and("age", "=", "32")
//                        }
//                    }
//                    .findAll()
//

//            val all = cad.findFirst()
//            Logcat.e("all" + all?.age)
//            val first = cad.findFirst()
//            Logcat.e("first" + first?.name)
            endTime = System.currentTimeMillis()
//            Logcat.e("endTime" + endTime)
//            Logcat.e(endTime - startTime);

        }
        mTxtDeleteTable.setOnClickListener {
            //            mDBImpl.dropTable(PersonModel::class)
            val modefy = mDBImpl.table.delete(PersonModel::class) {

                and("age", "=", "32")
            }
//            Logcat.e("lllll$modefy")
        }

    }

}

