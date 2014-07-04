// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.eclipse.emf.common.util.EList;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;

/**
 * cli class global comment. Detailled comment
 */
public final class ParameterValueUtil {

    private ParameterValueUtil() {
    }

    @SuppressWarnings("unchecked")
    public static void renameValues(final IElementParameter param, final String oldName, final String newName) {
        if (param == null || oldName == null || newName == null) {
            return;
        }
        boolean flag = true;
        if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
            flag = false;
        }
        if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
            String value = (String) param.getValue();
            if (value.contains(oldName)) {
                // param.setValue(value.replaceAll(oldName, newName));
                String newValue = renameValues(value, oldName, newName, flag);
                if (!value.equals(newValue)) {
                    param.setValue(newValue);
                }
            }
        } else if (param.getValue() instanceof List) { // for TABLE
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
            for (Map<String, Object> line : tableValues) {
                for (String key : line.keySet()) {
                    Object cellValue = line.get(key);
                    if (cellValue instanceof String) { // cell is text so
                        // rename data if
                        // needed
                        String value = (String) cellValue;
                        if (value.contains(oldName)) {
                            // line.put(key, value.replaceAll(oldName,
                            // newName));
                            String newValue = renameValues(value, oldName, newName, flag);
                            if (!value.equals(newValue)) {
                                line.put(key, newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    public static String renameValues(final String value, final String oldName, final String newName, boolean flag) {
        if (value == null || oldName == null || newName == null) {
            return value; // keep original value
        }
        PatternCompiler compiler = new Perl5Compiler();
        Perl5Matcher matcher = new Perl5Matcher();
        matcher.setMultiline(true);
        Perl5Substitution substitution = new Perl5Substitution(newName + "$2", Perl5Substitution.INTERPOLATE_ALL); //$NON-NLS-1$

        Pattern pattern;
        try {
            pattern = compiler.compile(getQuotePattern(oldName));
        } catch (MalformedPatternException e) {
            return value; // keep original value
        }

        if (matcher.contains(value, pattern)) {
            // replace
            String returnValue = "";
            if (value.contains(TalendQuoteUtils.getQuoteChar()) && !flag) {
                // returnValue = splitQueryData(matcher, pattern, substitution, value, Util.SUBSTITUTE_ALL);
                returnValue = splitQueryData(oldName, newName, value);
            } else {
                returnValue = Util.substitute(matcher, pattern, substitution, value, Util.SUBSTITUTE_ALL);
            }
            return returnValue;

        }
        return value; // keep original value

    }

    // function before TDI-29092 modify,this function seems only rename variables in context,I put this funciton back
    // incase any problem with the new function and we can refer the old one to check the problem.
    public static String splitQueryData(PatternMatcher matcher, Pattern pattern, Substitution sub, String value, int numSubs) {
        String[] split = value.split("\"");
        int i = 0;
        String replace = "";
        for (String s : split) {
            if (i % 2 == 0) {
                replace = s;
                if (i != 0) {
                    if (matcher.contains(value, pattern)) {
                        replace = split[i].toString();
                        split[i] = Util.substitute(matcher, pattern, sub, replace, numSubs);
                    }
                }
            }
            i++;
        }
        String returnValue = "";
        for (int t = 1; t < split.length; t++) {
            if (t % 2 == 0) {
                returnValue += split[t];
            } else {
                returnValue += "\"" + split[t] + "\"";
            }
        }
        return returnValue;
    }

    // for bug 12594 split "; for bug 29092(it has JUnits)
    public static String splitQueryData(String oldName, String newName, String value) {
        // example:"drop table "+context.oracle_schema+".\"TDI_26803\""
        // >>>>>>>>_△_(const)__ _____△_(varible)_______ __△_(const)___

        final int length = value.length();
        // quotaStrings which stores the start and end point for all const strings in the value
        LinkedHashMap<Integer, Integer> quotaStrings = new LinkedHashMap<Integer, Integer>();

        // get and store all start and end point of const strings
        int start = -1;
        int end = -2;
        char ch;
        for (int i = 0; i < length; i++) {
            ch = value.charAt(i);
            if (ch == '\"') {
                // in case of cases :
                // case 1 : [ "select * from " + context.table + " where value = \"context.table\"" ]
                // case 2 : [ "select * from " + context.table + " where value = \"\\" + context.table +
                // "\\context.table\"" ]
                if (0 < i) {
                    boolean isEscapeSequence = false;
                    for (int index = i; 0 < index; index--) {
                        if (value.charAt(index - 1) == '\\') {
                            isEscapeSequence = !isEscapeSequence;
                        } else {
                            break;
                        }
                    }
                    if (isEscapeSequence) {
                        continue;
                    }
                }

                // [0 <= start] >> in case the first const String position compute error
                if (0 <= start && end < start) {
                    end = i;
                    quotaStrings.put(start, end);
                } else {
                    start = i;
                }
            }
        }

        {
            // in case the value has not complete quota
            // exapmle > "select a,context.b from " + context.b + "where value = context.b

            // **but** maybe more impossible truth is that
            // they write this(context.b) just want to use it as a varible...
            // so maybe should not set the string behind the quota as a const by default..

            // ---▽--- the following code is set the string behind the quota as a const

            // if (0 <= start && end < start) {
            // end = length - 1;
            // quotaStrings.put(start, end);
            // }
        }

        // find the varible string, do replace, then concat them
        StringBuffer strBuffer = new StringBuffer();
        String subString = null;
        int vStart = 0;
        int vEnd = 0;
        start = 0;
        end = 0;
        for (Entry<Integer, Integer> entry : quotaStrings.entrySet()) {
            start = entry.getKey();
            end = entry.getValue() + 1;
            if (start == 0) {
                // the value begin with const string
                subString = value.substring(start, end);
            } else {
                vEnd = start;
                if (vStart == start) {
                    // const string follow with const string, maybe won't happen...
                    // get the const string
                    subString = value.substring(start, end);
                } else {
                    // get the varible string, do replace, then append it
                    subString = value.substring(vStart, vEnd);
                    strBuffer.append(subString.replaceAll("\\b" + oldName + "\\b", newName)); //$NON-NLS-1$//$NON-NLS-2$
                    // get the const string
                    subString = value.substring(start, end);
                }
            }
            // append the const string
            strBuffer.append(subString);
            // update the varible string start point
            vStart = end;
        }

        // in case the last string of the value is a const string
        // then get it, and do replace, finally append it.
        if (vStart < length) {
            vEnd = length;
            subString = value.substring(vStart, vEnd);
            strBuffer.append(subString.replaceAll("\\b" + oldName + "\\b", newName)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return strBuffer.toString();
    }

    public static boolean isUseData(final IElementParameter param, final String name) {
        if (param == null || name == null) {
            return false;
        }
        if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
            String value = (String) param.getValue();
            if (ParameterValueUtil.valueContains(value, name)) {
                return true;
            }
        } else if (param.getValue() instanceof List) { // for TABLE
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
            for (Map<String, Object> line : tableValues) {
                for (String key : line.keySet()) {
                    Object cellValue = line.get(key);
                    if (cellValue instanceof String) { // cell is text so
                        // test data
                        if (ParameterValueUtil.valueContains((String) cellValue, name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean isUseData(final ElementParameterType param, final String name) {
        if (param == null || name == null) {
            return false;
        }
        if (param.getField().equals(EParameterFieldType.TABLE.getName())) { // for TABLE
            EList elementValue = param.getElementValue();
            if (elementValue != null) {
                for (ElementValueType valueType : (List<ElementValueType>) elementValue) {
                    if (valueType.getValue() != null) { // cell is text so
                        // test data
                        if (ParameterValueUtil.valueContains(valueType.getValue(), name)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            String value = param.getValue();
            if (value != null && ParameterValueUtil.valueContains(value, name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean valueContains(String value, String toTest) {
        if (value.contains(toTest)) {
            Perl5Matcher matcher = new Perl5Matcher();
            Perl5Compiler compiler = new Perl5Compiler();
            Pattern pattern;

            try {
                pattern = compiler.compile(getQuotePattern(toTest));
                if (matcher.contains(value, pattern)) {
                    return true;
                }
            } catch (MalformedPatternException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private static String getQuotePattern(String toTest) {
        final String prefix = "\\b("; //$NON-NLS-1$
        String suffix = ")\\b"; //$NON-NLS-1$
        if (!ContextParameterUtils.isContainContextParam(toTest)) { // context parameter renamed
            suffix = ")(\\b|\\_)"; //$NON-NLS-1$
        }

        return prefix + UpdateContextVariablesHelper.replaceSpecialChar(toTest) + suffix;

    }
}
