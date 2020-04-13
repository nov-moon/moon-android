package com.meili.moon.sdk.app.base.adapter.dataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imuto on 15/12/1.
 */
public final class DataSetGroup<Group, Child> implements IDataSetGroup<Group, Child> {
    private List<ItemData> mDataSet = new ArrayList<>();
    private List<Group> mGroupDataSet = new ArrayList<>();
    private List<List<Child>> mChildDataSet = new ArrayList<>();
    private boolean mGroupFooter;

    class ItemData {
        final int groupIndex;
        final int groupPosition;
        final boolean isGroup;
        final int childIndex;
        final boolean groupFooter;

        ItemData(int groupIndex, int groupPosition) {
            this.groupIndex = groupIndex;
            this.groupPosition = groupPosition;
            this.isGroup = true;
            this.childIndex = -1;
            this.groupFooter = false;
        }

        ItemData(int groupIndex, int groupPosition, int childIndex, boolean groupFooter) {
            this.groupIndex = groupIndex;
            this.groupPosition = groupPosition;
            this.isGroup = false;
            this.childIndex = childIndex;
            this.groupFooter = groupFooter;
        }
    }

    public DataSetGroup() {
    }

    public DataSetGroup(boolean footer) {
        mGroupFooter = footer;
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public int getGroupCount() {
        return mGroupDataSet.size();
    }

    @Override
    public int getChildCount(int groupIndex) {
        if (groupIndex >= mChildDataSet.size()) {
            return -1;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null) {
            return 0;
        }
        return childList.size();
    }

    @Override
    public int getGroupIndex(int position) {
        if (!isLegalPosition(position)) {
            return -1;
        }
        return getItem(position).groupIndex;
    }

    @Override
    public int getGroupPosition(int position) {
        if (!isLegalPosition(position)) {
            return -1;
        }
        return getItem(position).groupPosition;
    }

    @Override
    public Index getChildIndex(int position) {
        if (!isLegalPosition(position)) {
            return null;
        }
        ItemData data = getItem(position);
        return new Index(data.groupIndex, data.childIndex);
    }

    @Override
    public boolean isGroup(int position) {
        if (!isLegalPosition(position)) {
            return false;
        }
        return getItem(position).isGroup;
    }

    @Override
    public void setDataSet(List<Group> groups, List<List<Child>> children) {
        clear();
        mGroupDataSet.addAll(groups);
        mChildDataSet.addAll(children);
        resetDataSet();
    }

    @Override
    public void addGroup(Group group, List<Child> children) {
        mGroupDataSet.add(group);
        mChildDataSet.add(children);
        resetDataSet();
    }

    @Override
    public void addGroup(Group group, List<Child> children, int groupIndex) {
        if (groupIndex > getGroupCount()) {
            groupIndex = getGroupCount();
        }
        mGroupDataSet.add(groupIndex, group);
        mChildDataSet.add(groupIndex, children);
        resetDataSet();
    }

    @Override
    public void addChildren(int groupIndex, List<Child> children) {
        if (groupIndex >= mGroupDataSet.size() || children == null || children.isEmpty()) {
            return;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null) {
            childList = new ArrayList<>();
            mChildDataSet.set(groupIndex, childList);
        }
        childList.addAll(children);
        resetDataSet();
    }

    @Override
    public void addChild(int groupIndex, Child child) {
        if (groupIndex >= mGroupDataSet.size()) {
            return;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null) {
            childList = new ArrayList<>();
            mChildDataSet.set(groupIndex, childList);
        }
        childList.add(child);
        resetDataSet();
    }

    @Override
    public Group getItemGroup(int groupIndex) {
        if (groupIndex >= mGroupDataSet.size()) {
            return null;
        }
        return mGroupDataSet.get(groupIndex);
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(mGroupDataSet);
    }

    @Override
    public Child getItemChild(int groupIndex, int childIndex) {
        if (groupIndex >= mChildDataSet.size()) {
            return null;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null || childIndex >= childList.size()) {
            return null;
        }
        return mChildDataSet.get(groupIndex).get(childIndex);
    }

    @Override
    public List<Child> getChildren(int groupIndex) {
        if (groupIndex >= mChildDataSet.size()) {
            return null;
        }
        return new ArrayList<>(mChildDataSet.get(groupIndex));
    }

    @Override
    public void removeGroup(int groupIndex) {
        if (groupIndex >= mGroupDataSet.size()) {
            return;
        }
        mGroupDataSet.remove(groupIndex);
        mChildDataSet.remove(groupIndex);
        resetDataSet();
    }

    @Override
    public void removeChild(int groupIndex, int childIndex) {
        if (groupIndex >= mGroupDataSet.size()) {
            return;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null || childIndex >= childList.size()) {
            return;
        }
        childList.remove(childIndex);
        resetDataSet();
    }

    @Override
    public void removeChild(int groupIndex, Child child) {
        if (groupIndex >= mGroupDataSet.size() || child == null) {
            return;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        if (childList == null || !childList.contains(child)) {
            return;
        }
        childList.remove(child);
        resetDataSet();

    }

    @Override
    public void clear() {
        mDataSet.clear();
        mGroupDataSet.clear();
        mChildDataSet.clear();
    }

    @Override
    public void clearChildren(int groupIndex) {
        if (mChildDataSet.size() <= groupIndex) {
            return;
        }
        List<Child> children = mChildDataSet.get(groupIndex);
        if (children != null) {
            children.clear();
        }
        resetDataSet();
    }

    @Override
    public boolean isEmpty() {
        return mDataSet.isEmpty();
    }

    @Override
    public boolean isEmptyChildren(int groupIndex) {
        if (groupIndex >= mChildDataSet.size()) {
            return true;
        }
        List<Child> childList = mChildDataSet.get(groupIndex);
        return childList == null || childList.isEmpty();
    }

    @Override
    public boolean hasGroupFooter() {
        return mGroupFooter;
    }

    @Override
    public boolean isGroupFooter(int position) {
        if (hasGroupFooter()) {
            ItemData itemData = mDataSet.get(position);
            return itemData.groupFooter;
        }
        return false;
    }

    private boolean isLegalPosition(int position) {
        return position < mDataSet.size();
    }

    private ItemData getItem(int position) {
        return mDataSet.get(position);
    }

    private void resetDataSet() {
        mDataSet.clear();

        int groupPosition = 0;
        for (int groupIndex = 0; groupIndex < mGroupDataSet.size(); groupIndex++) {
            List<Child> childList = (mChildDataSet == null || mChildDataSet.isEmpty() || mChildDataSet.size() <= groupIndex) ? null : mChildDataSet.get(groupIndex);
            if (childList == null) {
                mChildDataSet.add(childList = new ArrayList<Child>());
            }
            mDataSet.add(new ItemData(groupIndex, groupPosition));
            for (int childIndex = 0; childIndex < childList.size(); childIndex++) {
                mDataSet.add(new ItemData(groupIndex, groupPosition, childIndex, false));
            }
            if (hasGroupFooter()) {
                mDataSet.add(new ItemData(groupIndex, groupPosition, childList.size(), true));
                groupPosition += childList.size() + 2;
            } else {
                groupPosition += childList.size() + 1;
            }

//            groupPosition = mDataSet.size();

        }
    }
}
