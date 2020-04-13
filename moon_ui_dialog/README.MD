# 操作反馈提示类

## 简介
>moon_ui_dialog对dialog和toast进行了封装

### 导入sdk
在module的build.gradle中添加依赖：

```
implementation 'com.meili.moon.ui:dialog:版本号'
```
### 一、MNDialog快速使用
快速接入使用：常用的dialog形式为中心显示和底部显示（这里根据ui提供的规范进行了默认定义，即：中心显示默认使用系统动画；点击外围不解散；背景半透明；相应用户操作进行消失操作，底部显示默认底部弹出动画；点击外围解散；背景半透明；相应用户操作进行消失操作）

1.dialog中心显示：

```
MNDialog mnDialog = new MNDialog().showCenter(context, view);
mnDialog.show();
```
2.dialog底部显示：

```
MNDialog mnDialog = new MNDialog().showActionSheet(context, view);
mnDialog.show();
```

### 二、如果需要对MNDialog进行详细配置
1.MNDialogConfig(dialog配置类，支持元素get方法)
 * context: Context   上下文
 * themeResId: Int    主题
 * gravity: Int       位置
 * contentView: View  显示view
 * animation: Int     显示消失动画
 * isCancel: Boolean  点击外围是否解散

 sdk中提供一套**themeResId**和一套**animation**分别是：***MNDialogStyle***，***MNMenuAnimation***

 2.使用MNDialogConfig进行dialog的配置显示

```
View view = LayoutInflater.from(context).inflate(R.layout.id, null);
final MNDialog mnDialog = new MNDialog(new MNDialogConfig.Builder()
        .setContext(MNDialogSampleActivity.this)//设置上下文
        .setGravity(Gravity.BOTTOM)//显示位置在底部
        .setContentView(view)
        .setCancel(true)//点击外围解散
        .setAnimation(R.style.MNMenuAnimation)//sdk中的弹出动画，如过需要其他效果可以进行自定义
        .setThemeResId(R.style.MNDialogStyle)
        .build());
view.findViewById(R.id.btn_id).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //相应的操作
        mnDialog.dismiss();
    }
});
mnDialog.show();
```

### 三、当你需要对dialog或者用户点击进行监听或者拦截
1.拦截返回键

```
mnDialog.setResponseBackPress(true)
```
2.相应拦截

```
mnDialog.setOnBackpressListener(new MNDialog.OnBackPressListener() {
                    @Override
                    public void onBackPress() {
                        // TODO: 18/6/12 相应操作
                        MNToast.show(MNDialogActivity.this, "拦截返回键");
                    }
                });
```
3.dialog消失监听

```
mnDialog.setOnDismissListener(new MNDialog.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        MNToast.show(MNDialogActivity.this, "dialog消失监听");
                    }
                });
```

### 四、根据ui提供的model定义的一些典型的dialog：MNDialogFactory（不常用），主要提供三种类型的dialog

1.中心dialog，带有标题，描述和确定取消按钮，文案点击相应支持自己定义<br>
<img src="img/mndialog_factory_1.png" width="212" height="348" />

```
MNDialog mnDialog = MNDialogFactory.INSTANCE.creator(context,
                        new DialogModel("此处为主要标题内容", "此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容",
                                "取消", "确定", ""),
                        onClickListener,onClickListener);
mnDialog.show();
```
2.中心dialog，带有标题，描述和单个按钮，文案点击支持自定义<br>
<img src="img/mndialog_factory_2.png" width="212" height="348" />

```
MNDialog mnDialog = MNDialogFactory.INSTANCE.creator(context,
                        new DialogModel("此处为主要标题内容", "此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容此处为文案内容",
                                "按钮文案", "", ""),
                        onClickListeners);//onClickListeners 为数组
mnDialog.show();
```
3.中心dialog，带有标题，描述和三个按钮，文案点击支持自定义<br>
<img src="img/mndialog_factory_3.png" width="212" height="348" />

```
MNDialog mnDialog = MNDialogFactory.INSTANCE.creator(MNDialogActivity.this,
                        new DialogModel("此处为主要标题内容", "此处为文案内容此处文案内容此处为文案内容此处为文案内容",
                                "按钮文案", "按钮文案", "按钮文案"),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mnDialog.dismiss();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mnDialog.dismiss();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mnDialog.dismiss();
                            }
                        });
mnDialog.show();
```

### 五、MNToast快速接入使用
1.中心显示：

```
MNToast.showCenter(context,"显示文案");
```
2.底部默认显示：

```
MNToast.show(context,"显示文案");
```
3.如果感觉每次都写context麻烦可以在BaseActivity或BaseFragment自定义一个方法：

```
protected void toast(CharSequence message) {
        MNToast.show(context, message);
}

protected void toastCenter(CharSequence message) {
        MNToast.showCenter(context, message);
}
```
4.以上是不带icon，如果想在上面加上icon的话可以这样：

```
MNToast.showCenter(context,resouseID,"显示文案");

MNToast.show(context,resouseID,"显示文案");
```
### 六、MNToast详细使用
1.可以对toast的view，显示时间，位置进行全部自定义

```
MNToast.show(context, "", view, Toast.LENGTH_SHORT, Gravity.CENTER);

MNToast.show(context, charSequence, view, duration, gravity, xOffset, yOffset);
```
### 七、MNNotification（一般为toolbar下提示条）快速使用

显示n秒后消失
MNNotification.show(message,duration);

### 八、MNNotification自定义
MNNotification根据ui提供的规范支持左侧icon，右侧箭头，中间mseeage显示内容以及位置自定义
* setMessageCenter() 设置文案中心显示
* setMessage(message: CharSequence?) 设置显示文案
* setMessageColor(@ColorInt color: Int) 设置文案颜色
* setMessageTextSize(dp: Float) 设置文案字体大小
* setMessageGravity(gravity: Int) 设置文案位置
* setNotificationBackgroundColor(@ColorInt color: Int) 设置MNNotification背景色
* setIconVisibility(visibility: Int) 设置左侧icon是否显示
* setIcon(@DrawableRes icon: Int) 设置左侧icon
* setArrowVisibility(visibility: Int) 设置箭头是否显示
* setArrow(@DrawableRes icon: Int) 设置箭头icon
* setOnNotificationClickListener(lis: (View) -> Unit) 设置MNNotification点击监听
* dismiss() 消失



