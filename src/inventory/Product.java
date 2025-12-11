package inventory;

public class Product {
    private int id;
    private String name;
    private String category;
    private String shade;   // numeric string like "001"
    private double price;
    private int noOfItems;

    public Product(int id, String name, String category, String shade,
                   double price, int noOfItems) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.shade = shade;
        this.price = price;
        this.noOfItems = noOfItems;
    }

    public Product(String name, String category, String shade,
                   double price, int noOfItems) {
        this(0, name, category, shade, price, noOfItems);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getShade() { return shade; }
    public void setShade(String shade) { this.shade = shade; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getNoOfItems() { return noOfItems; }
    public void setNoOfItems(int noOfItems) { this.noOfItems = noOfItems; }
}
