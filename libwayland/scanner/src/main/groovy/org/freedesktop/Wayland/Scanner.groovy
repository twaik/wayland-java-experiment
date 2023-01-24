//file:noinspection GroovyAssignabilityCheck
package org.freedesktop.Wayland

import org.gradle.api.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.xml.sax.SAXException

import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChild

@SuppressWarnings('unused')
class Scanner implements Plugin<Project> {
    static abstract class WaylandScannerExtension {
        Project project
        static final String endl = "\n"
        static final String schema = """\
		|<?xml version="1.0" encoding="UTF-8"?>
		|<!-- Do not edit. This document is automatically generated
		|	 from wayland.dtd using IntelliJ Idea's XML Actions... -->
		|<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
		|	<xs:element name="protocol">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="copyright"/>
		|				<xs:element minOccurs="0" ref="description"/>
		|				<xs:element maxOccurs="unbounded" ref="interface"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="copyright" type="xs:string"/>
		|	<xs:element name="interface">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|				<xs:choice maxOccurs="unbounded">
		|					<xs:element ref="request"/>
		|					<xs:element ref="event"/>
		|					<xs:element ref="enum"/>
		|				</xs:choice>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="version" use="required"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="request">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|				<xs:element minOccurs="0" maxOccurs="unbounded" ref="arg"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="type"/>
		|			<xs:attribute name="since"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="event">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|				<xs:element minOccurs="0" maxOccurs="unbounded" ref="arg"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="type"/>
		|			<xs:attribute name="since"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="enum">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|				<xs:element minOccurs="0" maxOccurs="unbounded" ref="entry"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="since"/>
		|			<xs:attribute name="bitfield"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="entry">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="value" use="required"/>
		|			<xs:attribute name="summary"/>
		|			<xs:attribute name="since"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="arg">
		|		<xs:complexType>
		|			<xs:sequence>
		|				<xs:element minOccurs="0" ref="description"/>
		|			</xs:sequence>
		|			<xs:attribute name="name" use="required"/>
		|			<xs:attribute name="type" use="required"/>
		|			<xs:attribute name="summary"/>
		|			<xs:attribute name="interface"/>
		|			<xs:attribute name="allow-null"/>
		|			<xs:attribute name="enum"/>
		|		</xs:complexType>
		|	</xs:element>
		|	<xs:element name="description">
		|		<xs:complexType mixed="true">
		|			<xs:attribute name="summary" use="required"/>
		|		</xs:complexType>
		|	</xs:element>
		|</xs:schema>
"""
        class element_t {
            String name = ""
            String summary = ""
            String description = ""
            static final List<String> keywords = [
                    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
                    "default", "double", "do", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
                    "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
                    "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                    "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
            ]

            static String sanitise(String str) {
                return ((keywords.contains(str)) ? "_" : "") + str
            }
        }

        class argument_t extends element_t {
            String type = ""
            String iface = ""
            String iface_orig = ""
            String enum_iface = ""
            String enum_name = ""
            Boolean allow_null = false

            String print_enum_wire_type() {
                switch (type) {
                    case "int":
                        return "int32_t"
                    case "uint":
                        return "uint32_t"
                    default:
                        throw new RuntimeException("Enum type must be int or uint (have " + type + ")")
                }
            }

            String print_type() {
                //if (!iface.empty && type != "new_id")
                //    return snakeToCamel(iface, true)
                if ((type == "object" || type == "new_id") && "wl_callback".equals(iface_orig))
                    return "int";
                if (type == "object" || type == "new_id")
                    return "/* " + snakeToCamel(iface, true) + " */ Resource"
                switch (type) {
                    case "new_id":
                        return "/* " + snakeToCamel(iface, true) + " */ int"
                    case "int":
                        return "int"
                    case "uint":
                        return "int"
                    case "fixed":
                        return "Fixed"
                    case "string":
                        return "String"
                    case "object":
                        return "Object"
                    case "fd":
                        return "int"
                    case "array":
                        //return "Array"
                        return "Object"
                    default:
                        throw new RuntimeException("Can not use this type: " + type)
                }
            }

            String print_short() {
                switch (type) {
                    case "int":
                        return "i"
                    case "uint":
                        return "u"
                    case "fixed":
                        return "f"
                    case "string":
                        return "s"
                    case "object":
                        return "o"
                    case "new_id":
                        return "n"
                    case "array":
                        return "a"
                    case "fd":
                        return "h"
                    default:
                        return "x"
                }
            }
        }

