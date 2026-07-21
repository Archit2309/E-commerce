public class Checkout {
    private static final double TAX_RATE = 0.05;

    public double calculateTax(double subtotal) {
        return subtotal * TAX_RATE;
    }

    public double calculateTotal(double subtotal) {
        return subtotal + calculateTax(subtotal);
    }

    // Simulates a payment gateway call
    public boolean processPayment(double amount) {
        return true; // always succeeds in this mock version
    }
}
