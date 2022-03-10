package actors;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import actors.ChildActor.ChildActorResult;

import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.AbstractActor;
import scala.concurrent.duration.Duration;
import search.SearchQuery;
import search.SearchEngineInfo;

public class MasterActor extends AbstractActor {
    private final List<SearchEngineInfo> searchEnginesInfo;
    private final HashMap<String, String> result;
    private final CompletableFuture<HashMap<String, String>> futureResult;

    public MasterActor(List<SearchEngineInfo> info, CompletableFuture<HashMap<String, String>> futureResult, Duration duration) {
        this.searchEnginesInfo = info;
        this.result = new HashMap<>();
        this.futureResult = futureResult;

        this.getContext().setReceiveTimeout(duration);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchQuery.class, this::sendRequest)
                .match(ChildActorResult.class, this::collectChildResult)
                .match(ReceiveTimeout.class, timeout -> {
                    returnResult();
                })
                .build();
    }

    private void sendRequest(SearchQuery query) {
        searchEnginesInfo.forEach(searchEngineInfo ->
                getContext()
                        .actorOf(Props.create(ChildActor.class, searchEngineInfo))
                        .tell(query, getSelf())
        );
    }

    private void collectChildResult(ChildActorResult childResult) {
        result.put(childResult.getInfo().getEngine(), childResult.getResponse());

        if (result.size() == searchEnginesInfo.size()) {
            returnResult();
        }
    }

    private void returnResult() {
        futureResult.complete(result);
        getContext().system().stop(getSelf());
    }
}
