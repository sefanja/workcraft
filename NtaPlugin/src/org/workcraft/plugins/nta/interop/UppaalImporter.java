package org.workcraft.plugins.nta.interop;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.nta.*;
import org.workcraft.plugins.nta.uppaal.*;
import org.workcraft.plugins.nta.uppaal.Location;
import org.workcraft.plugins.nta.uppaal.Nta;
import org.workcraft.plugins.nta.uppaal.Template;
import org.workcraft.plugins.nta.uppaal.Transition;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UppaalImporter implements Importer {

    private static final double SCALE = 20.0;
    private static final double MARGIN = 2.0;

    @Override
    public UppaalFormat getFormat() {
        return UppaalFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new NtaDescriptor(), importUppaalSystem(in));
    }

    public VisualNta importUppaalSystem(InputStream in) throws DeserialisationException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Nta.class);

            // using our own XML reader to prevent the external DTD from being accessed
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            InputSource inputSource = new InputSource(in);
            SAXSource saxSource = new SAXSource(xmlReader, inputSource);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Nta uNta = (Nta) jaxbUnmarshaller.unmarshal(saxSource);

            VisualNta vNta = importNta(uNta);
            distributeVisualComponents(vNta);
            return vNta;
        } catch (JAXBException | ParserConfigurationException | SAXException e) {
            throw new DeserialisationException(e);
        }
    }

    /**
     * Imports `/nta`
     */
    private VisualNta importNta(Nta uNta) throws DeserialisationException {
        VisualNta vNta = new VisualNta(new org.workcraft.plugins.nta.Nta());

        // we don't import the `imports` element since we don't know what that should represent

        importDeclaration(vNta, uNta);
        importInstantiation(vNta, uNta);
        importSystem(vNta, uNta);
        importTemplates(vNta, uNta);

        return vNta;
    }

    /**
     * Imports `/nta/declaration`
     */
    private void importDeclaration(VisualNta vNta, Nta uNta) {
        addVisualTextNote(
                vNta,
                TextNote.Type.DECLARATION,
                uNta.getDeclaration(),
                vNta.getRoot(),
                vNta.getReferencedNta().getRoot());
    }

    /**
     * Imports `/nta/instantiation`
     */
    private void importInstantiation(VisualNta vNta, Nta uNta) {
        addVisualTextNote(
                vNta,
                TextNote.Type.INSTANTIATION,
                uNta.getInstantiation(),
                vNta.getRoot(),
                vNta.getReferencedNta().getRoot());
    }

    /**
     * Imports `/nta/system`
     */
    private void importSystem(VisualNta vNta, Nta uNta) {
        addVisualTextNote(
                vNta,
                TextNote.Type.SYSTEM,
                uNta.getSystem(),
                vNta.getRoot(),
                vNta.getReferencedNta().getRoot());
    }

    /**
     * Imports `/nta/template`
     */
    private void importTemplates(VisualNta vNta, Nta uNta) throws DeserialisationException {
        for (Template uTemplate : uNta.getTemplate()) {
            // the x and y attributes of the name element are ignored since Workcraft uses standard name positions
            String name = uTemplate.getName().getValue();
            VisualTemplate vTemplate = addVisualTemplate(vNta, name);
            org.workcraft.plugins.nta.Template mTemplate = vTemplate.getReferencedTemplate();

            importDeclaration(vNta, uTemplate, vTemplate);

            Parameter parameter = uTemplate.getParameter();
            mTemplate.setParameters(parameter != null ? parameter.getValue() : null);

            Map<Location, VisualLocation> locationMap = importLocations(vNta, uTemplate, vTemplate);

            importInit(locationMap, uTemplate);

            importTransitions(vNta, locationMap, uTemplate);
        }
    }

    /**
     * Imports `/nta/template/declaration`
     */
    private void importDeclaration(VisualNta vNta, Template uTemplate, VisualTemplate vTemplate) {
        addVisualTextNote(
                vNta,
                TextNote.Type.DECLARATION,
                uTemplate.getDeclaration(),
                vTemplate,
                vTemplate.getReferencedTemplate());
    }

    /**
     * Imports `/nta/template/location`
     */
    private Map<Location, VisualLocation> importLocations(
            VisualNta vNta, Template uTemplate, VisualTemplate vTemplate) {
        org.workcraft.plugins.nta.Template mTemplate = vTemplate.getReferencedTemplate();

        Map<Location, VisualLocation> locationMap = new HashMap<>();

        for (Location uLocation : uTemplate.getLocation()) {
            // the x and y attributes of the name element are ignored since Workcraft uses standard name positions
            String name = uLocation.getName() != null ? uLocation.getName().getValue() : null;
            org.workcraft.plugins.nta.Location mLocation =
                    vNta.getMathModel().createNode(name, mTemplate, org.workcraft.plugins.nta.Location.class);
            VisualLocation vLocation = vNta.createVisualComponent(mLocation, VisualLocation.class, vTemplate);
            locationMap.put(uLocation, vLocation);

            vLocation.setPosition(new Point2D.Double(uLocation.getX() / SCALE, uLocation.getY() / SCALE));

            if (uLocation.getCommitted() != null) {
                mLocation.setCommitted(true);
            }

            if (uLocation.getUrgent() != null) {
                mLocation.setUrgent(true);
            }

            for (Label uLabel : uLocation.getLabel()) {
                String value = uLabel.getValue();
                switch (uLabel.getKind()) {
                case "comments":
                    mLocation.setComments(value);
                    break;
                case "invariant":
                    mLocation.setInvariant(value);
                    break;
                }
            }
        }

        return locationMap;
    }

    /**
     * Imports `/nta/template/init`
     */
    private void importInit(Map<Location, VisualLocation> locationMap, Template uTemplate) {
        Init uInit = uTemplate.getInit();
        if (uInit != null) {
            Location uInitLocation = (Location) uInit.getRef();
            VisualLocation vLocation = locationMap.get(uInitLocation);
            vLocation.getReferencedLocation().setInitial(true);
        }
    }

    /**
     * Imports `/nta/template/transition`
     */
    private void importTransitions(VisualNta vNta, Map<Location, VisualLocation> locationMap, Template uTemplate)
            throws DeserialisationException {
        for (Transition uTransition : uTemplate.getTransition()) {
            Location uSourceLocation = (Location) uTransition.getSource().getRef();
            Location uTargetLocation = (Location) uTransition.getTarget().getRef();
            try {
                // the x and y attributes are ignored since their meaning is unclear
                VisualTransition vTransition = (VisualTransition) vNta.connect(
                        locationMap.get(uSourceLocation),
                        locationMap.get(uTargetLocation)
                );
                org.workcraft.plugins.nta.Transition mTransition =
                        (org.workcraft.plugins.nta.Transition) vTransition.getReferencedConnection();

                for (Label uLabel : uTransition.getLabel()) {
                    String value = uLabel.getValue();
                    switch (uLabel.getKind()) {
                    case "assignment":
                        mTransition.setAssignments(value);
                        break;
                    case "comments":
                        mTransition.setComments(value);
                        break;
                    case "guard":
                        mTransition.setGuard(value);
                        break;
                    case "select":
                        mTransition.setSelects(value);
                        break;
                    case "synchronisation":
                        mTransition.setSynchronisation(value);
                        break;
                    }
                }

                ConnectionGraphic graphic = vTransition.getGraphic();
                if (graphic instanceof Polyline) {
                    for (Nail uNail : uTransition.getNail()) {
                        ((Polyline) graphic).addControlPoint(new Point2D.Double(
                                uNail.getX() / SCALE,
                                uNail.getY() / SCALE
                        ));
                    }
                }
            } catch (InvalidConnectionException e) {
                throw new DeserialisationException(e);
            }
        }

    }

    private void addVisualTextNote(
            VisualNta vNta, TextNote.Type type, String text, Container vContainer, Container mContainer) {
        if (text != null && !text.isEmpty()) {
            TextNote mTextNote = vNta.getMathModel().createNode(null, mContainer, TextNote.class);
            mTextNote.setType(type);
            mTextNote.setText(replaceNewlines(text));
            vNta.createVisualComponent(mTextNote, VisualTextNote.class, vContainer);
        }
    }

    private String replaceNewlines(String uppaalString) {
        if (uppaalString == null) {
            return null;
        }
        return uppaalString.replace(
                '\n', // UPPAAL's newline character
                '|' // Workcraft's newline character
        );
    }

    private VisualTemplate addVisualTemplate(VisualNta vNta, String name) {
        Container mRoot = vNta.getMathModel().getRoot();
        org.workcraft.plugins.nta.Template mTemplate = new org.workcraft.plugins.nta.Template();
        mRoot.add(mTemplate);
        Container vRoot = vNta.getRoot();
        VisualTemplate vTemplate = new VisualTemplate(mTemplate);
        vRoot.add(vTemplate);
        vNta.setMathName(vTemplate, name);
        return vTemplate;
    }

    /**
     * Distributes visual components.
     */
    private void distributeVisualComponents(VisualNta vNta) {
        for (VisualTextNote vTextNote : Hierarchy.getDescendantsOfType(vNta.getRoot(), VisualTextNote.class)) {
            vTextNote.cacheLabelRenderedText(); // needed to better estimate the bounding box
        }

        // distribute VisualTextNotes
        double vOffset = 0.0;
        double maxWidth = 0.0;
        for (VisualTextNote vTextNote : Hierarchy.getChildrenOfType(vNta.getRoot(), VisualTextNote.class)) {
            Point2D pos = vTextNote.getPosition();
            vTextNote.setPosition(new Point2D.Double(pos.getX(), pos.getY() + vOffset));
            Rectangle2D bb = vTextNote.getBoundingBox();
            vOffset += bb.getHeight() + MARGIN;
            maxWidth = Math.max(maxWidth, bb.getWidth());
        }

        // distribute VisualTemplates
        double hOffset = maxWidth + MARGIN;
        for (VisualTemplate vTemplate: vNta.getVisualTemplates()) {
            Point2D pos = vTemplate.getPosition();
            vTemplate.setPosition(new Point2D.Double(pos.getX() + hOffset, pos.getY()));
            hOffset += vTemplate.getBoundingBox().getWidth() + MARGIN;
        }
    }

}
