/**
 * 本包定义了日志打印标准接口
 * <p>
 * 通过调用 {@link com.meili.moon.sdk.log.LogUtil} 打印日志，LogUtil会自动根据是否为debug模式选择日志是否打印
 * <p>
 * 这里定义了 {@link com.meili.moon.sdk.log.ILogger} 接口，定义了标准打印方法,可以实现自己的打印，并通过LogUtil.addLogger()方法添加
 * <p>
 * 在ILogger中定义了onlyTag方法，如果在打印过程中，有tag被指定logger匹配，则其他logger就不会打印此log，
 * 比如在组件中，自定义一个logger，指定他的onlyTag方法，并且添加到LogUtil中。之后此组件使用固定tag进行打印，那么框架中的其他logger是不会打印这些日志的
 */
package com.meili.moon.sdk.log;