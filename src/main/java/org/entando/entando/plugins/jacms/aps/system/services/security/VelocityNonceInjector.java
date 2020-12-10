package org.entando.entando.plugins.jacms.aps.system.services.security;

import java.util.regex.Matcher;
import org.entando.entando.aps.system.services.security.NonceInjector;

public class VelocityNonceInjector extends NonceInjector {
    private static final String NONCE_INJECTION = "nonce=\"\\$content\\.nonce\"";

    private VelocityNonceInjector() {
        super();
    }

    public static String process(String source) {
        if (source == null) return null;

        Matcher scriptsMatcher = SCRIPT_REGEX.matcher(source);
        StringBuffer sb = new StringBuffer();

        while (scriptsMatcher.find()) {
            String replacement;
            if (hasNonce(scriptsMatcher)) {
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
