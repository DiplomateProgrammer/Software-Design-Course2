package search;

import lombok.Getter;

@Getter
public class SearchEngineInfo {
    private final String host;
    private final String engine;
    private final int port;

    public SearchEngineInfo(String host, String engine, int port) {
        this.host = host;
        this.engine = engine;
        this.port = port;
    }
}