        class event_t extends element_t {
            List<argument_t> args
            int since = 0
            argument_t ret
            int opcode = 0

            String signature() {
                def ss = "" << ""
                if (since > 1)
                    ss << since
                args.each { arg ->
                    if (arg.allow_null)
                        ss << "?"
                    if (arg.type == "new_id" && arg.iface.empty)
                        ss << "su"
                    ss << arg.print_short()
                }
                return ss
            }

            String types(boolean withNames) {
                def ss = "" << ""
                if (withNames)
                    args.each { arg -> ss << arg.print_type() << " " << arg.name << ", " }
                else
                    args.each { arg -> ss << arg.print_type() << ".class, " }
                return removeLastCharacters(ss.toString(), 2)
            }

            String writeSignalHeader() {
                def ss = "" << ""
                def interfaces = []
                args.each { arg ->
                    if (arg.type == "object" || arg.type == "new_id")
                        //interfaces.add("\"" + arg.iface_orig + "\"")
                        interfaces.add(snakeToCamel(arg.iface, true) + ".iface")
                    else
                        interfaces.add("null")
                }
                ss << "      new Message(\""
                ss << name << "\", \"" << sanitise(snakeToCamel(name, false)) << "\", \""
                ss << signature() << "\", new Interface[] { "
                ss << interfaces.join(", ")
                ss << " })"
                return ss
            }

            String writeRequestBody() {
                def ss = "" << ""
                if (!description.empty) {
                    ss << endl << "      /**" << ((!summary.empty) ? " " << summary : "") << endl
                    description.stripIndent().trim().eachLine { line ->
                        ss << "       * " << line << endl
                    }
                    ss << "       */" << endl
                } else if (!summary.empty)
                    ss << "      /** " << summary << " */" << endl
                ss << "      public default void " << sanitise(snakeToCamel(name, false))
                ss << "(" + types(true) << ") {};" << endl
            }

            String writeEventBody() {
                def ss = "" << ""
                if (!description.empty) {
                    ss << "    /**" << ((!summary.empty) ? " " << summary : "") << endl
                    description.stripIndent().trim().eachLine { line ->
                        ss << "     * " << line << endl
                    }
                    ss << "     */" << endl
                } else if (!summary.empty)
                    ss << "/** " << summary << " */" << endl

                def names = []
                args.each { arg -> names.add(arg.name) }

                ss << "    public void " << sanitise(snakeToCamel(name, false)) << "(" << types(true) << ") {" << endl
                ss << "      if (instance != null" << ((since > 0) ? " && instance.getVersion() >= " + since : "") << ")" << endl
                ss << "        instance.postEvent(" << opcode << ((names.size() > 0) ? ", " : "") << names.join(", ") << ");" << endl
                ss << "    }" << endl

                if (since > 0) {
                    ss << endl
                    ss << "    public boolean can" << snakeToCamel(name, true) << "() {" << endl
                    ss << "      return instance != null && instance.getVersion() >= " << since << ";" << endl
                    ss << "    }" << endl
                }
                return ss
            }
        }

        class request_t extends event_t {
        }

        class enum_entry_t extends element_t {
            String value = ""
        }

        class enumeration_t extends element_t {
            List<enum_entry_t> entries
            Boolean bitfield = false

            String writeBody() {
                def ss = "" << ""
                if (!description.empty) {
                    ss << "    /**" << ((!summary.empty) ? " " << summary : "") << endl
                    description.stripIndent().trim().eachLine { line ->
                        ss << "     * " << line << endl
                    }
                    ss << "     */" << endl
                } else if (!summary.empty)
                    ss << "/** " << summary << " */" << endl

                ss << "    public enum " << snakeToCamel(name, true) << " {" << endl
                entries.each { entry ->
                    if (!entry.summary.empty)
                        ss << "      /** " << entry.summary << " */" << endl

                    ss << "      " << (entry.name.charAt(0).isDigit() ? "_" : "") << entry.name.toUpperCase()
                    ss << "(" << entry.value << ")," << endl
                }
                ss.setLength(ss.length() - 2)
                ss << ";" << endl

                ss << endl
                ss << "      public final int value;" << endl
                ss << "      " << snakeToCamel(name, true) << "(int value) {" << endl
                ss << "        this.value = value;" << endl
                ss << "      }" << endl
                ss << "    }" << endl
            }
        }

        class post_error_t extends element_t {
            String writeBody() {
                def ss = "" << ""

                return ss
            }
        }

