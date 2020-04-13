package com.meili.moon.sdk.app.base.adapter.holders;

/**
 * Created by imuto on 15/12/1.
 */
public interface IViewHolderGroupFooter<Group> {

    public void onBindViewHolderGroupFooter(Group group);

    public void onClickGroupFooter(int position, Group group);

    public boolean onLongClickGroupFooter(int position, Group group);

}
