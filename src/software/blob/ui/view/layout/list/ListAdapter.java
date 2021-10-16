package software.blob.ui.view.layout.list;

import software.blob.ui.view.View;

/**
 * Used to generate views for a {@link ListView}
 */
public abstract class ListAdapter {

    protected ListView listView;

    /**
     * Get the total number of items in the list
     * @return Number of items
     */
    public abstract int getCount();

    /**
     * Get the item at the given position
     * @param position Position index
     * @return Item
     */
    public abstract Object getItem(int position);

    /**
     * Get/create the view at the given position
     * @param position Position index
     * @param existing Existing view (null if it needs to be created)
     * @param list List that is requesting this view
     * @return New or updated view
     */
    public abstract View getView(int position, View existing, ListView list);

    /**
     * Notify that the data in this adapter has been changed in some way
     */
    public void notifyDatasetChanged() {
        if (this.listView != null)
            this.listView.refresh();
    }

    /**
     * Set the list view to push refresh calls to
     * @param listView List view
     */
    protected void setListView(ListView listView) {
        this.listView = listView;
    }
}
