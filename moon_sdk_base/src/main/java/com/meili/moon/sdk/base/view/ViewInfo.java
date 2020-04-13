package com.meili.moon.sdk.base.view;

/**
 * Created by imuto on 16/5/13.
 */
/*package*/ final class ViewInfo {
    public int value;
    public int parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewInfo viewInfo = (ViewInfo) o;

        if (value != viewInfo.value) return false;
        return parentId == viewInfo.parentId;

    }

    @Override
    public int hashCode() {
        int result = value;
        result = 31 * result + parentId;
        return result;
    }
}
