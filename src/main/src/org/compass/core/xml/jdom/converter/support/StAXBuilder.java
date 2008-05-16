package org.compass.core.xml.jdom.converter.support;

import java.util.HashMap;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.jdom.Namespace;
import org.jdom.UncheckedJDOMFactory;

/**
 * Builds a JDOM {@link org.jdom.Document org.jdom.Document} using a
 * {@link javax.xml.stream.XMLStreamReader}.
 *
 * @author kimchy
 */
public class StAXBuilder {

    /**
     * Map that contains conversion from textual attribute types StAX uses,
     * to int values JDOM uses.
     */
    final static HashMap<String, Integer> attrTypes = new HashMap<String, Integer>(32);

    static {
        attrTypes.put("CDATA", Attribute.CDATA_TYPE);
        attrTypes.put("cdata", Attribute.CDATA_TYPE);
        attrTypes.put("ID", Attribute.ID_TYPE);
        attrTypes.put("id", Attribute.ID_TYPE);
        attrTypes.put("IDREF", Attribute.IDREF_TYPE);
        attrTypes.put("idref", Attribute.IDREF_TYPE);
        attrTypes.put("IDREFS", Attribute.IDREFS_TYPE);
        attrTypes.put("idrefs", Attribute.IDREFS_TYPE);
        attrTypes.put("ENTITY", Attribute.ENTITY_TYPE);
        attrTypes.put("entity", Attribute.ENTITY_TYPE);
        attrTypes.put("ENTITIES", Attribute.ENTITIES_TYPE);
        attrTypes.put("entities", Attribute.ENTITIES_TYPE);
        attrTypes.put("NMTOKEN", Attribute.NMTOKEN_TYPE);
        attrTypes.put("nmtoken", Attribute.NMTOKEN_TYPE);
        attrTypes.put("NMTOKENS", Attribute.NMTOKENS_TYPE);
        attrTypes.put("nmtokens", Attribute.NMTOKENS_TYPE);
        attrTypes.put("NOTATION", Attribute.NOTATION_TYPE);
        attrTypes.put("notation", Attribute.NOTATION_TYPE);
        attrTypes.put("ENUMERATED", Attribute.ENUMERATED_TYPE);
        attrTypes.put("enumerated", Attribute.ENUMERATED_TYPE);
    }

    // // // Configuration settings:

    /**
     * The factory for creating new JDOM objects
     */
    private JDOMFactory factory = null;

    /**
     * Whether ignorable white space should be ignored, ie not added
     * in the resulting JDOM tree. If true, it will be ignored; if false,
     * it will be added in the tree. Default value if false.
     */
    protected boolean cfgIgnoreWS = false;

    /**
     * Object that will be used when trying to remove indentation white
     * space: if so, the object is consulted to figure out what consistutes
     * indentation white space, as well as about context in which such
     * white space is to be removed.
     * <p>
     * Note that only such text events (CHARACTERS) are considered that
     * are not known to be fully ignorable (ignorable white space would
     * be reported as SPACE) by this removal process. SPACE events can
     * be trimmed simply by setting {@link #cfgIgnoreWS} to true.
     */
    protected StAXTextModifier textModifier = null;

    /**
     * Default constructor.
     */
    public StAXBuilder() {
    }

    /*
     * This sets a custom JDOMFactory for the builder.  Use this to build
     * the tree with your own subclasses of the JDOM classes.
     *
     * @param factory <code>JDOMFactory</code> to use
     */
    public void setFactory(JDOMFactory f) {
        factory = f;
    }

    public void setTextModifier(StAXTextModifier mod) {
        textModifier = mod;
    }

    /**
     * Method used to set value of {@link #cfgIgnoreWS}; that is, to
     * make parser either remove ignorable white space (true), or
     * to include it (false).
     * <p>
     * Whether all-whitespace text segment is ignorable white space or
     * not is based on DTD read in, as per XML specifications (white space
     * is only significant in mixed content or pure text elements).
     */
    public void setIgnoreWhitespace(boolean state) {
        cfgIgnoreWS = state;
    }

