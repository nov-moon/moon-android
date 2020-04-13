package com.meili.moon.gradle.plugin

public class MavenExtension {
    private String group
    private String artifact
    private String version

    String getGroup() {
        return group
    }

    void group(String group) {
        this.group = group
    }

    String getArtifact() {
        return artifact
    }

    void artifact(String artifact) {
        this.artifact = artifact
    }

    String getVersion() {
        return version
    }

    void version(String version) {
        this.version = version
    }
}