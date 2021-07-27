package org.smartbit4all.platform.gradle

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property;

/**
 * parameters for OpenApi generation
 */
abstract public class SB4OpenApiExtension {

    abstract public Property<String> getApiDescriptorPath()
    // abstract public Property<String> getOutputDir()
    abstract public Property<String> getModelPackage()
    abstract public Property<String> getModelPackagePrefix()
    abstract public Property<String> getModelPackagePostfix()
    abstract public Property<String> getApiPackage()
    abstract public Property<String> getApiPackagePrefix()
    abstract public Property<String> getApiPackagePostfix()
    abstract public Property<String> getInvokerPackage()
    abstract public Property<String> getInvokerPackagePrefix()
    abstract public Property<String> getInvokerPackagePostfix()

    abstract public Property<Boolean> getGenModel()
    abstract public Property<Boolean> getGenApiRestClient()
    abstract public Property<Boolean> getGenApiRestServer()

    abstract public Property<Boolean> getRunGenAllOnCompile()

    abstract public MapProperty<String, String> getImportMappings()

    abstract public Property<String> getDateTimeMapping()

}

