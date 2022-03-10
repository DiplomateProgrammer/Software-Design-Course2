package http_server;

import com.mongodb.rx.client.Success;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import db.models.Product;
import db.MongoDriver;
import db.models.User;

import java.util.*;

import static db.models.ModelsConventions.*;


public class NettyHttpServer {
    public final static Integer PORT = 8080;

    public static void main(final String[] args) {
        HttpServer.newServer(PORT).start((req, resp) -> {
                    Observable<String> response;
                    String operationName = req.getDecodedPath().substring(1);
                    Map<String, List<String>> queryParams = req.getQueryParameters();
                    switch (operationName) {
                        case "createUser" -> {
                            response = createUser(queryParams);
                            resp.setStatus(HttpResponseStatus.OK);
                        }
                        case "getUsers" -> {
                            response = getUsers();
                            resp.setStatus(HttpResponseStatus.OK);
                        }
                        case "createProduct" -> {
                            response = createProduct(queryParams);
                            resp.setStatus(HttpResponseStatus.OK);
                        }
                        case "getProducts" -> {
                            response = getProducts(queryParams);
                            resp.setStatus(HttpResponseStatus.OK);
                        }
                        default -> {
                            response = Observable.just("Error: No such operation exists!");
                            resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                        }
                    }
                    return resp.writeString(response);
                })
                .awaitShutdown();
    }

    public static Observable<String> createUser(Map<String, List<String>> queryParams) {
        ArrayList<String> requiredParameters = new ArrayList<>(Arrays.asList(ID_FIELD, CURRENCY_FIELD, NAME_FIELD));
        if (!isValidQueryParams(queryParams, requiredParameters)) {
            return Observable.just("Error: Received invalid query parameters!");
        }
        String name = queryParams.get(NAME_FIELD).get(0);
        int id = Integer.parseInt(queryParams.get(ID_FIELD).get(0));
        String currency = queryParams.get(CURRENCY_FIELD).get(0);
        if (MongoDriver.createUser(new User(id, name, currency)) == Success.SUCCESS) {
            return Observable.just("SUCCESS");
        } else {
            return Observable.just("Error: Error while trying to add to database!");
        }
    }

    public static Observable<String> getUsers() {
        Observable<String> users = MongoDriver.getUsers();
        return Observable.just("{ users = [").concatWith(users).concatWith(Observable.just("]}"));
    }

    public static Observable<String> createProduct(Map<String, List<String>> queryParam) {
        ArrayList<String> required = new ArrayList<>(Arrays.asList(ID_FIELD, NAME_FIELD, EUR, RUB, USD));
        if (!isValidQueryParams(queryParam, required)) {
            return Observable.just("Error: Wrong attributes");
        }
        int id = Integer.parseInt(queryParam.get(ID_FIELD).get(0));
        String name = queryParam.get(NAME_FIELD).get(0);
        String price_euro = queryParam.get(EUR).get(0);
        String price_rub = queryParam.get(RUB).get(0);
        String price_usd = queryParam.get(USD).get(0);
        if (MongoDriver.createProduct(new Product(id, name, price_rub, price_usd, price_euro)) == Success.SUCCESS) {
            return Observable.just("SUCCESS");
        } else {
            return Observable.just("Error: Error while trying to add to database!");
        }
    }

    // Accepts user id to return a list of products with prices in currency that this user prefers
    public static Observable<String> getProducts(Map<String, List<String>> queryParam) {
        ArrayList<String> requiredParameters = new ArrayList<>(Collections.singletonList(ID_FIELD));
        if (!isValidQueryParams(queryParam, requiredParameters)) {
            return Observable.just("Error: Received invalid query parameters!");
        }
        Integer userId = Integer.valueOf(queryParam.get(ID_FIELD).get(0));
        Observable<String> products = MongoDriver.getProducts(userId);
        return Observable.just("{ products for user_id = " + userId + ", products = [").concatWith(products).
                concatWith(Observable.just("]}"));
    }

    private static boolean isValidQueryParams(Map<String, List<String>> queryParam, List<String> required) {
        for (String value : required) {
            if (!queryParam.containsKey(value)) {
                return false;
            }
        }
        return true;
    }
}