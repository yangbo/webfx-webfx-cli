package dev.webfx.buildtool.modulefiles;

import dev.webfx.buildtool.LibraryModule;
import org.w3c.dom.Node;
import dev.webfx.buildtool.ModuleDependency;
import dev.webfx.buildtool.ProjectModule;
import dev.webfx.tools.util.reusablestream.ReusableStream;
import dev.webfx.buildtool.util.xml.XmlUtil;

import java.nio.file.Path;

/**
 * @author Bruno Salmon
 */
public final class WebFxModuleFile extends XmlModuleFile {

    public WebFxModuleFile(ProjectModule module) {
        super(module, true);
    }

    public Path getModulePath() {
        return resolveFromModuleHomeDirectory("webfx.xml");
    }

    public boolean isExecutable() {
        return getBooleanModuleAttributeValue("executable");
    }

    public boolean isInterface() {
        return getBooleanModuleAttributeValue("interface");
    }

    public boolean isAutomatic() {
        return getBooleanModuleAttributeValue("automatic");
    }

    public boolean areSourcePackagesExported() {
        return lookupNode("/module/packages/source-packages") != null;
    }

    public ReusableStream<String> getExplicitSourcePackages() {
        return lookupNodeListTextContent("/module/packages//source-package");
    }

    public ReusableStream<String> getHiddenPackages() {
        return lookupNodeListTextContent("" +
                "/module/packages//hidden-package");
    }

    public ReusableStream<String> getResourcePackages() {
        return lookupNodeListTextContent("/module/packages//resource-package");
    }

    public String implementingInterface() {
        return getModuleAttributeValue("implements-module");
    }

    public String getModuleProperty(String property) {
        return getModuleAttributeValue(property);
    }

    public ReusableStream<Path> getChildrenModules() {
        return XmlUtil.nodeListToReusableStream(lookupNodeList("/module/modules//module"), node -> resolveFromModuleHomeDirectory(node.getTextContent()));
    }

    public ReusableStream<ModuleDependency> getSourceModuleDependencies() {
        return lookupDependencies("/module/dependencies//source-module", ModuleDependency.Type.SOURCE);
    }

    public ReusableStream<ModuleDependency> getPluginModuleDependencies() {
        return lookupDependencies("/module/dependencies//plugin-module", ModuleDependency.Type.PLUGIN);
    }

    public ReusableStream<ModuleDependency> getResourceModuleDependencies() {
        return lookupDependencies("/module/dependencies/resource-modules//module", ModuleDependency.Type.RESOURCE);
    }

    public ReusableStream<LibraryModule> getLibraryModules() {
        return XmlUtil.nodeListToReusableStream(lookupNodeList("/module/libraries//module"), LibraryModule::new);
    }

    public ReusableStream<String> getRequiredPackages() {
        return lookupNodeListTextContent("/module/required-conditions//if-uses-java-package");
    }

    public ReusableStream<String> getEmbedResources() {
        return lookupNodeListTextContent("/module/embed-resources//resource");
    }

    public ReusableStream<String> getSystemProperties() {
        return lookupNodeListTextContent("/module/system-properties//property");
    }

    public ReusableStream<String> getArrayNewInstanceClasses() {
        return lookupNodeListTextContent("/module/reflect/array-new-instance//class");
    }

    public String getGraalVmReflectionJson() {
        return lookupNodeTextContent("/module/graalvm-reflection-json");
    }

    public ReusableStream<String> providedJavaServices() {
        return lookupNodeListAttribute("/module/providers//provider", "spi").distinct();
    }

    public ReusableStream<String> providedJavaServicesProviders(String javaService) {
        return lookupNodeListTextContent("/module/providers//provider[@spi='" + javaService + "']");
    }

    public Node getHtmlNode() {
        return lookupNode("/module/html");
    }

    private boolean getBooleanModuleAttributeValue(String attribute) {
        return XmlUtil.getBooleanAttributeValue(getDocument().getDocumentElement(), attribute);
    }

    private String getModuleAttributeValue(String attribute) {
        return XmlUtil.getAttributeValue(getDocument().getDocumentElement(), attribute);
    }
}