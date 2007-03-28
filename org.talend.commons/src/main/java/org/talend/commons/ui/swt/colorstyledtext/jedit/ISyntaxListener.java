// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.commons.ui.swt.colorstyledtext.jedit;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public interface ISyntaxListener {

    void newRules(String name, boolean highlightDigits, boolean ignoreCase, String digitRE, char escape, String defaultTokenType);

    void newEOLSpan(String type, String text);

    void newSpan(String type, String begin, String end, boolean atLineStart, boolean excludeMatch, boolean noLineBreak,
            boolean noWordBreak, String delegate);

    void newKeywords(KeywordMap keywords);

    void newTextSequence(String type, String text, boolean atLineStart, boolean atWhitespaceEnd, boolean atWordStart, String delegate);

    void newMark(String type, String text, boolean atLineStart, boolean atWhitespaceEnd, boolean atWordStart, String delegate,
            boolean isPrevious, boolean excludeMatch);
}
