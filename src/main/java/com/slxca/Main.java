package com.slxca;

import com.slxca.rdap.RDAPClient;
import com.slxca.rdap.RDAPResult;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar rdap-java.jar <domain>");
            System.out.println("Example: java -jar rdap-java.jar google.com");
            return;
        }

        RDAPClient client = new RDAPClient();
        String domain = args[0];
        try {
            RDAPResult result = client.checkDomain(domain);
            System.out.println("Domain: " + result.getDomain());
            System.out.println("Registered: " + result.isRegistered());
            System.out.println("RDAP Server: " + result.getServerUrl());
            if (result.isRegistered() && !result.getNameservers().isEmpty()) {
                System.out.println("Nameservers: " + String.join(", ", result.getNameservers()));
            }
        } catch (Exception e) {
            System.err.println("Error checking domain: " + e.getMessage());
        }
    }
}
