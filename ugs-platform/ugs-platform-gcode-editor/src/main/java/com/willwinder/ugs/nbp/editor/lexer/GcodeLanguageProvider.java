/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.ugs.nbp.editor.lexer;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * A language provider for registering gcode language hierarcy
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service=LanguageProvider.class)
public class GcodeLanguageProvider extends LanguageProvider {

  @Override
  public Language<?> findLanguage(String mimeType) {
    if (GcodeLanguageConfig.MIME_TYPE.equals(mimeType)) {
      return new GcodeLanguageHierarcy().language();
    }
    return null;
  }

  @Override
  public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token, LanguagePath lp, InputAttributes ia) {
    return null;
  }
}