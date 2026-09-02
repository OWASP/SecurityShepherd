package org.owasp.mobileshepherd;

import java.util.ArrayList;
import java.util.List;

public class NavigationItem {
    private int id;
    private String title;
    private int iconResId;
    private boolean isExpandable;
    private boolean isExpanded;
    private List<NavigationItem> children;
    private int navigationId; // For navigation component

    public NavigationItem(int id, String title, int iconResId, int navigationId) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.navigationId = navigationId;
        this.isExpandable = false;
        this.isExpanded = false;
        this.children = new ArrayList<>();
    }

    public NavigationItem(int id, String title, int iconResId) {
        this(id, title, iconResId, -1);
        this.isExpandable = true;
    }

    public void addChild(NavigationItem child) {
        this.children.add(child);
        this.isExpandable = true;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<NavigationItem> getChildren() {
        return children;
    }

    public int getNavigationId() {
        return navigationId;
    }
}
