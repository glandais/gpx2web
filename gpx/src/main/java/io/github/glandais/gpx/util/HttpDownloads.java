package io.github.glandais.gpx.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Internal helper shared by the tile downloaders. */
public final class HttpDownloads {

    /** Time allowed to open the connection. */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Time allowed for a whole download, response headers and body. */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private HttpDownloads() {}

    public static HttpClient newClient(Duration connectTimeout) {
        return HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    /**
     * Sends the request and waits for the whole response, body included, for at most {@code timeout}.
     *
     * <p>{@link HttpRequest.Builder#timeout} alone only covers the wait for the response headers: a server that
     * sends its headers and then stalls would block {@link HttpClient#send} forever.
     *
     * @throws HttpTimeoutException if the response is not complete within {@code timeout}
     * @throws InterruptedIOException if the thread is interrupted; its interrupt flag is restored
     */
    public static <T> HttpResponse<T> send(
            HttpClient httpClient,
            HttpRequest request,
            HttpResponse.BodyHandler<T> bodyHandler,
            Duration timeout,
            String description)
            throws IOException {
        CompletableFuture<HttpResponse<T>> future = httpClient.sendAsync(request, bodyHandler);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new HttpTimeoutException("Timed out after " + timeout.toMillis() + " ms " + description);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            InterruptedIOException ioe = new InterruptedIOException("Interrupted " + description);
            ioe.initCause(e);
            throw ioe;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpTimeoutException timeoutException) {
                HttpTimeoutException copy = new HttpTimeoutException(timeoutException.getMessage() + " " + description);
                copy.initCause(cause);
                throw copy;
            }
            throw new IOException("Failed " + description + ": " + cause, cause);
        }
    }
}
