package net.codjo.confluence;
/**
 *
 */
public class ConfluenceSession {
    private String user;
    private String password;
    private String serverUrl;
    private String confluenceSessionToken;


    public ConfluenceSession(String serverUrl, String user, String password) {
        this.serverUrl = serverUrl;
        this.user = user;
        this.password = password;
    }


    public String getUser() {
        return user;
    }


    public String getPassword() {
        return password;
    }


    public String getServerUrl() {
        return serverUrl;
    }


    public void setConfluenceSessionToken(String confluenceSessionToken) {
        this.confluenceSessionToken = confluenceSessionToken;
    }


    public String getConfluenceSessionToken() {
        return confluenceSessionToken;
    }
}
