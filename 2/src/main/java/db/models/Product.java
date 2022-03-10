package db.models;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

import static db.models.ModelsConventions.*;


public class Product {
    private final int id;
    private final String name;
    private final Map<String, String> priceByCurrency;

    public Product(Document doc) {
        this(doc.getInteger(ID_FIELD), doc.getString(NAME_FIELD), doc.getString(EUR), doc.getString(RUB), doc.getString(USD));
    }

    public Product(int id, String name, String eur, String rub, String usd) {
        this.priceByCurrency = new HashMap<>();
        priceByCurrency.put(EUR, eur);
        priceByCurrency.put(RUB, rub);
        priceByCurrency.put(USD, usd);
        this.id = id;
        this.name = name;
    }

    public Document getDocument() {
        return new Document(ID_FIELD, id).append(NAME_FIELD, name).append(EUR, priceByCurrency.get(EUR))
                .append(RUB, priceByCurrency.get(RUB)).append(USD, priceByCurrency.get(USD));
    }

    public String toString(String currency) {
        return "Product{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", price=" + priceByCurrency.get(currency) +
                "}";
    }
}
