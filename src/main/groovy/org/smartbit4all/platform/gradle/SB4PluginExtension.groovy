package org.smartbit4all.platform.gradle

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory

class SB4PluginExtension {

    static final String EXTENSION_NAME = "smartbit4all"

    JavaVersion javaVersion = JavaVersion.VERSION_1_8
    String sourceEncoding = "UTF-8"
    String vaadinVersion = "14.4.6"

    final SB4OpenApiExtension openApi

    public SB4PluginExtension(ObjectFactory objects) {
        openApi = objects.newInstance(SB4OpenApiExtension)
        openApi.apiDescriptorPath = ""
        //openApi.outputDir = ""
        openApi.modelPackagePrefix = "org.smartbit4all.api"
        openApi.modelPackagePostfix = "model"
        openApi.genModel = true
        openApi.genApis = false
        openApi.runGenAllOnCompile = false
    }

    public SB4OpenApiExtension getOpenApi() {
        return openApi
    }

    public void openApi(Action<? super SB4OpenApiExtension> action) {
        action.execute(openApi)
    }
}
