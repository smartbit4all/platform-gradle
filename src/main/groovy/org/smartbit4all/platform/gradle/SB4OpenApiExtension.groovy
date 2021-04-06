package org.smartbit4all.platform.gradle

import org.gradle.api.provider.Property;

/**
 * parameters for OpenApi generation
 */
abstract public class SB4OpenApiExtension {

    abstract public Property<String> getApiDescriptorPath()
    // abstract public Property<String> getOutputDir()
    abstract public Property<String> getModelPackagePrefix()
    abstract public Property<String> getModelPackagePostfix()
    abstract public Property<String> getApiPackagePrefix()
    abstract public Property<String> getApiPackagePostfix()

    abstract public Property<Boolean> getGenModel();
    abstract public Property<Boolean> getGenApis();

    abstract public Property<Boolean> getRunGenAllOnCompile();
}