        class interface_t extends element_t {
            int version = 1
            String orig_name = ""
            int destroy_opcode = 0
            List<request_t> requests
            List<event_t> events
            List<enumeration_t> enums
            List<post_error_t> errors

            String writeBody() {
                def ss = "" << ""

                if (!description.empty) {
                    ss << "  /**" << ((!summary.empty) ? " " + summary : "") << endl
                    description.trim().eachLine { line -> ss << "   * " << line.trim().stripIndent() << endl }
                    ss << "   */" << endl
                } else if (!summary.empty)
                    ss << "  /** " << summary << " */" << endl
                ss << "  public static class " << snakeToCamel(name, true) << " {" << endl

                if (!requests.empty) {
                    ss << "    public interface Callbacks extends Resource.Callbacks {" << endl
                    requests.each { request -> ss << request.writeRequestBody() }
                    ss << "    }" << endl
                }

                ss << endl

                if (!events.empty)
                    events.each { event -> ss << event.writeEventBody() << endl }

                if (!enums.empty)
                    enums.each { e -> ss << e.writeBody() << endl }

                def eventSignals = []
                def requestSignals = []
                events.each { event -> eventSignals.add(event.writeSignalHeader()) }
                requests.each { request -> requestSignals.add(request.writeSignalHeader()) }

                ss << "    public " << snakeToCamel(name, true) << "() {}" << endl << endl
                ss << "    public " << snakeToCamel(name, true) << "(Resource id) {"
                if (!requests.empty) {
                    ss << endl << "      id.setCallbacks(new Callbacks() {});" << endl << "    "
                }
                ss << "}" << endl

                ss << "    public static final Interface iface = Interface.create(\"" << orig_name << "\", "
                ss << version << ", new Message[] /* requests */ {" << endl
                ss << ((requestSignals.size()) > 0 ? requestSignals.join(", " + endl) << endl : "")
                ss << "    }, new Message[] /* events */ {" << endl
                ss << ((eventSignals.size()) > 0 ? eventSignals.join(", " + endl) << endl : "")
                ss << "    }, Callbacks.class);" << endl
                ss << endl
                ss << "    public Resource instance = null;" << endl
                ss << "  }" << endl

                return ss
            }
        }

        class protocol_t extends element_t {
            String copyright = ""
            String javaPackage = ""
            File file = null
            List<interface_t> ifaces = new ArrayList<interface_t>()

            String writeBody() {
                def ss = "" << ""
                ss << "// AUTO-GENERATED -- DO NOT EDIT" << endl
                if (!copyright.empty) {
                    ss << "/*" << endl
                    copyright.stripIndent().trim().eachLine { line ->
                        ss << " * " << line << endl
                    }
                    ss << " */" << endl
                }

                ss << "package " << javaPackage << ";" << endl
                ss << endl
                ss << "import org.freedesktop.Wayland.*;" << endl
                ss << "import static org.freedesktop.Wayland.Interface.Message;" << endl
                protocols.each { protocol ->
                    if (protocol != this)
                        ss << "import " << javaPackage << "." << snakeToCamel(protocol.name, true) << ".*;" << endl
                }
                ss << endl
                ss << "@SuppressWarnings({\"ALL\"})" << endl
                ss << "public class " << snakeToCamel(name, true) << " {" << endl

                ifaces.each { iface -> ss << iface.writeBody() << endl }
                ss = removeLastCharacters(ss.toString(), 1) << ""

                ss << "}" << endl
                return ss
            }
        }

        // Function to convert snake case
        // to camel case
        @SuppressWarnings('GrEqualsBetweenInconvertibleTypes')
        static String snakeToCamel(String str, boolean capitalize) {
            // Capitalize first letter of string
            if (capitalize)
                str = str.substring(0, 1).toUpperCase() << str.substring(1)

            // Convert to StringBuilder
            StringBuilder builder
                    = new StringBuilder(str)

            // Traverse the string character by
            // character and remove underscore
            // and capitalize next letter
            for (int i = 0; i < builder.length(); i++) {

                // Check char is underscore
                if (builder.charAt(i) == '_') {

                    builder.deleteCharAt(i)
                    builder.replace(
                            i, i + 1,
                            String.valueOf(
                                    Character.toUpperCase(
                                            builder.charAt(i))))
                }
            }

            // Return in String type
            return builder.toString()
        }

        static String removePrefix(String name) {
            def prefix_len = name.indexOf('_')
            if (prefix_len != -1) {
                def prefix = name.substring(0, prefix_len)
                if (prefix == "wl" || prefix == "wp")
                    return name.substring(prefix_len + 1, name.size())
            }
            return name
        }

