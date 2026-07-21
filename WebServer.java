import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WebServer {

    // Shared "backend state" - in a real app this would live in a database
    private static Inventory inventory = new Inventory();
    private static Cart cart = new Cart();
    private static Checkout checkout = new Checkout();
    private static List<Order> orderHistory = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        inventory.loadSampleData();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ----- API routes -----
        server.createContext("/api/products", WebServer::handleProducts);
        server.createContext("/api/cart", WebServer::handleCart);
        server.createContext("/api/cart/add", WebServer::handleCartAdd);
        server.createContext("/api/cart/remove", WebServer::handleCartRemove);
        server.createContext("/api/checkout", WebServer::handleCheckout);
        server.createContext("/api/orders", WebServer::handleOrders);

        // ----- Static file serving (the actual website: HTML/CSS/JS) -----
        server.createContext("/", WebServer::handleStatic);

        server.setExecutor(null); // default executor is fine for a student project
        System.out.println("Server running at http://localhost:8080");
        server.start();
    }

    // GET /api/products -> list of all products
    private static void handleProducts(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        sendResponse(exchange, 200, inventory.allProductsJson());
    }

    // GET /api/cart -> current cart contents
    private static void handleCart(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, cart.toJson());
    }

    // POST /api/cart/add   body: {"productId":1,"quantity":2}
    private static void handleCartAdd(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        String body = readBody(exchange);
        try {
            int productId = JsonUtil.getInt(body, "productId");
            int quantity = JsonUtil.getInt(body, "quantity");
            Product product = inventory.findById(productId);
            if (product == null) {
                sendResponse(exchange, 404, "{\"error\":\"Product not found\"}");
                return;
            }
            String message = cart.addItem(product, quantity);
            sendResponse(exchange, 200, "{\"message\":\"" + JsonUtil.escape(message) + "\",\"cart\":" + cart.toJson() + "}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
        }
    }

    // POST /api/cart/remove   body: {"productId":1}
    private static void handleCartRemove(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        String body = readBody(exchange);
        int productId = JsonUtil.getInt(body, "productId");
        cart.removeItem(productId);
        sendResponse(exchange, 200, cart.toJson());
    }

    // POST /api/checkout -> confirms the order, reduces stock, clears cart
    private static void handleCheckout(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        if (cart.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Cart is empty\"}");
            return;
        }
        double subtotal = cart.getSubtotal();
        double tax = checkout.calculateTax(subtotal);
        double total = checkout.calculateTotal(subtotal);

        boolean paid = checkout.processPayment(total);
        if (!paid) {
            sendResponse(exchange, 402, "{\"error\":\"Payment failed\"}");
            return;
        }

        for (CartItem item : cart.getItems()) {
            item.getProduct().reduceStock(item.getQuantity());
        }

        Order order = new Order(cart.getItems(), subtotal, tax, total);
        orderHistory.add(order);
        cart.clearCart();

        sendResponse(exchange, 200, order.toJson());
    }

    // GET /api/orders -> order history
    private static void handleOrders(HttpExchange exchange) throws IOException {
        List<String> jsons = new ArrayList<>();
        for (Order o : orderHistory) jsons.add(o.toJson());
        sendResponse(exchange, 200, JsonUtil.array(jsons));
    }

    // Serves index.html, style.css, script.js from the /public folder
    private static void handleStatic(HttpExchange exchange) throws IOException {
        String requested = exchange.getRequestURI().getPath();
        if (requested.equals("/")) requested = "/index.html";

        Path filePath = Path.of("public" + requested);
        if (!Files.exists(filePath)) {
            sendResponse(exchange, 404, "Not found");
            return;
        }

        String contentType = "text/plain";
        if (requested.endsWith(".html")) contentType = "text/html";
        else if (requested.endsWith(".css")) contentType = "text/css";
        else if (requested.endsWith(".js")) contentType = "application/javascript";

        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ----- Helpers -----

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toString("UTF-8");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // Allows the frontend JS to call this API cleanly
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
