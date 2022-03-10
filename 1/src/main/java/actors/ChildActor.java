package actors;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;
import search.SearchQuery;
import search.SearchEngineInfo;

public class ChildActor extends AbstractActor {
    @Getter
    static public class ChildActorResult {
        private final String response;
        private final SearchEngineInfo info;

        public ChildActorResult(String response, SearchEngineInfo info) {
            this.response = response;
            this.info = info;
        }
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final SearchEngineInfo info;

    public ChildActor(SearchEngineInfo info) {
        this.info = info;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SearchQuery.class, this::processQuery).build();
    }

    public void processQuery(SearchQuery msg) {
        URI uri = URI.create(String.format("http://%s:%d/search?q=%s", info.getHost(), info.getPort(), msg.getQuery()));

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        try {
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body().intern();

            sender().tell(new ChildActorResult(response, info), getSelf());
        } catch (Throwable error) {
            log.error("Query={} to engine={} with port={} failed: {}", msg.getQuery(), info.getEngine(), info.getPort(),
                    error.getMessage());
        }
    }
}