        static String removeLastCharacters(String text, int count) {
            if (text.length() - count < 0)
                return text
            return text.substring(0, text.length() - count)
        }

        boolean xmlValidate(File xmlFile) {
            File schemaFile = new File(project.buildDir, "/intermediates/wayland/wayland.xsd")
            schemaFile.getParentFile().mkdirs()
            schemaFile.text = ''
            schemaFile << schema.stripMargin("|").stripIndent()
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            try {
                schemaFactory.newSchema(schemaFile)
                        .newValidator()
                        .validate(new StreamSource(xmlFile))
                return true
            } catch (SAXException e) {
                println(xmlFile.toString() + " is NOT valid reason:" + e)
                return false
            } catch (IOException ignored) {
                return false
            }
        }

        @SuppressWarnings('GroovyAssignabilityCheck')
        def parseXml(File xmlFile) {
            xmlValidate(xmlFile)
            String[] blacklistedInterfaces = ["wl_display", "wl_registry", "wl_shm", "wl_callback"]

            protocol_t p = new protocol_t()
            def protocol = new XmlSlurper().parseText(xmlFile.getText())

            p.name = protocol.@name
            protocol.children("copyright").each { cr ->
                p.copyright = cr.text()
            }
            protocol.children("interface").each { iface ->
                if (blacklistedInterfaces.contains(iface.@name))
                    return

                interface_t i = new interface_t()
                i.requests = new ArrayList<request_t>()
                i.events = new ArrayList<event_t>()
                i.enums = new ArrayList<enumeration_t>()
                i.errors = new ArrayList<post_error_t>()
                i.destroy_opcode = -1
                i.orig_name = iface.@name
                i.name = removePrefix(iface.@name.text())

                if (iface.@version != null)
                    i.version = iface.@version.text() as int
                else
                    i.version = 1

                if (iface.description) {
                    i.summary = String.valueOf(iface.description.@summary).capitalize()
                    i.description = iface.description.text()
                }

                int opcode = 0 // Opcodes are in order of the XML. (Sadly undocumented)
                iface.children("request").each { req ->
                    request_t r = new request_t()
                    r.name = req.@name
                    r.opcode = opcode++
                    r.args = new ArrayList<argument_t>()

                    if (req?.@since?.text()?.length() > 0)
                        r.since = req.@since.text() as int
                    else
                        r.since = 0

                    if (req.description) {
                        r.summary = String.valueOf(req.description.@summary).capitalize()
                        r.description = req.description.text()
                    }

                    // destruction takes place through the class destructor
                    if (req.@name == "destroy")
                        i.destroy_opcode = r.opcode
                    req.children("arg").each { arg ->
                        argument_t a = new argument_t()
                        a.name = arg.@name
                        a.type = arg.@type
                        a.summary = String.valueOf(arg.@summary).capitalize()
                        a.iface_orig = arg.@interface
                        if ("wl_buffer" == arg.@interface.text())
                            a.iface = "shm_buffer"
                        else
                            a.iface = removePrefix(arg.@interface.text())

                        String enum_val = arg.@enum
                        if (!enum_val.empty) {
                            if (enum_val.indexOf('.') == -1) {
                                a.enum_iface = i.name
                                a.enum_name = enum_val
                            } else {
                                a.enum_iface = removePrefix(enum_val.substring(0, enum_val.indexOf('.')))
                                a.enum_name = enum_val.substring(enum_val.indexOf('.') + 1)
                            }
                        }

                        a.allow_null = arg.@"allow-null" == "true"
                        if (arg.@type == "new_id")
                            r.ret = a
                        r.args.add(a)
                    }
                    i.requests.add(r)
                }

                opcode = 0
                iface.children("event").each { ev ->
                    event_t e = new event_t()
                    e.name = ev.@name
                    e.opcode = opcode++
                    e.args = new ArrayList<argument_t>()

                    if (ev?.@since?.text()?.length() > 0)
                        e.since = ev.@since.text() as int
                    else
                        e.since = 0

                    if (ev.description) {
                        e.summary = String.valueOf(ev.description.@summary).capitalize()
                        e.description = ev.description.text()
                    }

                    ev.children("arg").each { arg ->
                        argument_t a = new argument_t()
                        a.name = arg.@name
                        a.type = arg.@type
                        a.summary = String.valueOf(arg.@summary).capitalize()
                        a.iface_orig = arg.@interface
                        if ("wl_buffer" == arg.@interface.text())
                            a.iface = "shm_buffer"
                        else
                            a.iface = removePrefix(arg.@interface.text())

                        String enum_val = arg.@enum.text()
                        if (!enum_val.empty) {
                            if (enum_val.indexOf('.') == -1) {
                                a.enum_iface = i.name
                                a.enum_name = enum_val
                            } else {
                                a.enum_iface = removePrefix(enum_val.substring(0, enum_val.indexOf('.')))
                                a.enum_name = enum_val.substring(enum_val.indexOf('.') + 1)
                            }
                        }

                        a.allow_null = arg.@"allow-null" == "true"
                        if (arg.@type == "new_id")
                            e.ret = a
                        e.args.add(a)
                    }
                    i.events.add(e)
                }

                iface.children("enum").each { en ->
                    enumeration_t e = new enumeration_t()
                    e.name = en.@name
                    e.bitfield = (en.@bitfield == "true")
                    e.entries = new ArrayList<enum_entry_t>()

                    if (en.description) {
                        e.summary = String.valueOf(en.description.@summary).capitalize()
                        e.description = en.description.text()
                    }

                    en.children("entry").each { entry ->
                        enum_entry_t ent = new enum_entry_t()
                        ent.name = entry.@name
                        if (ent.name != null &&
                                (ent.name == "default" || Character.isDigit(ent.name.charAt(0))))
                            ent.name = "_" + ent.name

                        ent.value = entry.@value
                        ent.summary = String.valueOf(entry.@summary).capitalize()
                        e.entries.add(ent)

                        if (e.name == "error") {
                            post_error_t error = new post_error_t()
                            error.name = ent.name
                            error.summary = String.valueOf(ent.summary).capitalize()
                            error.description = ent.description
                            i.errors.add(error)
                        }
                    }
                    i.enums.add(e)
                }
                p.ifaces.add(i)
            }
            return p
        }

