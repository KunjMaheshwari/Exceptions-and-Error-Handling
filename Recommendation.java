import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Recommendation {
    public static void main(String[] args) {
        RecommendationService service = new RecommendationService();
        System.out.println(service.getRecommendedItems("user-1"));
        System.out.println(service.getMenu("restaurant-1"));
        service.placeOrder(new Order("order-1", 24.99));
        System.out.println(service.getETA());
        System.out.println(service.getETAWithBackoff());

        PaymentService paymentService = new PaymentService();
        System.out.println(paymentService.charge("user-1", 19.99));
    }
}

class RecommendationService {
    private final RecommendationRepository recommendationService = new RecommendationRepository();
    private final RecommendationCache cacheService = new RecommendationCache();
    private final MenuService menuService = new MenuService();
    private final PaymentProcessor paymentService = new PaymentProcessor();
    private final OrderRetryQueue orderRetryQueue = new OrderRetryQueue();
    private final EtaService etaService = new EtaService();
    private final Logger log = new Logger();

    public List<String> getRecommendedItems(String userId) {
        try {
            return recommendationService.fetchLiveRecommendations(userId);
        } catch (Exception ex) {
            log.warn("Live service failed, falling back to cache");
            return cacheService.getCachedRecommendations(userId);
        }
    }

    public Menu getMenu(String restaurantId) {
        try {
            return menuService.fetchMenu(restaurantId);
        } catch (Exception e) {
            return new Menu("Menu currently unavailable. Please try again later.");
        }
    }

    public void placeOrder(Order order) {
        try {
            paymentService.charge(order);
        } catch (Exception e) {
            orderRetryQueue.enqueue(order);
            log.warn("Payment failed. Queued for retry.");
        }
    }

    public String getETA() {
        int retries = 3;
        while (retries-- > 0) {
            try {
                return etaService.getETA();
            } catch (Exception e) {
                log.warn("Retrying ETA, attempts left: " + retries);
            }
        }
        return "ETA unavailable";
    }

    public String getETAWithBackoff() {
        int retries = 3;
        int delay = 1;
        while (retries-- > 0) {
            try {
                return etaService.getETA();
            } catch (Exception e) {
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return "ETA unavailable";
                }
                delay *= 2;
            }
        }
        return "ETA unavailable";
    }
}

class PaymentService {
    private final ExternalPaymentApi externalPaymentApi = new ExternalPaymentApi();
    private final Logger log = new Logger();

    public String charge(String userId, double amount) {
        try {
            return externalPaymentApi.charge(userId, amount);
        } catch (RuntimeException ex) {
            return paymentFallback(userId, amount, ex);
        }
    }

    public String paymentFallback(String userId, double amount, Throwable throwable) {
        log.error("Payment Service Down. Fallback triggered.");
        return "PAYMENT_FAILED";
    }
}

class RecommendationRepository {
    public List<String> fetchLiveRecommendations(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be blank");
        }
        return List.of("movie-1", "movie-2");
    }
}

class RecommendationCache {
    public List<String> getCachedRecommendations(String userId) {
        return List.of("cached-movie-1", "cached-movie-2");
    }
}

class MenuService {
    public Menu fetchMenu(String restaurantId) {
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("restaurantId cannot be blank");
        }
        return new Menu("Menu for " + restaurantId);
    }
}

class PaymentProcessor {
    public void charge(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
    }
}

class OrderRetryQueue {
    private final Queue<Order> queue = new ArrayDeque<>();

    public void enqueue(Order order) {
        queue.add(order);
    }
}

class EtaService {
    public String getETA() {
        return "15 minutes";
    }
}

class ExternalPaymentApi {
    public String charge(String userId, double amount) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be blank");
        }
        return "CHARGED:" + userId + ":" + amount;
    }
}

class Menu {
    private final String description;

    public Menu(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}

class Order {
    private final String orderId;
    private final double amount;

    public Order(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }
}

class Logger {
    public void warn(String message) {
        System.out.println("WARN: " + message);
    }

    public void error(String message) {
        System.out.println("ERROR: " + message);
    }
}
