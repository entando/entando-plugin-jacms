/*
 * Copyright 2020-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.plugins.jacms.aps.system.services.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.entando.entando.aps.system.services.security.NonceInjector;

public class VelocityNonceInjector {
    
    private static final String NONCE_INJECTION = "nonce=\"\\$content\\.nonce\"";
    
    /**
     * TODO: consider inheriting from engine's class {@link NonceInjector}
     */
    private static final Pattern SCRIPT_REGEX = Pattern.compile(
            "(<script)(?:[\\t\\r\\n\\s]+([^\"=]*)=\"([^\"]*)\"[\\t\\r\\n\\s]*){0,8}([^>]*>)");
    
    private VelocityNonceInjector() {
        //Not used
    }
    
    public static String process(String source) {
        if (source == null) return null;
        Matcher scriptsMatcher = SCRIPT_REGEX.matcher(source);
        StringBuffer sb = new StringBuffer();
        while (scriptsMatcher.find()) {
            String replacement;
            if (NonceInjector.hasNonce(scriptsMatcher)) {
                replacement = "$0";
            } else {
                replacement = "$1 " + NONCE_INJECTION;
                if (scriptsMatcher.groupCount() == 4 && scriptsMatcher.group(2) != null && scriptsMatcher.group(3) != null) {
                    replacement += " $2=\"$3\" ";
                }
                replacement += "$4";
            }
            scriptsMatcher.appendReplacement(sb, replacement);
        }
        return scriptsMatcher.appendTail(sb).toString();
    }
    
}
