package org.owasp.mobileshepherd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    private List<NavigationItem> items;
    private List<NavigationItem> displayedItems;
    private OnItemClickListener listener;
    private ProgressTracker progressTracker;
    private Map<Integer, FlagValidator.Module> navToModuleMap;

    public interface OnItemClickListener {
        void onItemClick(NavigationItem item);
    }

    public NavigationAdapter(List<NavigationItem> items, OnItemClickListener listener, 
                           ProgressTracker progressTracker, Map<Integer, FlagValidator.Module> navToModuleMap) {
        this.items = items;
        this.displayedItems = new ArrayList<>();
        this.listener = listener;
        this.progressTracker = progressTracker;
        this.navToModuleMap = navToModuleMap;
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems.clear();
        for (NavigationItem item : items) {
            addItemWithChildren(item, 0);
        }
    }
    
    public void updateItems(List<NavigationItem> newItems) {
        this.items = newItems;
        updateDisplayedItems();
        notifyDataSetChanged();
    }
    
    private void addItemWithChildren(NavigationItem item, int depth) {
        displayedItems.add(item);
        if (item.isExpandable() && item.isExpanded()) {
            for (NavigationItem child : item.getChildren()) {
                addItemWithChildren(child, depth + 1);
            }
        }
    }
    
    private int getItemDepth(NavigationItem item) {
        return getItemDepth(item, items, 0);
    }
    
    private int getItemDepth(NavigationItem target, List<NavigationItem> itemList, int currentDepth) {
        for (NavigationItem item : itemList) {
            if (item == target) {
                return currentDepth;
            }
            if (item.isExpandable()) {
                int childDepth = getItemDepth(target, item.getChildren(), currentDepth + 1);
                if (childDepth != -1) {
                    return childDepth;
                }
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nav_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NavigationItem item = displayedItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        private ImageView expandIcon;
        private TextView completedIndicator;
        private View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.nav_item_icon);
            title = itemView.findViewById(R.id.nav_item_title);
            expandIcon = itemView.findViewById(R.id.nav_item_expand_icon);
            completedIndicator = itemView.findViewById(R.id.nav_item_completed_indicator);
        }

        public void bind(NavigationItem item) {
            title.setText(item.getTitle());
            
            // Check if this item is completed
            boolean isCompleted = false;
            if (item.getNavigationId() != 0 && navToModuleMap != null) {
                FlagValidator.Module module = navToModuleMap.get(item.getNavigationId());
                if (module != null && progressTracker != null) {
                    isCompleted = progressTracker.isCompleted(module);
                }
            }
            
            // Show/hide completed indicator
            if (isCompleted && !item.isExpandable()) {
                completedIndicator.setVisibility(View.VISIBLE);
            } else {
                completedIndicator.setVisibility(View.GONE);
            }
            
            int depth = getItemDepth(item);
            int leftPadding = 16 + (depth * 32); // 16dp base, 32dp per level
            
            itemView.setPaddingRelative(
                leftPadding,
                itemView.getPaddingTop(),
                itemView.getPaddingEnd(),
                itemView.getPaddingBottom()
            );
            
            // Show icon only for top-level items (depth 0)
            if (depth == 0 && item.getIconResId() != 0) {
                icon.setImageResource(item.getIconResId());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
            
            // Show expand icon for expandable items at any level
            if (item.isExpandable()) {
                expandIcon.setVisibility(View.VISIBLE);
                expandIcon.setRotation(item.isExpanded() ? 180 : 0);
            } else {
                expandIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (item.isExpandable()) {
                    item.setExpanded(!item.isExpanded());
                    updateDisplayedItems();
                    notifyDataSetChanged();
                } else {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
