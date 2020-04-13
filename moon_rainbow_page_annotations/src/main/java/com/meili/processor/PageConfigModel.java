package com.meili.processor;

/**
 * Author： fanyafeng
 * Date： 2019/2/26 4:44 PM
 * Email: fanyafeng@live.cn
 */
public class PageConfigModel {
    /**
     * 包名加类名全称
     */
    private String className;

    /**
     * 页面的affinity
     */
    private String affinityId;

    /**
     * 页面的注释
     */
    private String note;

    /**
     * 页面的PageName
     */
    private String pageName;

    /**
     * 页面配置的拦截器
     */
    private String[] interceptors;


    public PageConfigModel(String pageName, String affinityId, String note, String className, String[] interceptors) {
        this.pageName = pageName;
        this.affinityId = affinityId;
        this.note = note;
        this.className = className;
        this.interceptors = interceptors;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAffinityId() {
        return affinityId;
    }

    public void setAffinityId(String affinityId) {
        this.affinityId = affinityId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String[] getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(String[] interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public String toString() {
        return "PageNameModel{" +
                "className='" + className + '\'' +
                ", affinityId='" + affinityId + '\'' +
                ", note='" + note + '\'' +
                ", pageName='" + pageName + '\'' +
                '}';
    }
}
