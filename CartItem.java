public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    public String toJson() {
        return "{"
                + "\"productId\":" + product.getId() + ","
                + "\"name\":\"" + JsonUtil.escape(product.getName()) + "\","
                + "\"price\":" + product.getPrice() + ","
                + "\"quantity\":" + quantity + ","
                + "\"lineTotal\":" + getLineTotal()
                + "}";
    }
}
