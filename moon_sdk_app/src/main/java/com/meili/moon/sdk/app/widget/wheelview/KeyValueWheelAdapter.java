package com.meili.moon.sdk.app.widget.wheelview;

import com.meili.moon.sdk.app.base.IKeyValueModel;

import java.util.List;

/**
 * Author： fanyafeng
 * Date： 18/8/6 下午5:43
 * Email: fanyafeng@live.cn
 */
public class KeyValueWheelAdapter implements WheelAdapter {

    private List<IKeyValueModel> modelList;

    public KeyValueWheelAdapter(List<IKeyValueModel> modelList) {
        this.modelList = modelList;
    }

    @Override
    public int getItemsCount() {
        return modelList.size();
    }

    @Override
    public String getItem(int index) {
        return modelList.get(index).getKeyValueName();
    }


    @Override
    public int getMaximumLength() {
        return modelList.size();
    }
}
