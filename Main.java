import java.io.*;

class Product {
    private final String id;
    private final String name;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class ProductRepository {
    public Product find(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be empty");
        }
        return new Product(productId, "Product " + productId);
    }
}

class ProductServiceFailFirst {
    private final ProductRepository productRepo = new ProductRepository();

    public Product getProduct(String productId) {
        if (productId == null)
            throw new IllegalArgumentException("Product ID cannot be null");
        // fail-fast for invalid input
        return productRepo.find(productId);
    }
}

// Search Product in any of the websites..
class ProductServiceFailSafe {
    private final ProductRepository productRepo = new ProductRepository();

    public Product getProduct(String productId) {
        try {
            return productRepo.find(productId);
        } catch (Exception e) {
            // fail-safe: return default
            return new Product("default", "Fallback Product");
        }
    }
}

class FileReaderExample {
    public void readFile(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath); // This may throw an IOException
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        System.out.println(line);
        bufferedReader.close();
    }

    public static void main(String[] args) {
        FileReaderExample example = new FileReaderExample();
        try {
            example.readFile("somefile.txt"); // Must handle the IOException
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
    }
}

// Custom Exception
class CustomerNotPlusException extends RuntimeException {
    public CustomerNotPlusException(String userId) {
        super("User " + userId + " is not a plus customer");
    }
}

class CourseService {
    public void accessCourse(String userId) {
        if (!hasAccess(userId)) {
            // Throwing the custom exception if the user doesn't have access
            throw new CustomerNotPlusException(userId);
        }
        // continue enrollment...
    }

    private boolean hasAccess(String userId) {
        // Logic to check if the user is a Plus customer from the database
        return false; // For the sake of this example, assume the user doesn't have access
    }
}
