package net.codjo.confluence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class ConfluenceServer {
    private static final Logger LOG = Logger.getLogger(ConfluenceServer.class);

    private final ConfluenceSession session;
    private XmlRpcClient xmlRpcClient;
    private long timeout;

    private int retryCount = 2;
    private long retryDelay = 1000;


    public ConfluenceServer(ConfluenceSession session) {
        this.session = session;
    }


    public long getTimeout() {
        return timeout;
    }


    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }


    public int getRetryCount() {
        return retryCount;
    }


    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }


    public long getRetryDelay() {
        return retryDelay;
    }


    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }


    public void login() throws ConfluenceException {
        String confluenceSessionToken = (String)
              executeRemoteCall("confluence1.login", 0, session.getUser(), session.getPassword());
        session.setConfluenceSessionToken(confluenceSessionToken);
    }


    public void logout() throws ConfluenceException {
        executeRemoteCall("confluence1.logout", 0, token());
        session.setConfluenceSessionToken(null);
    }


    public String getVersion() throws ConfluenceException {
        Map result = (Map)executeRemoteCall("confluence1.getServerInfo", 0, token());
        return result.get("majorVersion") + "-" + result.get("minorVersion");
    }


    public List<Space> getSpaces() throws ConfluenceException {
        Object[] spaces = (Object[])executeRemoteCall("confluence1.getSpaces", 0, token());
        List<Space> results = new ArrayList<Space>();
        for (Object space : spaces) {
            results.add(new Space((Map<String, String>)space));
        }
        return results;
    }


    public List<PageSummary> getAllPageSummaries(Space space) throws ConfluenceException {
        Object[] pages = (Object[])executeRemoteCall("confluence1.getPages", 0, token(), space.getKey());

        List<PageSummary> sortedPageList = new ArrayList<PageSummary>();
        for (Object page : pages) {
            sortedPageList.add(new PageSummary((Map<String, String>)page));
        }
        return sortedPageList;
    }


    public Page getPage(String pageId) throws ConfluenceException {
        Map<String, String> page = (Map<String, String>)executeRemoteCall("confluence1.getPage", 0, token(),
                                                                          pageId);
        return new Page(page);
    }


    public Page getPage(String spaceKey, String pageTitle) throws ConfluenceException {
        Map<String, String> page = (Map<String, String>)
              executeRemoteCall("confluence1.getPage", 0, token(), spaceKey, pageTitle);
        return new Page(page);
    }


    public List<PageSummary> getChildren(String pageId) throws ConfluenceException {
        Object[] pageSummaries = (Object[])executeRemoteCall("confluence1.getChildren", 0, token(), pageId);
        List<PageSummary> sortedPageList = new ArrayList<PageSummary>();
        for (Object pageSummary : pageSummaries) {
            sortedPageList.add(new PageSummary((Map<String, String>)pageSummary));
        }
        return sortedPageList;
    }


    public List<PageSummary> getChildren(String spaceKey, String pageTitle) throws ConfluenceException {
        Page parent = getPage(spaceKey, pageTitle);
        return getChildren(parent.getId());
    }


    public boolean removeLabelByName(String labelName, String objectId) throws ConfluenceException {
        Object result;
        try {
            result = executeRemoteCall("confluence1.removeLabelByName", 0, token(), labelName, objectId);
        }
        catch (ConfluenceException e) {
            return !(e.getMessage().contains("The given label does not exist: " + labelName));
        }
        return (Boolean)
              result;
    }


    public void removeLabelByName(String labelName) throws ConfluenceException {
        try {
            List<SearchResult> list = searchByLabelName(labelName);
            for (SearchResult searchResult : list) {
                executeRemoteCall("confluence1.removeLabelByName", 0, token(), labelName,
                                  searchResult.getId());
            }
        }
        catch (ConfluenceException e) {
            if (!(e.getMessage().contains("The given label does not exist: " + labelName))) {
                throw e;
            }
        }
    }


    public boolean addLabel(String labelName, String objectId) throws ConfluenceException {
        Object result;
        try {
            result = executeRemoteCall("confluence1.addLabelByName", 0, token(), labelName, objectId);
        }
        catch (ConfluenceException e) {
            return !(e.getMessage().contains("The given label does not exist: " + labelName));
        }
        return (Boolean)
              result;
    }


    public Page storePage(Page page) throws ConfluenceException {
        return new Page((Map<String, String>)
              executeRemoteCall("confluence1.storePage", 0, token(), page.getConfluenceStructure()));
    }


    public void removePage(Page page) throws ConfluenceException {
        executeRemoteCall("confluence1.removePage", 0, token(), page.getId());
    }


    public List<SearchResult> search(String spaceKey, String queryString, int nbResults)
          throws ConfluenceException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("spaceKey", spaceKey);
        parameters.put("type", "page");
        Object[] pages = (Object[])executeRemoteCall("confluence1.search", 0, token(), queryString,
                                                     parameters,
                                                     nbResults);
        List<SearchResult> sortedPageList = new ArrayList<SearchResult>();
        for (Object page : pages) {
            sortedPageList.add(new SearchResult((Map<String, String>)page));
        }
        return sortedPageList;
    }


    public List<SearchResult> searchByLabelName(String label) throws ConfluenceException {
        List<SearchResult> labels = new ArrayList<SearchResult>();

        try {
            Object[] labelObjects = (Object[])executeRemoteCall("confluence1.getLabelContentByName", 0,
                                                                token(),
                                                                label);

            for (Object currentLabel : labelObjects) {
                labels.add(new SearchResult((Map<String, String>)currentLabel));
            }
        }
        catch (ConfluenceException exception) {
            if (!exception.getLocalizedMessage().contains("does not exist")) {
                throw exception;
            }
        }
        return labels;
    }


    public List<Attachment> getAttachments(String pageId) throws ConfluenceException {
        Object[] attachments = (Object[])executeRemoteCall("confluence1.getAttachments", 0, token(), pageId);
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        for (Object attachment : attachments) {
            attachmentList.add(new Attachment((Map<String, String>)attachment));
        }
        return attachmentList;
    }


    public boolean moveAttachment(String oldPageId, String oldName, String newPageId, String newName)
          throws ConfluenceException {
        Object result = executeRemoteCall("confluence1.moveAttachment", 0, token(), oldPageId, oldName,
                                          newPageId, newName);
        return (Boolean)result;
    }


    public File downloadAttachment(String pageId, String attachmentName, String directory)
          throws ConfluenceException, IOException {
        byte[] byteArray = (byte[])executeRemoteCall("confluence1.getAttachmentData", 0, token(), pageId,
                                                     attachmentName, "0");

        File result = new File(directory, attachmentName);
        FileChannel fileChannel = new FileOutputStream(result).getChannel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        fileChannel.write(byteBuffer);
        fileChannel.close();

        return result;
    }


    public List<Label> getLabelsById(String id) throws ConfluenceException {
        List<Label> labels = new ArrayList<Label>();

        try {
            Object[] labelObjects = (Object[])executeRemoteCall("confluence1.getLabelsById", 0, token(), id);

            for (Object currentLabel : labelObjects) {
                labels.add(new Label((Map<String, String>)currentLabel));
            }
        }
        catch (ConfluenceException exception) {
            if (!exception.getLocalizedMessage().contains("does not exist")) {
                throw exception;
            }
        }
        return labels;
    }


    public String renderPage(String spaceKey, String pageId, String content) throws ConfluenceException {
        return (String)executeRemoteCall("confluence1.renderContent", 0, token(), spaceKey, pageId, content);
    }


    private Object executeRemoteCall(String methodName, int currentRetryCount, Object... arguments)
          throws ConfluenceException {
        try {
            XmlRpcClient rpcClient = createRpcClient();
            TimingOutCallback callback = new TimingOutCallback(timeout);
            if (timeout <= 0) {
                return rpcClient.execute(methodName, arguments(arguments));
            }
            else {
                rpcClient.executeAsync(methodName, arguments(arguments), callback);
                return callback.waitForResponse();
            }
        }
        catch (TimingOutCallback.TimeoutException exception) {
            ConfluenceTimeoutException timeoutException = new ConfluenceTimeoutException(exception);
            return retryExecuteRemoteCall(currentRetryCount, timeoutException, methodName, arguments);
        }
        catch (XmlRpcException exception) {
            boolean mustRetry = isException(exception);
            ConfluenceException confluenceException = new ConfluenceException(exception);
            if (mustRetry) {
                return retryExecuteRemoteCall(currentRetryCount, confluenceException, methodName, arguments);
            }
            else {
                throw confluenceException;
            }
        }
        catch (Throwable throwable) {
            throw new ConfluenceException(throwable);
        }
    }


    private Object retryExecuteRemoteCall(int currentRetryCount,
                                          ConfluenceException exception,
                                          String methodName,
                                          Object... arguments) throws ConfluenceException {
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
            return executeRemoteCall(methodName, currentRetryCount + 1, arguments);
        }
    }


    private List<Object> arguments(Object... arguments) {
        List<Object> args = new ArrayList<Object>();
        for (Object argument : arguments) {
            args.add(argument);
        }
        return args;
    }


    private String token() {
        return session.getConfluenceSessionToken();
    }


    void setXmlRpcClient(XmlRpcClient xmlRpcClient) {
        this.xmlRpcClient = xmlRpcClient;
    }


    private XmlRpcClient createRpcClient() throws MalformedURLException {
        if (xmlRpcClient == null) {
            xmlRpcClient = new XmlRpcClient();
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(session.getServerUrl() + "/rpc/xmlrpc"));
            config.setEncoding("ISO-8859-1");
            xmlRpcClient.setConfig(config);
        }
        return xmlRpcClient;
    }


    private boolean isException(XmlRpcException exception) {
        return !exception.getLocalizedMessage().contains("does not exist");
    }
}