    /**
     * Method used to enable or disable automatic heuristic removal
     * of indentation white  If set to true, the builder will
     * try to remove white space that seems to be used for
     * indentation purposes; otherwise it will not try to do any removal.
     * <p>
     * Note that this setting only applies to all-whitespace segments
     * that have NOT been determined to be ignorable white space (either
     * because DTD is not available, or because such white space is in
     * mixed or text-only element content). As such it is a heuristics
     * that should only be enabled when application knows that such
     * white space removal does not cause problems.
     * <p>
     * Also note that internally the method calls
     * {@link #setTextModifier} with either the default text modifier
     * (true), or with null (false).
     */
    public void setRemoveIndentation(boolean state) {
        if (state) {
            setTextModifier(IndentRemover.getInstance());
        } else {
            setTextModifier(null);
        }
    }

    /**
     * Returns the current {@link org.jdom.JDOMFactory} in use, if
     * one has been previously set with {@link #setFactory}, otherwise
     * null.
     *
     * @return the factory builder will use
     */
    public JDOMFactory getFactory() {
        return factory;
    }

    /**
     * This will build a JDOM tree given a StAX stream reader.
     *
     * @param r Stream reader from which input is read.
     * @return <code>Document</code> - JDOM document object.
     * @throws XMLStreamException If the reader threw such exception (to
     *                            indicate a parsing or I/O problem)
     */
    public Document build(XMLStreamReader r)
            throws XMLStreamException {
        /* Should we do sanity checking to see that r is positioned at
         * beginning? Not doing so will allow creating documents from
         * sub-trees, though?  (not necessarily, depending on the
         * build loop: it may expect END_DOCUMENT?)
         */
        JDOMFactory f = factory;
        if (f == null) {
            f = new UncheckedJDOMFactory();
        }
        Document doc = f.document(null);
        buildTree(f, r, doc, textModifier);
        return doc;
    }