        def scanFile(File xmlFile) {
            def protocol = parseXml(xmlFile)

            String outFolderName = "/generated/wayland/java/" + javaPackage.replaceAll("\\.", "/")
            String outName = snakeToCamel(protocol.name, true) << ".java"

            File outFolder = new File(project.buildDir, outFolderName)
            File out = new File(outFolder, outName)

            protocol.javaPackage = javaPackage
            protocol.file = out
            protocols.add(protocol)
        }

        def scanDirectory(File dir) {
            File[] files = dir.listFiles()
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    scanDirectory(files[i])
                } else if (files[i].isFile()) {
                    if (files[i].getName().toLowerCase().endsWith(".xml")) {
                        scanFile(files[i])
                    }
                }
            }
        }

        List<protocol_t> protocols = new ArrayList<>()
        private String javaPackage = ""

        WaylandScannerExtension() {
            NodeChild.metaClass.children << { name ->
                return delegate.depthFirst().findAll { t -> t.name() == name }
            }
        }

        void from(String s) {
            if (javaPackage.empty)
                throw new Exception("javaPackage property is empty")

            File src = getProject().file(s)
            if (!src.exists())
                throw new Exception(src.absolutePath + " does not exist")

            if (src.isFile() && src.getName().toLowerCase().endsWith(".xml")) {
                scanFile(src)
            } else if (src.isDirectory()) {
                scanDirectory(src)
            } else throw new Exception(src.absolutePath + "is not directory or xml file")
        }

        void javaPackage(String s) {
            javaPackage = s
        }
    }

    @SuppressWarnings('GroovyFallthrough')
    static abstract class WaylandScannerTask extends DefaultTask {
        @Internal
        abstract Property<WaylandScannerExtension> getExtension()

        @SuppressWarnings('unused')
        @TaskAction
        def execute() {
            extension.get().protocols.each { protocol ->
                File outFolder = new File(protocol.file.getParent())
                outFolder.mkdirs()

                protocol.file.text = ''
                protocol.file << protocol.writeBody()
            }
        }
    }

    void apply(Project project) {
        project.extensions.create("wayland", WaylandScannerExtension)
        project.extensions.wayland.project = project

        Task task = project.tasks.register('generateWaylandProtocols', WaylandScannerTask) {
            extension = project.extensions.wayland
        }.get()

        project.sourceSets.main.java {
            srcDir "${project.buildDir}/generated/wayland/java"
        }

        project.tasks.matching {t -> t.name.contains("Java") || t.name.contains("Json")}.all {
            t -> t.dependsOn task
        }
    }
}