import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items = new ArrayList<>();

    public String addItem(Product product, int quantity) {
        if (quantity <= 0) {
            return "Quantity must be greater than 0.";
        }
        if (quantity > product.getStockQuantity()) {
            return "Only " + product.getStockQuantity() + " units of " + product.getName() + " available.";
        }
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return "Updated quantity for " + product.getName();
            }
        }
        items.add(new CartItem(product, quantity));
        return product.getName() + " added to cart.";
    }

    public void removeItem(int productId) {
        items.removeIf(item -> item.getProduct().getId() == productId);
    }

    public List<CartItem> getItems() { return items; }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) total += item.getLineTotal();
        return total;
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public void clearCart() { items.clear(); }

    public String toJson() {
        List<String> itemJsons = new ArrayList<>();
        for (CartItem item : items) itemJsons.add(item.toJson());
        return "{\"items\":" + JsonUtil.array(itemJsons) + ",\"subtotal\":" + getSubtotal() + "}";
    }
}
