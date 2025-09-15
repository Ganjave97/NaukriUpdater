package com.naukri.automation.automation.security;

import java.nio.file.Paths;
import java.util.Scanner;

public class CreateEncryptedCreds {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter credentials (format: username=you@example.com\\npassword=secret):");
        String creds = sc.useDelimiter("\\A").next();
        System.out.print("Enter master passphrase: ");
        char[] pass = System.console() != null ? System.console().readPassword() : sc.nextLine().toCharArray();

        String encrypted = CryptoUtil.encrypt(creds, pass);
        CryptoUtil.writeEncryptedToFile(Paths.get("credentials.enc"), encrypted);
        System.out.println("Encrypted credentials saved to credentials.enc");
    }
}

