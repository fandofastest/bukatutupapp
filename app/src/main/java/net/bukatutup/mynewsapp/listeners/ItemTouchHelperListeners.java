package net.bukatutup.mynewsapp.listeners;

/**
 * Created by Ashiq on 4/13/2017.
 */
public interface ItemTouchHelperListeners {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
