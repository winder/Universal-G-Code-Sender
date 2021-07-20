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
import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public class PatternRemover implements CommandProcessor {
    final private Pattern p;
    // r[0] is "" or 'replace with' string from regexpattern supplied
    final private List<String> r = new ArrayList<String>();
    //private String r = "";
     
    public PatternRemover(String regexPattern) {
        /* AndyCXL enhancing 'remover' into 'remover or replacer' by
        *  using sed or grep interpretation of the regexPattern.
        *  If the pattern is like " s/grep-pattern/any-text " then match using
        *  grep with the split-out grep-pattern, and replace the return string using
        *  the any-text even if it is empty. 
        *  If the pattern is anything else, ie: not starting with " s/ " then
        *  intepret the whole pattern as grep and force replace the return string
        *  with the empty string ""
        */
        String[] s3 = regexPattern.split("/", 3);
        
        if (s3[0].trim().equals("s")) {
            // sed pattern identified, second split is therefore grep pattern
            p = Pattern.compile(s3[1].trim());
            // Third split is the 'replace with' string, if it exists. Lazy pattern
            // entry by user might omit the final / and string in the sed syntax
            if (s3.length == 3) {
                r.add(s3[2].trim());
                //r = s3[2];
            } else {
                r.add("");
                //r = "";
            }
        } else {
            // grep pattern received, presume entire pattern is the regexPattern
            p = Pattern.compile(regexPattern);
            r.add("");
            //r = "";
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
        /* Property p contains the grep string in either case of grep or sed
        *  syntax given or derived, so p.matcher() is correct in both cases.
        *  In processing the regexPattern, sed (r) is populated correctly for
        *  either case too, so simply replace matches to regex with sed
        */
        //ret.add(p.matcher(command).replaceAll(""));
        ret.add( p.matcher(command).replaceAll( r.get(0) ) );
        //System.out.println( "Command sed:" + command + "->-" + r.get(0) );
        return ret;
    }
}
