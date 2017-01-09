/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.grimi.ldapConnection;

/**
 *
 * @author SBellotti
 */
public class LdapClientWrapper
{

    private LdapClient ldapClient;

    /**
     *
     * @param ldapServerUrl
     * @param localDomain
     * @param domain
     * @param userDomain
     * @param username
     * @param password
     * @param propertyName
     * @return
     */
    public String GetLDAPUserInfo(String ldapServerUrl, String localDomain, 
            String domain, String userDomain, String username, String password, 
            String propertyName)
    {
        ldapClient = new LdapClient();
        return ldapClient.GetLDAPUserInfo(ldapServerUrl, 
                localDomain, 
                domain, 
                userDomain, 
                username, 
                password, 
                propertyName);
    }

    /**
     *
     * @param ldapServerUrl
     * @param localDomain
     * @param domain
     * @param userDomain
     * @param username
     * @param propertyName
     * @return
     */
    public String GetLDAPUserInfoFromUsername(String ldapServerUrl, String localDomain,
            String domain, String userDomain, String username, String propertyName)
    {
        ldapClient = new LdapClient();
        try
        {            
            return ldapClient.GetLDAPUserInfoFromUsername(ldapServerUrl, localDomain, domain,userDomain,username,propertyName);
        }
        catch(Exception ex)
        {}
        
        return null;
    }
}
