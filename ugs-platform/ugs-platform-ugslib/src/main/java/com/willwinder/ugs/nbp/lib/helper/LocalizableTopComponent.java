/*
 * 
 */
package com.willwinder.ugs.nbp.lib.helper;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author wwinder
 */
public abstract class LocalizableTopComponent extends TopComponent {
    
    protected LocalizableTopComponent(
            String title, String tooltip,
            String category, String actionId,
            String menuPath, String localizedMenuPath) {

        setName(title);
        setToolTipText(tooltip);
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);
        ars.overrideActionName(category, actionId, title);
        
        if (menuPath != null && localizedMenuPath != null) {
            try {
                ars.createAndLocalizeFullMenu(menuPath, localizedMenuPath);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
