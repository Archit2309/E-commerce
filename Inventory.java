import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) { products.add(product); }

    public List<Product> getAllProducts() { return products; }

    public Product findById(int id) {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public String allProductsJson() {
        List<String> jsons = new ArrayList<>();
        for (Product p : products) jsons.add(p.toJson());
        return JsonUtil.array(jsons);
    }

    public void loadSampleData() {
        addProduct(new Product(1, "Laptop", 55000.00, "Electronics", 10));
        addProduct(new Product(2, "Headphones", 1500.00, "Electronics", 25));
        addProduct(new Product(3, "T-Shirt", 499.00, "Clothing", 50));
        addProduct(new Product(4, "Jeans", 1200.00, "Clothing", 30));
        addProduct(new Product(5, "Novel - Fiction", 350.00, "Books", 40));
        addProduct(new Product(6, "Coffee Mug", 250.00, "Home", 60));
    }
}
