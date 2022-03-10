import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import actors.MasterActor;
import search.SearchQuery;
import search.SearchEngineInfo;
import scala.concurrent.duration.Duration;

public class SearchAgregatorService {
    private final List<SearchEngineInfo> searchEngines;

    public SearchAgregatorService() {
        this.searchEngines = new ArrayList<>();
    }

    public void addSearchEngine(SearchEngineInfo info) {
        searchEngines.add(info);
    }

    public HashMap<String, String> search(String msg, Duration timeout) throws ExecutionException, InterruptedException {
        ActorSystem actorSystem = ActorSystem.create("SearchSystem");

        CompletableFuture<HashMap<String, String>> futureResult = new CompletableFuture<>();

        try {
            ActorRef master = actorSystem.actorOf(Props.create(MasterActor.class, searchEngines, futureResult, timeout));
            master.tell(new SearchQuery(msg), ActorRef.noSender());

            return futureResult.get();
        } finally {
            actorSystem.terminate();
        }
    }
}
