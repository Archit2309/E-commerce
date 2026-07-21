import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Order {
    private static int orderCounter = 1000;

    private int orderId;
    private List<CartItem> orderedItems;
    private double subtotal;
    private double tax;
    private double total;
    private String status;
    private LocalDateTime timestamp;

    public Order(List<CartItem> orderedItems, double subtotal, double tax, double total) {
        this.orderId = ++orderCounter;
        this.orderedItems = new ArrayList<>(orderedItems);
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.status = "CONFIRMED";
        this.timestamp = LocalDateTime.now();
    }

    public int getOrderId() { return orderId; }
    public double getTotal() { return total; }

    public String toJson() {
        List<String> itemJsons = new ArrayList<>();
        for (CartItem item : orderedItems) itemJsons.add(item.toJson());
        return "{"
                + "\"orderId\":" + orderId + ","
                + "\"items\":" + JsonUtil.array(itemJsons) + ","
                + "\"subtotal\":" + subtotal + ","
                + "\"tax\":" + tax + ","
                + "\"total\":" + total + ","
                + "\"status\":\"" + status + "\","
                + "\"timestamp\":\"" + timestamp + "\""
                + "}";
    }
}
