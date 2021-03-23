// Generated by WebFx

module webfx.buildtool {

    // Direct dependencies modules
    requires info.picocli;
    requires java.base;
    requires java.xml;
    requires webfx.lib.reusablestream;

    // Exported packages
    exports dev.webfx.buildtool;
    exports dev.webfx.buildtool.cli;
    exports dev.webfx.buildtool.modulefiles;
    exports dev.webfx.buildtool.sourcegenerators;
    exports dev.webfx.buildtool.util.javacode;
    exports dev.webfx.buildtool.util.splitfiles;
    exports dev.webfx.buildtool.util.textfile;
    exports dev.webfx.buildtool.util.xml;

    // Resources packages
    opens dev.webfx.buildtool.cli;

}