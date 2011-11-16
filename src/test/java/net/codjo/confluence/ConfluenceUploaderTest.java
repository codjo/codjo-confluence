package net.codjo.confluence;
import java.net.SocketTimeoutException;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import static org.apache.commons.httpclient.HttpStatus.SC_NOT_FOUND;
import org.apache.commons.httpclient.params.HttpClientParams;
import static org.mockito.Matchers.anyObject;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
/**
 *
 */
public class ConfluenceUploaderTest extends TestCase {
    private ConfluenceUploader uploader;
    private HttpClient httpClientMock;


    public void test_retry_default() throws Exception {
        Mockito.when(httpClientMock.executeMethod((HttpMethod)anyObject())).thenReturn(SC_NOT_FOUND);

        try {
            uploader.login();
            fail("Exception attendue");
        }
        catch (ConfluenceException exception) {
            assertTrue(exception.getLocalizedMessage().contains("Http Error : " + SC_NOT_FOUND));
        }

        verify(httpClientMock, times(3)).executeMethod((HttpMethod)anyObject());
    }


    public void test_retry() throws Exception {
        uploader.setRetryCount(10);

        Mockito.when(httpClientMock.executeMethod((HttpMethod)anyObject())).thenReturn(SC_NOT_FOUND);

        try {
            uploader.login();
            fail("Exception attendue");
        }
        catch (ConfluenceException exception) {
            assertTrue(exception.getLocalizedMessage().contains("Http Error : " + SC_NOT_FOUND));
        }

        verify(httpClientMock, times(11)).executeMethod((HttpMethod)anyObject());
    }


    public void test_retry_timeoutException() throws Exception {
        uploader.setRetryCount(10);

        Mockito.when(httpClientMock.executeMethod((HttpMethod)anyObject()))
              .thenThrow(new SocketTimeoutException());

        try {
            uploader.login();
            fail("Exception attendue");
        }
        catch (ConfluenceTimeoutException exception) {
            assertEquals("Délai d'attente dépassé.", exception.getLocalizedMessage());
        }

        verify(httpClientMock, times(11)).executeMethod((HttpMethod)anyObject());
    }


    @Override
    protected void setUp() throws Exception {
        httpClientMock = Mockito.mock(HttpClient.class);
        Mockito.when(httpClientMock.getParams()).thenReturn(new HttpClientParams());

        uploader = new ConfluenceUploader(new ConfluenceSession("http://wrong-url", "user_dev", "user_dev"));
        uploader.setHttpClient(httpClientMock);
    }
}
