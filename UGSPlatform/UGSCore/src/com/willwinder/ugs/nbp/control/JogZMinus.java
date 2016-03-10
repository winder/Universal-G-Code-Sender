/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.control;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author wwinder
 */
@ActionID(
        category = "Machine",
        id = "com.willwinder.ugs.nbp.control.JogZMinus"
)
@ActionRegistration(
        displayName = "seeConstructor",
        lazy = false
)
@ActionReference(path = "Menu/Machine", position = 1300)
public class JogZMinus extends AbstractAction implements ContextAwareAction {
    JogService jogService;

    public JogZMinus() {
        jogService = Lookup.getDefault().lookup(JogService.class);
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(JogService.class, "JogService.zMinus")); // NOI18N
    }
    
    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new JogZMinus();
    }
    
    @Override
    public boolean isEnabled() {
        return jogService.canJog();
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        jogService.adjustManualLocation(0, 0, -1);
    }
}
