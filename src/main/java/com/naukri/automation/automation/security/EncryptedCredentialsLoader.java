package com.naukri.automation.automation.security;

import java.nio.file.Paths;
import java.util.Properties;
import java.io.StringReader;

public class EncryptedCredentialsLoader {
    public static Properties load() throws Exception {
        String encrypted = CryptoUtil.readEncryptedFromFile(Paths.get("credentials.enc"));
        char[] masterPass = System.getenv("NAUKRI_MASTER_PASS").toCharArray();
        String plain = CryptoUtil.decrypt(encrypted, masterPass);

        Properties props = new Properties();
        props.load(new StringReader(plain));
        return props;
    }
}

