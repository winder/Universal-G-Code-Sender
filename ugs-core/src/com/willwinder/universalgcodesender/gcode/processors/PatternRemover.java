/**
 * Removes a specified regex pattern from the command.
 */
/*
    Copyright 2016 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
//
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
//
/**
 *
 * @author wwinder, AndyCXL
 *
 */
public class PatternRemover implements CommandProcessor {
    final private Pattern p;
    final private List<String> r = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(PatternRemover.class.getName());
    //
    public PatternRemover(String regexPattern) {
        // AndyCXL enhancing 'remover' into 'remover or replacer' with sed
        String[] s3 = regexPattern.split("/", 3);
        if (s3[0].trim().equals("s")) {
            // sed pattern identified, second split is therefore grep pattern
            p = Pattern.compile(s3[1].trim());
            // Third split is the 'replace with' string, use "" if absent
            if (s3.length == 3) {
                // Does s3[2] contain a known macro name (one)? Expand if so
                Pattern pm = Pattern.compile("%.+%");
                Matcher mp = pm.matcher(s3[2].trim());
                // Retrieve and match macros, expand macro.gcode() if defined
                if (mp.matches()) {
                    int expanded = 0;
                    // TODO:
                    // Get the backend, through which macros are retrieved
                    // backend = CentralLookup.getDefault().lookup(BackendAPI.class);
                    List<Macro> macros = SettingsFactory.loadSettings().getMacros();
                    // Enumerate macros to find match
                    for (Macro macro: macros) {
                        // Iterate macros and test given name amongst macro names
                        if (mp.group().equals("%"+macro.getName()+"%")) {
                            s3[2] = s3[2].replace(mp.group(), macro.getGcode());
                            expanded = 1;
                            break;
                        }
                    }
                    // :TODO //
                    // If there was no macro matched safely degrade s3[2] into ""
                    if (expanded == 0) {
                        s3[2] = "";
                    }
                }
                // Any macros are expanded, string is ready to submit
                r.add(s3[2].trim());
            } else {
                r.add("");
            }
        } else {
            // grep pattern received, presume entire pattern is the regexPattern
            p = Pattern.compile(regexPattern);
            r.add("");
        }
    }
    
    @Override
    public String getHelp() {
        return Localization.getString("sender.help.patternRemover")
                + ": \"" + p.pattern() + "/" + r.get(0) + "\"";
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) {
        List<String> ret = new ArrayList<>();
        // Property p contains the grep string in either case of grep or sed
        ret.add( p.matcher(command).replaceAll( r.get(0) ) );
        if (!command.equals(ret.get(0))) {
            logger.log(Level.INFO, "Replacer: "+command+" to: "+ret.get(0));
        }
        return ret;
    }
}
