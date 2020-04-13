/**
 * 本包提供了数据库接口定义
 * <p>
 * 数据库的引入：
 * 在使用方添加相关实现库，并调用{@link com.meili.moon.sdk.ComponentsInstaller#installDb(com.meili.moon.sdk.db.DBManager)}方法进行初始化。
 * <p>
 * 用户可以通过[{@link com.meili.moon.sdk.CommonSdk#db(com.meili.moon.sdk.db.IDB.Config)}]方法进行调用
 * <p>
 * 在要保存到数据库的model上，使用{@link com.meili.moon.sdk.db.annotation.Table}注解进行标注和定义
 * 在model的字段上使用{@link com.meili.moon.sdk.db.annotation.Column}注解进行标注定义
 * <p>
 * {@link com.meili.moon.sdk.db.ITable}类负责表数据的CURD操作
 * {@link com.meili.moon.sdk.db.IDB}类负责数据库的CURD操作
 */
package com.meili.moon.sdk.db;