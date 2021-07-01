package com.willwinder.ugs.nbp.lib;

import org.apache.commons.lang3.ArrayUtils;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditorUtils {
    /**
     * Close all open editors
     *
     * @return true if all editors could be closed
     */
    public static boolean closeOpenEditors() {
        List<TopComponent> editorCookies = TopComponent.getRegistry().getOpened()
                .stream()
                .filter(topComponent ->
                        Arrays.stream(ArrayUtils.nullToEmpty(topComponent.getActivatedNodes(), Node[].class))
                                .anyMatch(node -> node.getCookie(EditorCookie.class) != null))
                .collect(Collectors.toList());

        boolean closed = true;
        for(TopComponent editorCookie : editorCookies) {
            if (!editorCookie.close()) {
                closed = false;
            }
        }
        return closed;
    }
}
