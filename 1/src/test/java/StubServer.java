import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mockserver.integration.ClientAndServer;
import scala.concurrent.duration.Duration;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

public class StubServer implements AutoCloseable {
    private final ClientAndServer stubServer;

    public StubServer(int port, Duration timeout) {
        stubServer = startClientAndServer(port);
        stubServer.when(request()
                .withMethod("GET")
                .withPath("/search")
        ).respond(
                request -> {
                    long timeoutMilliseconds = timeout.toMillis();

                    if (timeoutMilliseconds > 0) {
                        Thread.sleep(timeoutMilliseconds);
                    }

                    String queryName = request.getFirstQueryStringParameter("q");
                    String responseBody = genResponseBody(queryName);

                    return response()
                            .withStatusCode(OK_200.code())
                            .withBody(responseBody);
                }
        );
    }

    public static String genResponseBody(String queryName) {
        return IntStream.range(1, 5)
                .mapToObj(i -> queryName + ": result number " + i)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void close() {
        stubServer.close();
    }
}
