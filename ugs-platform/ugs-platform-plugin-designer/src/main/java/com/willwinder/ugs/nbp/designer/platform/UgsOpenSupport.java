package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.DesignerTopComponent;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataNode;
import org.openide.loaders.OpenSupport;
import org.openide.nodes.Children;
import org.openide.text.Line;
import org.openide.util.Task;
import org.openide.windows.CloneableTopComponent;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.io.IOException;

public class UgsOpenSupport extends OpenSupport implements OpenCookie, CloseCookie, EditorCookie {

    public UgsOpenSupport(UgsDataObject.Entry entry) {
        super(entry);
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        UgsDataObject dobj = (UgsDataObject) entry.getDataObject();
        DesignerTopComponent tc = new DesignerTopComponent();
        tc.setActivatedNodes(new DataNode[]{new DataNode(dobj, Children.LEAF)});
        tc.setDisplayName(dobj.getName());
        return tc;
    }

    @Override
    public Task prepareDocument() {
        return null;
    }

    @Override
    public StyledDocument openDocument() throws IOException {
        return null;
    }

    @Override
    public StyledDocument getDocument() {
        return null;
    }

    @Override
    public void saveDocument() throws IOException {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public JEditorPane[] getOpenedPanes() {
        return new JEditorPane[0];
    }

    @Override
    public Line.Set getLineSet() {
        return null;
    }
}
