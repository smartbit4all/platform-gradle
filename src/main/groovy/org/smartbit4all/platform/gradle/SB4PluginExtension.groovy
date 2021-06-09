package org.smartbit4all.platform.gradle

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory

class SB4PluginExtension {

    static final String EXTENSION_NAME = "smartbit4all"

    String sourceEncoding = "UTF-8"
    String vaadinVersion = "14.4.6"

    final SB4OpenApiExtension openApi

    public SB4PluginExtension(ObjectFactory objects) {
        openApi = objects.newInstance(SB4OpenApiExtension)
        openApi.apiDescriptorPath = ""
        //openApi.outputDir = ""
        openApi.modelPackage = ""
        openApi.modelPackagePrefix = "org.smartbit4all.api"
        openApi.modelPackagePostfix = "model"
        openApi.apiPackage = ""
        openApi.apiPackagePrefix = "org.smartbit4all.api"
        openApi.apiPackagePostfix = "service"
        openApi.invokerPackage = ""
        openApi.invokerPackagePrefix = "org.smartbit4all.api"
        openApi.invokerPackagePostfix = "service.util"
        openApi.genModel = false
        openApi.genApiRestClient = false
        openApi.genApiRestServer = false
        openApi.runGenAllOnCompile = false
        openApi.importMappings = objects.mapProperty(String.class, String.class)
    }

    public SB4OpenApiExtension getOpenApi() {
        return openApi
    }

    public void openApi(Action<? super SB4OpenApiExtension> action) {
        action.execute(openApi)
    }
}
