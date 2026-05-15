package kr.ac.kopo.todayroutine.data;

public class Category {
    private int id;
    private String name;
    private boolean isExpanded;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
        this.isExpanded = false; // Default collapsed
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
