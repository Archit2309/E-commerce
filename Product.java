public class Product {
    private int id;
    private String name;
    private double price;
    private String category;
    private int stockQuantity;

    public Product(int id, String name, double price, String category, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStockQuantity() { return stockQuantity; }

    public void reduceStock(int amount) {
        if (amount > stockQuantity) {
            throw new IllegalArgumentException("Not enough stock for " + name);
        }
        stockQuantity -= amount;
    }

    // Converts this product into a JSON object string
    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"name\":\"" + JsonUtil.escape(name) + "\","
                + "\"price\":" + price + ","
                + "\"category\":\"" + JsonUtil.escape(category) + "\","
                + "\"stock\":" + stockQuantity
                + "}";
    }
}
