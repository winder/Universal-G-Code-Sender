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
        // Check if 'remover or replacer' by detecting sed syntax
        String[] s3 = regexPattern.split("/", 3);
        switch(s3.length) {
            case 2:
                // Replacer s/regex match to p, blank r
                p = Pattern.compile(s3[1].trim());
                r.add("");
                break;
            case 3:
                // Full s/regex/replace match, r as-is or with macro expansion
                Pattern pm = Pattern.compile("%.+%");
                Matcher mp = pm.matcher(s3[2].trim());
                if (mp.matches()) {
                    // Get the user's macros for match searching
                    List<Macro> macros = SettingsFactory.loadSettings().getMacros();
                    for (Macro macro: macros) {
                        if (mp.group().equals("%"+macro.getName()+"%")) {
                            s3[2] = s3[2].replace(mp.group(), macro.getGcode());
                            break;
                        }
                    }
                }
                // s3[2] contains macro expansion or the orig string if no matches
                p = Pattern.compile(s3[1].trim());
                if (s3[2].contains("%")) {
                    r.add("");
                } else {
                    r.add(s3[2].trim());
                }
                break;
            default:
                // No sed, or mal-formed, so Simple regex to p, blank r
                p = Pattern.compile(regexPattern);
                r.add("");
                break;
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
            logger.log(Level.INFO, "Replacer: \""+command+"\" to: \""+ret.get(0)+"\"");
        }
        return ret;
    }
}