    /**
     * This takes a <code>XMLStreamReader</code> and builds up
     * a JDOM tree. Recursion has been eliminated by using nodes'
     * parent/child relationship; this improves performance somewhat
     * (classic recursion-by-iteration-and-explicit stack transformation)
     *
     * @param f    Node factory to use for creating JDOM nodes
     * @param r    Stream reader to use for reading the document from which
     *             to build the tree
     * @param doc  JDOM <code>Document</code> being built.
     * @param tmod Text modifier to use for modifying content of text
     *             nodes (CHARACTERS, not CDATA), if any; null if no modifications
     *             are needed (modifier is usually used for trimming unnecessary
     *             but non-ignorable white space).
     */
    protected void buildTree(JDOMFactory f, XMLStreamReader r, Document doc,
                             StAXTextModifier tmod)
            throws XMLStreamException {
        Element current = null; // At top level

        /* Only relevant when trying to trim indentation. But if so, let's
         * just always allow modifications in prolog/epilog.
         */
        boolean allowTextMods = (tmod != null);
        int evtType = XMLStreamConstants.START_DOCUMENT;

        main_loop:

        while (true) {
            int prevEvent = evtType;
            evtType = r.next();

            /* 11-Dec-2004, TSa: We may want to trim (indentation) white
             *    space... and it's easiest to do as a completely separate
             *    piece of logic, before the main switch.
             */
            if (allowTextMods) {
                // Ok; did we get CHARACTERS to potentially modify?
                if (evtType == XMLStreamConstants.CHARACTERS) {
                    // Mayhaps we could be interested in modifying it?
                    if (tmod.possiblyModifyText(r, prevEvent)) {
                        /* Need to get text before iterating to see the
                         * following event (as that'll lose it)
                         */
                        String txt = r.getText();
                        evtType = r.next();
                        // So how should the text be modified if at all?
                        txt = tmod.textToIncludeBetween(r, prevEvent, evtType,
                                txt);
                        // Need to output if it's non-empty text, then:
                        if (txt != null && txt.length() > 0) {
                            /* See discussion below for CHARACTERS case; basically
                             * we apparently can't add anything in epilog/prolog,
                             * not even white space.
                             */
                            if (current != null) {
                                f.addContent(current, f.text(txt));
                            }
                        }
                        prevEvent = XMLStreamConstants.CHARACTERS;
                        // Ok, let's fall down to handle new current event
                    }
                }
                // And then can just fall back to the regular handling
            }

            Content child;

            switch (evtType) {
                case XMLStreamConstants.CDATA:
                    child = f.cdata(r.getText());
                    break;

                case XMLStreamConstants.SPACE:
                    if (cfgIgnoreWS) {
                        continue main_loop;
                    }
                    // fall through

                case XMLStreamConstants.CHARACTERS:
                    /* Small complication: although (ignorable) white space
                    * is allowed in prolog/epilog, and StAX may report such
                    * event, JDOM barfs if trying to add it. Thus, let's just
                    * ignore all textual stuff outside the tree:
                    */
                    if (current == null) {
                        continue main_loop;
                    }

                    child = f.text(r.getText());
                    break;

                case XMLStreamConstants.COMMENT:
                    child = f.comment(r.getText());
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    break main_loop;

                case XMLStreamConstants.END_ELEMENT:
                    current = current.getParentElement();
                    if (tmod != null) {
                        allowTextMods = tmod.allowModificationsAfter(r, evtType);
                    }
                    continue main_loop;

                case XMLStreamConstants.ENTITY_DECLARATION:
                case XMLStreamConstants.NOTATION_DECLARATION:
                    /* Shouldn't really get these, but maybe some stream readers
                    * do provide the info. If so, better ignore it -- DTD event
                    * should have most/all we need.
                    */
                    continue main_loop;

                case XMLStreamConstants.ENTITY_REFERENCE:
                    child = f.entityRef(r.getLocalName());
                    break;

                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    child = f.processingInstruction(r.getPITarget(), r.getPIData());
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    // Ok, need to add a new element...
                {
                    Element newElem = null;
                    String nsURI = r.getNamespaceURI();
                    String elemPrefix = r.getPrefix(); // needed for special handling of elem's namespace
                    String ln = r.getLocalName();

                    if (nsURI == null || nsURI.length() == 0) {
                        if (elemPrefix == null || elemPrefix.length() == 0) {
                            newElem = f.element(ln);
                        } else {
                            /* Happens when a prefix is bound to the default
                             * (empty) namespace...
                             */
                            newElem = f.element(ln, elemPrefix, "");
                        }
                    } else {
                        newElem = f.element(ln, elemPrefix, nsURI);
                    }

                    /* Let's add element right away (probably have to do
                     * it to bind attribute namespaces, too)
                     */
                    if (current == null) { // at root
                        doc.setRootElement(newElem);
                    } else {
                        f.addContent(current, newElem);
                    }

                    // Any declared namespaces?
                    for (int i = 0, len = r.getNamespaceCount(); i < len; ++i) {
                        String prefix = r.getNamespacePrefix(i);
                        if (prefix == null) {
                            prefix = "";
                        }
                        Namespace ns = Namespace.getNamespace(prefix, r.getNamespaceURI(i));

                        // JDOM has special handling for element's "own" ns:
                        if (prefix.equals(elemPrefix)) {
                            ; // already set by when it was constructed...
                        } else {
                            f.addNamespaceDeclaration(newElem, ns);
                        }
                    }

                    // And then the attributes:
                    for (int i = 0, len = r.getAttributeCount(); i < len; ++i) {
                        String prefix = r.getAttributePrefix(i);
                        Namespace ns;

                        if (prefix == null || prefix.length() == 0) {
                            // Attribute not in any namespace
                            ns = Namespace.NO_NAMESPACE;
                        } else {
                            ns = newElem.getNamespace(prefix);
                        }
                        Attribute attr = f.attribute(r.getAttributeLocalName(i),
                                r.getAttributeValue(i),
                                resolveAttrType(r.getAttributeType(i)),
                                ns);
                        f.setAttribute(newElem, attr);
                    }
                    // And then 'push' new element...
                    current = newElem;
                }

                if (tmod != null) {
                    allowTextMods = tmod.allowModificationsAfter(r, evtType);
                }

                // Already added the element, can continue
                continue main_loop;

                case XMLStreamConstants.START_DOCUMENT:
                    /* This should only be received at the beginning of document...
                    * so, should we indicate the problem or not?
                    */
                    /* For now, let it pass: maybe some (broken) readers pass
                    * that info as first event in beginning of doc?
                    */
                    continue main_loop;

                case XMLStreamConstants.DTD:
                    /* !!! Note: StAX does not expose enough information about
                    *  doctype declaration (specifically, public and system id!);
                    *  should (re-)parse information... not yet implemented
                    */
                    // TBI
                    continue main_loop;

                    // Should never get these, from a stream reader:

                    /* (commented out entries are just FYI; default catches
                    * them all)
                    */

                    //case XMLStreamConstants.ATTRIBUTE:
                    //case XMLStreamConstants.NAMESPACE:
                default:
                    throw new XMLStreamException("Unrecognized iterator event type: " + r.getEventType() + "; should not receive such types (broken stream reader?)");
            }

            if (child != null) {
                if (current == null) {
                    f.addContent(doc, child);
                } else {
                    f.addContent(current, child);
                }
            }
        }
    }

