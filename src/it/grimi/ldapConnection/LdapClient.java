/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.grimi.ldapConnection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.apache.log4j.Logger;

/**
 *
 * @author sbellotti
 */
public class LdapClient
{

    /**
     * Il logger principale di questa classe
     */
    final static Logger logger = Logger.getLogger(LdapClient.class.getName());

    /**
     *
     */
    private final PropertiesReader propertiesReader = new PropertiesReader();

    /**
     *
     * @param urlLdapServer
     * @param localDomain
     * @param domain
     * @param userDomain
     * @param username
     * @param propertyName
     * @return
     */
    public String GetLDAPUserInfoFromUsername(String urlLdapServer, String localDomain, String domain,
            String userDomain, String username, String propertyName) throws IOException
    {
        LdapClient.logger.info("********** GetLDAPUserInfoFromUsername **********");
        try
        {
            LdapClient.logger.info("Reading Admin Username from file...");
            String adminUserName = this.propertiesReader.getPropValue("username_" + domain);
            LdapClient.logger.info("Admin Username: " + adminUserName);
            LdapClient.logger.info("Reading Admin Password from file...");
            String adminPwd = this.propertiesReader.getPropValue("password_" + domain);
            LdapClient.logger.info("Got the password! ;D");
            
            return GetLDAPUserInfo(urlLdapServer, localDomain, domain, userDomain,
                adminUserName, adminPwd, username, propertyName); 
        }
        catch(FileNotFoundException ex)
        {
            LdapClient.logger.error("Error in reading from file. " + ex.getMessage());
            throw ex;
        }
    }

    /**
     *
     * @param url
     * @param localDomain
     * @param domain
     * @param userDomain
     * @param username
     * @param password
     * @param propertyName
     * @return
     */
    public String GetLDAPUserInfo(String url, String localDomain, String domain, String userDomain,
            String username, String password, String propertyName)
    {
        LdapClient.logger.info("********** trying to connect as " + username + " **********");

        return GetLDAPUserInfo(url, localDomain, domain, userDomain,
            username, password, username, propertyName);                        
    }

    /**
     *
     * @param url
     * @param localDomain
     * @param domain
     * @param userDomain
     * @param username
     * @param password
     * @param propertyName
     * @return
     */
    private String GetLDAPUserInfo(String url, String localDomain, String domain, String userDomain,
            String username, String password, String usernameToCheck, String propertyName)
    {
        LdapClient.logger.info("********** looking for '" + propertyName + "' of " + username + " **********");

        LdapContext ldapContext = getLdapContext(url, 
                userDomain, 
                username, 
                password);
        
        if (ldapContext != null)
        {
            LdapClient.logger.info("Got LDAP context.");

            SearchControls searchControls = getSearchControls();
            return getUserInfo(localDomain, domain, usernameToCheck, ldapContext, searchControls, propertyName);
        } else
        {
            LdapClient.logger.info("LDAP Context is null!");
        }

        return null;
    }

    /**
     * Crea il contesto con il quale LDAP andrà ad effettuare l'autenticazione
     *
     * @param url
     * userDomain domain
     * @param username
     * @param password
     * @return
     */
    private LdapContext getLdapContext(String url, String userDomain, String username, String password)
    {
        LdapContext ldapContext = null;

        try
        {
            Hashtable<String, String> ldapEnvironment = new Hashtable<>();

            ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "Simple");

            if (username.contains("\\"))
            {
                ldapEnvironment.put(Context.SECURITY_PRINCIPAL, username);
            } else
            {
                ldapEnvironment.put(Context.SECURITY_PRINCIPAL, userDomain + "\\" + username);
            }

            ldapEnvironment.put(Context.SECURITY_CREDENTIALS, password);
            ldapEnvironment.put(Context.PROVIDER_URL, url);
            ldapEnvironment.put(Context.REFERRAL, "follow");

            ldapContext = new InitialLdapContext(ldapEnvironment, null);

            LdapClient.logger.info("LDAP Context for user " + username + " COMPLETED");

        } catch (NamingException ex)
        {
            LdapClient.logger.error("LDAP Context for user " + username + " FAILED for reason: \n" + ex.getMessage());
        }

        return ldapContext;
    }

    /**
     * Cerca l'utente all'interno di LDAP e ne ritorna la proprietà evidenziata
     * nel parametro <b>propertyName</b>
     *
     * @param localDomain
     * @param domain
     * @param userName
     * @param ldapContext
     * @param searchControls
     * @param propertyName
     * @return
     */
    private String getUserInfo(String localDomain, String domain, String userName, LdapContext ldapContext,
            SearchControls searchControls, String propertyName)
    {
        String result = "";

        try
        {
            String cnParam = "dc=" + domain.toLowerCase() + ",dc=" + localDomain;

            //Nel dubbio provo a rimuovere un eventuale dominio forzato
            userName = userName.substring(userName.indexOf("\\") + 1);
            
            NamingEnumeration<SearchResult> answer = ldapContext.search(cnParam, "sAMAccountName=" + userName, searchControls);

            if (answer.hasMore())
            {
                LdapClient.logger.error("User found.");
                Attributes attrs = answer.next().getAttributes();
                result = attrs.get(propertyName).toString().replace(propertyName + ": ", "");
            } else
            {
                LdapClient.logger.error("User NOT found.");
            }
        } catch (NamingException ex)
        {
            LdapClient.logger.error(ex.getMessage());
        }

        return result;
    }

    /**
     *
     * @return
     */
    private SearchControls getSearchControls()
    {
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String[] attrIDs =
        {
            "distinguishedName", "sn", "givenname", "mail"
        };

        cons.setReturningAttributes(attrIDs);

        return cons;
    }
}
