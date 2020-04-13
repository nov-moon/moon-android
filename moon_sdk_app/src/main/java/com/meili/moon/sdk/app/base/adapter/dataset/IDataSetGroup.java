package com.meili.moon.sdk.app.base.adapter.dataset;

import java.util.List;

/**
 * Created by imuto on 15/12/1.
 */
public interface IDataSetGroup<Group, Child> extends IBaseDataSet{

    class Index {
        public final int groupIndex;
        public final int childIndex;

        public Index(int groupIndex, int childIndex) {
            this.groupIndex = groupIndex;
            this.childIndex = childIndex;
        }
    }

    int getGroupCount();

    int getChildCount(int groupIndex);

    /**
     * find group index, by list position.
     *
     * @param position list position.
     * @return group index.
     */
    int getGroupIndex(int position);

    /**
     * find group position, by list position.
     *
     * @param position any list position
     * @return
     */
    int getGroupPosition(int position);

    /**
     * find child index, by list position.
     *
     * @param position list position.
     * @return child index.
     */
    Index getChildIndex(int position);


    boolean isGroup(int position);

    void setDataSet(List<Group> groups, List<List<Child>> children);

    void addGroup(Group group, List<Child> children);
    void addGroup(Group group, List<Child> children, int index);

    void addChildren(int groupIndex, List<Child> children);

    void addChild(int groupIndex, Child child);

    Group getItemGroup(int groupIndex);

    List<Group> getGroups();

    Child getItemChild(int groupIndex, int childIndex);

    List<Child> getChildren(int groupIndex);

//  List<List<Child>> getChilds();

    void removeGroup(int groupIndex);

    void removeChild(int groupIndex, int childIndex);
    void removeChild(int groupIndex, Child childIndex);

    boolean isEmpty();

    boolean isEmptyChildren(int groupIndex);

    boolean hasGroupFooter();

    boolean isGroupFooter(int position);

    void clearChildren(int groupIndex);
}