    /**
     * Method called when option is turned on;
     * to determine if current CHARACTERS event looks like it might
     * be used for indentation purposes (it is all white space, and
     * is either immediately after a start element, or could be
     * immediately before a start element).
     * <p>
     * The default implementation just checks whether the text segment
     * (known to be all white space) starts with a
     * linefeed character.
     */
    protected boolean isIndentationWhitespace(XMLStreamReader r)
            throws XMLStreamException {
        String text = r.getText();
        // Should never be empty... but let's be sure
        if (text.length() > 0) {
            char c = text.charAt(0);
            return (c == '\n' || c == '\r');
        }
        return false;
    }

    // // // Private methods:

    private static int resolveAttrType(String typeStr) {
        if (typeStr != null && typeStr.length() > 0) {
            Integer I = attrTypes.get(typeStr);
            if (I != null) {
                return I.intValue();
            }
        }
        return Attribute.UNDECLARED_TYPE;
    }

    // // // Basic text modifier class(es)

    public static class IndentRemover
            extends StAXTextModifier {
        final static IndentRemover sInstance = new IndentRemover();

        protected IndentRemover() {
            super();
        }

        public static IndentRemover getInstance() {
            return sInstance;
        }

        /**
         * Always removes indentation after
         * all start and elements without any further checks; essentially
         * allowing (indentation) white space removal anywhere in the
         * document.
         */
        public boolean allowModificationsAfter(XMLStreamReader r, int eventType)
                throws XMLStreamException {
            return true;
        }

        /**
         * Enables modifications for
         * so-called "indentation
         * white space", ie. all-whitespace (non-CDATA) text segment that
         * starts with
         * a linefeed character (\n or \r); provided it follows a non-text
         * event (anything other than CDATA, ENTITY_REFERENCE and CHARACTERS;
         * none of which usually should be adjacent to CHARACTERS event,
         * if text coalescing is enabled, and automatic entity expansion
         * is not disabled).
         */
        public boolean possiblyModifyText(XMLStreamReader r, int prevEvent)
                throws XMLStreamException {
            if (r.getEventType() == XMLStreamConstants.CHARACTERS) {
                if (!(prevEvent == XMLStreamConstants.CHARACTERS
                        || prevEvent == XMLStreamConstants.CDATA
                        || prevEvent == XMLStreamConstants.ENTITY_REFERENCE)) {
                    if (r.isWhiteSpace()) {
                        String txt = r.getText();
                        if (txt.length() > 0) { // should always be true
                            char c = txt.charAt(0);
                            return (c == '\n' || c == '\r');
                        }
                    }
                }
            }
            return false;
        }

        /**
         * If we ever get this far, we will still check that
         * the CHARACTERS event is not immediately followed by another
         * textual event. If so, we'll just remove the (all white space)
         * text event.
         */
        public String textToIncludeBetween(XMLStreamReader r,
                                           int prevEvent, int nextEvent,
                                           String text)
                throws XMLStreamException {
            /* Only remove white space if neither preceding nor following
             * event is of non-ignorable textual type (CHARACTERS, CDATA,
             * ENTITY_REFERENCE; note that SPACE should never be adjacent
             * to CHARACTERS event).
             */
            if (nextEvent == XMLStreamConstants.CHARACTERS
                    || nextEvent == XMLStreamConstants.CDATA
                    || nextEvent == XMLStreamConstants.ENTITY_REFERENCE) {
                return text;
            }

            /* If we got this far, we know it's indentation white space
            * and should just be removed completely:
            */
            return null;
        }
    }

    // // // Testing

    /**
     * Trivial test driver for testing functionality.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ... [file]");
            System.exit(1);
        }
        String filename = args[0];
        java.io.Reader r = new java.io.FileReader(filename);
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newInstance();
        XMLStreamReader sr = f.createXMLStreamReader(r);

        StAXBuilder builder = new StAXBuilder();

        Document domDoc = builder.build(sr);
        System.out.println("Done [with " + sr.getClass() + "]:");
        System.out.println("----- JDom -----");
        org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter();
        java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
        outputter.output(domDoc, pw);
        pw.flush();
        System.out.println("----- /JDom -----");
    }
}