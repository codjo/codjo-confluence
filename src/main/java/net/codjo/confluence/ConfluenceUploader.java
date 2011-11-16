/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.confluence;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import static org.apache.commons.httpclient.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

public class ConfluenceUploader {
    private static final String LOGIN_URL = "/login.action";
    private static final String LOGOUT_URL = "/logout.action";
    private static final String LOGIN_USER_PARAM = "os_username";
    private static final String LOGIN_PWD_PARAM = "os_password";
    private static final String UPLOAD_FILE_PARAM = "file_0";
    private static final String UPLOAD_URL_PREFIX = "/pages/doattachfile.action?pageId=";

    private static final Logger LOG = Logger.getLogger(ConfluenceUploader.class);

    private HttpClient httpClient;
    private final ConfluenceSession session;
    private final String now;
    private int timeout;
    private int retryCount = 2;
    private long retryDelay = 1000;


    public ConfluenceUploader(ConfluenceSession session) {
        this.session = session;
        now = DateFormat.getDateInstance().format(new Date());
    }


    public int getTimeout() {
        return timeout;
    }


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    public void upload(File file, String pageId) throws IOException, ConfluenceException {
        upload(file, pageId, null);
    }


    public void upload(File file, String pageId, String newName) throws IOException, ConfluenceException {

        String mimeType = (new MimetypesFileTypeMap()).getContentType(file);

        StringPart stringPart = new StringPart("comment_0", "récupération du " + now, "UTF-8");
        stringPart.setTransferEncoding(null);

        FilePartSource filePartSource = new FilePartSource(newName != null ? newName : file.getName(), file);
        FilePart filePart = new FilePart(UPLOAD_FILE_PARAM, filePartSource);
        filePart.setContentType(mimeType);
        filePart.setCharSet("UTF-8");
        filePart.setTransferEncoding(null);

        Part[] parts = {filePart, stringPart};

        PostMethod filePost = new PostMethod(
              session.getServerUrl() + UPLOAD_URL_PREFIX + pageId);
        filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

        getHttpClient().getParams().setSoTimeout(timeout);
        executeMethod(0, filePost);
    }


    public void login() throws ConfluenceException {
        getHttpClient().getParams().setCookiePolicy("compatibility");
        getHttpClient().getParams().setSoTimeout(timeout);
        PostMethod login = new PostMethod(session.getServerUrl() + "/" + LOGIN_URL);
        NameValuePair username = new NameValuePair(LOGIN_USER_PARAM, session.getUser());
        NameValuePair password = new NameValuePair(LOGIN_PWD_PARAM, session.getPassword());
        login.setRequestBody(new NameValuePair[]{username, password});
        executeMethod(0, login);
    }


    public void logout() throws ConfluenceException {
        getHttpClient().getParams().setSoTimeout(timeout);
        GetMethod logout = new GetMethod(session.getServerUrl() + "/" + LOGOUT_URL);
        executeMethod(0, logout);
    }


    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }


    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }


    private void executeMethod(int currentRetryCount, HttpMethod httpMethod) throws ConfluenceException {
        try {
            int httpCode = getHttpClient().executeMethod(httpMethod);
            if (httpCode != SC_MOVED_TEMPORARILY && httpCode != SC_OK) {
                ConfluenceException exception = new ConfluenceException("Http Error : " + httpCode);
                retryExecuteMethod(exception, currentRetryCount, httpMethod);
            }
        }
        catch (SocketTimeoutException ex) {
            ConfluenceTimeoutException timeoutException = new ConfluenceTimeoutException(ex);
            retryExecuteMethod(timeoutException, currentRetryCount, httpMethod);
        }
        catch (IOException ioException) {
            ConfluenceException confluenceException = new ConfluenceException(ioException);
            retryExecuteMethod(confluenceException, currentRetryCount, httpMethod);
        }
        httpMethod.releaseConnection();
    }


    private void retryExecuteMethod(ConfluenceException exception,
                                    int currentRetryCount,
                                    HttpMethod httpMethod) throws ConfluenceException {
        if (currentRetryCount == retryCount) {
            throw exception;
        }
        else {
            try {
                LOG.info("Exception " + exception.getLocalizedMessage() + " générée. Retry...");
                Thread.sleep(retryDelay);
            }
            catch (InterruptedException interruptedException) {
                LOG.debug(interruptedException);
            }
            executeMethod(currentRetryCount + 1, httpMethod);
        }
    }


    void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    private HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient();
        }
        return httpClient;
    }
}
