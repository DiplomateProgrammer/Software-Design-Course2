package db;

import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.Success;
import db.models.Product;
import db.models.User;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;
import static db.MongoConfig.*;
import static db.models.ModelsConventions.*;


public class MongoDriver {
    public static MongoClient client = MongoClients.create("mongodb://localhost:27017");
    public final static Integer TIMEOUT = 10;

    public static Success createUser(User user) {
        return client
                .getDatabase(DATABASE)
                .getCollection(USERS_COLLECTION)
                .insertOne(user.getDocument())
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .toBlocking()
                .single();
    }

    public static Observable<String> getUsers() {
        return client
                .getDatabase(DATABASE)
                .getCollection(USERS_COLLECTION)
                .find()
                .toObservable()
                .map(d -> new User(d).toString())
                .reduce((u1, u2) -> u1 + ", " + u2);
    }

    public static Success createProduct(Product product) {
        return client
                .getDatabase(DATABASE)
                .getCollection(PRODUCTS_COLLECTION)
                .insertOne(product.getDocument())
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .toBlocking()
                .single();
    }


    // Accepts user id and shows all goods with prices in currency that this user prefers
    public static Observable<String> getProducts(Integer userId) {
        // Tricky part: this observable needs another observable. I believe this construction using "flatMap"
        // will be non-blocking (credits to stackoverflow)
        return findUser(userId).flatMap(user -> client
                .getDatabase(DATABASE)
                .getCollection(PRODUCTS_COLLECTION)
                .find()
                .toObservable()
                .map(document -> new Product(document).toString(user.getCurrency()))
                .reduce((product1, product2) -> product1 + ", " + product2));
    }

    public static Observable<User> findUser(Integer id) {
        return client
                .getDatabase(DATABASE)
                .getCollection(USERS_COLLECTION)
                .find(eq(ID_FIELD, id))
                .first()
                .map(User::new)
                .timeout(TIMEOUT, TimeUnit.SECONDS);
    }
}
