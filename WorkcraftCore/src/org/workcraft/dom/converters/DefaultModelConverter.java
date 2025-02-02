package org.workcraft.dom.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.utils.Hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DefaultModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel> extends AbstractModelConverter<TSrcModel, TDstModel> {

    public DefaultModelConverter(TSrcModel srcModel, TDstModel dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = new HashMap<>();
        result.put(CommentNode.class, CommentNode.class);
        return result;
    }

    @Override
    public Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap() {
        return new HashMap<Class<? extends VisualReplica>, Class<? extends VisualReplica>>();
    }

    @Override
    public String convertNodeName(String srcName, Container container) {
        return srcName;
    }

    @Override
    public void preprocessing() {
    }

    @Override
    public void postprocessing() {
    }

    @Override
    public VisualPage convertPage(VisualPage srcPage) {
        VisualPage dstPage = null;
        String ref = getSrcModel().getMathReference(srcPage);
        Container dstContainer = getRefToDstPage(ref);
        if (dstContainer instanceof VisualPage) {
            dstPage = (VisualPage) dstContainer;
            dstPage.copyPosition(srcPage);
            dstPage.copyStyle(srcPage);
        }
        return dstPage;
    }

    @Override
    public VisualComponent convertComponent(VisualComponent srcComponent) {
        VisualComponent dstComponent = null;
        String srcRef = getSrcModel().getMathReference(srcComponent);
        if (srcRef != null) {
            Map<Class<? extends MathNode>, Class<? extends MathNode>> componentClassMap = getComponentClassMap();
            Class<? extends MathNode> dstMathNodeClass = componentClassMap.get(srcComponent.getReferencedComponent().getClass());
            if (dstMathNodeClass != null) {
                Class<? extends VisualComponent> dstVisualComponentClass = getVisualComponentClass(dstMathNodeClass);
                if (dstVisualComponentClass != null) {
                    String path = NamespaceHelper.getParentReference(srcRef);
                    Container container = getRefToDstPage(path);
                    String srcName = NamespaceHelper.getReferenceName(srcRef);
                    String dstName = convertNodeName(srcName, container);
                    Container mathContainer = NamespaceHelper.getMathContainer(getDstModel(), container);
                    MathModel dstMathModel = getDstModel().getMathModel();
                    MathNode dstMathNode = dstMathModel.createNode(dstName, mathContainer, dstMathNodeClass);

                    dstComponent = getDstModel().createVisualComponent(dstMathNode, dstVisualComponentClass, container);
                    dstComponent.copyPosition(srcComponent);
                    dstComponent.copyStyle(srcComponent);
                }
            }
        }
        return dstComponent;
    }

    @Override
    public VisualReplica convertReplica(VisualReplica srcReplica) {
        VisualReplica dstReplica = null;
        Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> replicaClassMap = getReplicaClassMap();
        Class<? extends VisualReplica> dstVisualReplicaClass = replicaClassMap.get(srcReplica.getClass());
        if (dstVisualReplicaClass != null) {
            Container dstContainer = null;
            Node srcParent = srcReplica.getParent();
            VisualComponent srcMasterComponent = srcReplica.getMaster();
            if ((srcParent instanceof Container) && (srcParent instanceof VisualNode)) {
                VisualNode dstParent = getSrcToDstNode((VisualNode) srcParent);
                if (dstParent instanceof Container) {
                    dstContainer = (Container) dstParent;
                }
            }

            VisualComponent dstMasterComponent = null;
            VisualNode dstMasterNode = getSrcToDstNode(srcMasterComponent);
            if (dstMasterNode instanceof VisualComponent) {
                dstMasterComponent = (VisualComponent) dstMasterNode;
            }

            dstReplica = getDstModel().createVisualReplica(dstMasterComponent, dstVisualReplicaClass, dstContainer);
            dstReplica.copyPosition(srcReplica);
            dstReplica.copyStyle(srcReplica);
        }
        return dstReplica;
    }

    @Override
    public VisualConnection convertConnection(VisualConnection srcConnection) {
        VisualNode srcFirst = srcConnection.getFirst();
        VisualNode srcSecond = srcConnection.getSecond();
        VisualNode dstFirst = getSrcToDstNode(srcFirst);
        VisualNode dstSecond = getSrcToDstNode(srcSecond);
        VisualConnection dstConnection = null;
        if ((dstFirst != null) && (dstSecond != null)) {
            try {
                dstConnection = getDstModel().connect(dstFirst, dstSecond);
                dstConnection.copyStyle(srcConnection);
                dstConnection.copyShape(srcConnection);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        return dstConnection;
    }

    @Override
    public VisualGroup convertGroup(VisualGroup srcGroup) {
        VisualGroup dstGroup = null;
        HashSet<VisualNode> dstSelection = new HashSet<>();
        for (Node srcNode: srcGroup.getChildren()) {
            VisualNode dstNode = null;
            if (srcNode instanceof VisualNode) {
                dstNode = getSrcToDstNode((VisualNode) srcNode);
            }
            if (dstNode != null) {
                dstSelection.add(dstNode);
            }
        }
        if (!dstSelection.isEmpty()) {
            Container c = Hierarchy.getNearestContainer(dstSelection);
            getDstModel().setCurrentLevel(c);
            getDstModel().addToSelection(dstSelection);
            dstGroup = getDstModel().groupSelection();
            getDstModel().selectNone();
            dstGroup.copyPosition(srcGroup);
            dstGroup.copyStyle(srcGroup);
        }
        return dstGroup;

    }

}
