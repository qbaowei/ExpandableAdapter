package com.qbw.recyclerview.expandable;

import com.qbw.log.XLog;
import com.qbw.recyclerview.base.BaseExpandableAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bond on 2016/4/2.
 */
public abstract class ExpandableAdapter<T> extends BaseExpandableAdapter<T> {

    private List<T> mList;

    private int mHeaderCount;
    private int mChildCount;
    private int mGroupCount;
    private List<Integer> mGroupChildCount;
    private int mGroupAndGroupChildCount;
    private int mFooterCount;

    public ExpandableAdapter() {
        mList = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        int vt = getItemViewType(mList.get(position));
        return vt == -1 ? super.getItemViewType(position) : vt;
    }

    public int getItemViewType(T t) {
        return -1;
    }

    @Override
    public final T getItem(int itemPosition) {
        return mList.get(itemPosition);
    }

    public final int getItemPosition(T item) {
        return mList.indexOf(item);
    }

    public final void removeItem(int itemPosition) {
        if (!checkItemPosition(itemPosition)) {
            return;
        }
        if (mHeaderCount > 0 && itemPosition < mHeaderCount) {
            mHeaderCount--;
        } else if (mChildCount > 0 && itemPosition < mHeaderCount + mChildCount) {
            mChildCount--;
        } else if (mGroupCount > 0 && itemPosition < mHeaderCount + mChildCount + getGroupAndGroupChildCount()) {
            int groupPosition = getGroupPosition(itemPosition);
            if (groupPosition != -1) {
                removeGroup(groupPosition);
                return;
            } else {
                int[] groupChildPosition = getGroupChildPosition(itemPosition);
                removeGroupChild(groupChildPosition[0], groupChildPosition[1]);
                return;
            }
        } else if (mFooterCount > 0 && itemPosition >= mList.size() - mFooterCount) {
            mFooterCount--;
        } else {
            XLog.w("Remove item failed!");
            return;
        }
        mList.remove(itemPosition);
        notifyItemRemoved(itemPosition);
    }

    public final void swapItem(int sourcePosition, int targetPosition) {
        int itemCount = getItemCount();
        if (sourcePosition < 0 || sourcePosition >= itemCount) {
            XLog.e("Invalid sourcePosition %d", sourcePosition);
            return;
        } else if (targetPosition < 0 || targetPosition >= itemCount) {
            XLog.e("Invalid targetPosition %d", targetPosition);
            return;
        }
        Collections.swap(mList, sourcePosition, targetPosition);
        notifyItemMoved(sourcePosition, targetPosition);
    }

    public final void updateItem(int itemPosition, T item) {
        if (!checkItemPosition(itemPosition)) {
            return;
        }
        mList.set(itemPosition, item);
        notifyItemChanged(itemPosition);
    }

    public final void clear() {
        mList.clear();
        mHeaderCount = 0;
        mChildCount = 0;
        mGroupCount = 0;
        mGroupAndGroupChildCount = 0;
        if (mGroupChildCount != null) {
            mGroupChildCount.clear();
        }
        mFooterCount = 0;
        notifyDataSetChanged();
    }

    public final int addHeader(T header) {
        return addHeader(mHeaderCount, header, null);
    }

    public final int addHeader(int headerPosition, T header) {
        return addHeader(headerPosition, header, null);
    }

    public final int addHeader(List<T> headerList) {
        return addHeader(mHeaderCount, null, headerList);
    }

    public final int addHeader(int headerPosition, List<T> headerList) {
        return addHeader(headerPosition, null, headerList);
    }

    private final int addHeader(int headerPosition, T header, List<T> headerList) {
        if (headerPosition < 0) {
            XLog.e("Invalid header position %d", headerPosition);
            return -1;
        } else if (header == null && (headerList == null || headerList.isEmpty())) {
            XLog.e("Invalid header parameter");
            return -1;
        }
        if (headerPosition > mHeaderCount) {
            headerPosition = mHeaderCount;
        }
        int itemPosition = headerPosition;
        int itemAddSize;
        if (header != null) {
            mList.add(itemPosition, header);
            itemAddSize = 1;
        } else {
            mList.addAll(itemPosition, headerList);
            itemAddSize = headerList.size();
        }
        XLog.v("Notify item from %d, count is %d", itemPosition, itemAddSize);
        mHeaderCount += itemAddSize;
        notifyItemRangeInserted(itemPosition, itemAddSize);
        return headerPosition;
    }

    public final void removeHeader(T header) {
        int itemPosition = mList.indexOf(header);
        if (itemPosition == -1) {
            XLog.e("Remove header fiiled for not finding the header position");
            return;
        }
        mList.remove(itemPosition);
        mHeaderCount--;
        notifyItemRemoved(itemPosition);
    }

    public final void removeHeaders(List<T> headers) {
        int size = headers != null ? headers.size() : 0;
        for (int i = 0; i < size; i++) {
            removeHeader(headers.get(i));
        }
    }

    public final void removeHeader(int headerPosition) {
        removeHeader(headerPosition, 1);
    }

    public final void clearHeader() {
        removeHeader(0, mHeaderCount);
    }

    public final void clearHeader(int headerBeginPosition) {
        removeHeader(headerBeginPosition, mHeaderCount - headerBeginPosition);
    }

    public final void removeHeader(int headerBeginPosition, int removeCount) {
        if (!checkHeaderPosition(headerBeginPosition)) {
            return;
        } else if (removeCount <= 0) {
            XLog.e("Invalid header removeCount %d", removeCount);
            return;
        }
        int itemBeginPosition = headerBeginPosition;
        int itemEndPosition = headerBeginPosition + removeCount;
        if (itemEndPosition > mHeaderCount) {
            itemEndPosition = mHeaderCount;
            int oldRemoveCount = removeCount;
            removeCount = itemEndPosition - itemBeginPosition;
            XLog.i("Reset removeCount from %d to %d", oldRemoveCount, removeCount);
        }
        mList.subList(itemBeginPosition, itemEndPosition).clear();
        notifyItemRangeRemoved(itemBeginPosition, removeCount);
        mHeaderCount -= removeCount;
    }

    public final List<T> getHeaders() {
        if (mHeaderCount <= 0 || getItemCount() <= 0) {
            XLog.w("No header items");
            return null;
        }
        return new ArrayList<>(mList.subList(0, mHeaderCount));
    }

    public final T getHeader(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return null;
        }
        return mList.get(headerPosition);
    }


    public final void updateHeader(int headerPosition, T header) {
        if (!checkHeaderPosition(headerPosition)) {
            return;
        }
        mList.set(headerPosition, header);
        notifyItemChanged(headerPosition);
    }

    public final void notifyHeaderChanged(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return;
        }
        notifyItemChanged(headerPosition);
    }

    @Override
    public final int getHeaderCount() {
        return mHeaderCount;
    }

    private boolean checkHeaderPosition(int headerPosition) {
        if (headerPosition < 0) {
            XLog.w("Invalid header position %d", headerPosition);
            return false;
        } else if (headerPosition >= mHeaderCount) {
            XLog.w("Invalid header position %d, header size is %d", headerPosition, mHeaderCount);
            return false;
        }
        return true;
    }

    public final int getHeaderPosition(int itemPosition) {
        if (!checkItemPosition(itemPosition) || itemPosition >= mHeaderCount) {
            return -1;
        }
        return itemPosition;
    }

    public final int getHeaderPosition(T header) {
        return mList.indexOf(header);
    }

    public final int convertHeaderPosition(int headerPosition) {
        if (!checkHeaderPosition(headerPosition)) {
            return -1;
        }
        return headerPosition;
    }

    public final int addChild(T child) {
        return addChild(mChildCount, child, null);
    }

    public final int addChild(int childPosition, T child) {
        return addChild(childPosition, child, null);
    }

    public final int addChild(List<T> childList) {
        return addChild(mChildCount, null, childList);
    }

    public final int addChild(int childPosition, List<T> childList) {
        return addChild(childPosition, null, childList);
    }

    public final int addChild(int childPosition, T child, List<T> childList) {
        if (childPosition < 0) {
            XLog.e("Invalid child position %d", childPosition);
            return -1;
        } else if (null == child && (null == childList || childList.isEmpty())) {
            XLog.e("Invalid child parameter");
            return -1;
        }
        if (childPosition > mChildCount) {
            childPosition = mChildCount;
        }
        int itemPosition = mHeaderCount + childPosition;
        int addSize;
        if (child != null) {
            mList.add(itemPosition, child);
            addSize = 1;
        } else {
            mList.addAll(itemPosition, childList);
            addSize = childList.size();
        }
        XLog.v("Notify item from %d, count is %d", itemPosition, addSize);
        mChildCount += addSize;
        notifyItemRangeInserted(itemPosition, addSize);
        return childPosition;
    }

    public final void removeChild(int childPosition) {
        removeChild(childPosition, 1);
    }

    public final void removeChild(T child) {
        int itemPosition = indexOfChild(child);
        if (itemPosition == -1) {
            XLog.e("Remove the child failed for not finding the child position");
            return;
        }
        mList.remove(itemPosition);
        notifyItemRemoved(itemPosition);
        mChildCount--;
    }

    public final void removeChilds(List<T> childs) {
        int size = childs != null ? childs.size() : 0;
        for (int i = 0; i < size; i++) {
            removeChild(childs.get(i));
        }
    }

    public final void clearChild(int childBeginPosition) {
        removeChild(childBeginPosition, mChildCount - childBeginPosition);
    }

    public final void clearChild() {
        removeChild(0, mChildCount);
    }

    public final void removeChild(int childBeginPosition, int removeCount) {
        if (!checkChildPosition(childBeginPosition)) {
            return;
        } else if (removeCount <= 0) {
            XLog.e("Invalid child removeCount %d", removeCount);
            return;
        }
        int itemBeginPosition = convertChildPosition(childBeginPosition);
        int headerChildCount = mHeaderCount + mChildCount;
        int itemEndPosition = itemBeginPosition + removeCount;
        if (itemEndPosition > headerChildCount) {
            itemEndPosition = headerChildCount;
            int oldRemoveCount = removeCount;
            removeCount = itemEndPosition - itemBeginPosition;
            XLog.i("Reset child removeCount from %d to %d", oldRemoveCount, removeCount);
        }
        mList.subList(itemBeginPosition, itemEndPosition).clear();
        mChildCount -= removeCount;
        notifyItemRangeRemoved(itemBeginPosition, removeCount);
    }

    public final List<T> getChilds() {
        if (mChildCount <= 0 || getItemCount() <= 0) {
            XLog.w("No child items");
            return null;
        }
        return new ArrayList<>(mList.subList(mHeaderCount, mHeaderCount + mChildCount));
    }

    public final T getChild(int childPosition) {
        if (!checkChildPosition(childPosition)) {
            return null;
        }
        return mList.get(mHeaderCount + childPosition);
    }


    public final void updateChild(int childPosition, T child) {
        int itemPosition = convertChildPosition(childPosition);
        if (itemPosition == -1) {
            return;
        }
        mList.set(itemPosition, child);
        notifyItemChanged(itemPosition);
    }


    public final void notifyChildChanged(int childPosition) {
        int itemPosition = convertChildPosition(childPosition);
        if (itemPosition == -1) {
            return;
        }
        notifyItemChanged(itemPosition);
    }

    @Override
    public final int getChildCount() {
        return mChildCount;
    }

    private boolean checkChildPosition(int childPosition) {
        if (childPosition < 0) {
            XLog.w("Invalid child position %d", childPosition);
            return false;
        } else if (childPosition >= mChildCount) {
            XLog.w("invalid child position %d, child size is %d", childPosition, mChildCount);
            return false;
        }
        return true;
    }

    public final int getChildPosition(int itemPosition) {
        if (!checkItemPosition(itemPosition)) {
            XLog.e("invalid adapterPosition %d", itemPosition);
            return -1;
        } else if (mChildCount <= 0 || itemPosition >= mHeaderCount + mChildCount) {
            return -1;
        }
        return itemPosition - mHeaderCount;
    }

    public final int getChildPosition(T child) {
        int itemPosition = indexOfChild(child);
        if (itemPosition == -1) {
            return -1;
        }
        return itemPosition - mHeaderCount;
    }

    public final int convertChildPosition(int childPosition) {
        if (!checkChildPosition(childPosition)) {
            return -1;
        }
        return mHeaderCount + childPosition;
    }

    public final int indexOfChild(T child) {
        if (mChildCount <= 0) {
            return -1;
        }
        int itemPosition = -1;
        int itemBeginPosition = mHeaderCount;
        int itemEndPosition = itemBeginPosition + mChildCount;
        for (int i = itemBeginPosition; i < itemEndPosition; i++) {
            if (mList.get(i).equals(child)) {
                itemPosition = i;
                break;
            }
        }
        return itemPosition;
    }

    public final int addGroup(T group) {
        return addGroup(mGroupCount, group);
    }

    public final int addGroup(int groupPosition, T group) {
        if (groupPosition < 0) {
            XLog.e("Invalid group position %d", groupPosition);
            return -1;
        } else if (indexOfGroup(group) != -1) {
            XLog.e("Group is alread exist! You must use a different object to create a new group");
            return -1;
        }
        if (groupPosition > mGroupCount) {
            XLog.w("Reset group position from %d to %d", groupPosition, mGroupCount);
            groupPosition = mGroupCount;
        }
        int itemPosition = 0;
        for (int i = 0; i < groupPosition; i++) {
            itemPosition += mGroupChildCount.get(i) + 1;
        }
        itemPosition += mHeaderCount + mChildCount;
        mList.add(itemPosition, group);
        mGroupCount += 1;
        mGroupAndGroupChildCount += 1;
        if (mGroupChildCount == null) {
            mGroupChildCount = new ArrayList<>();
        }
        mGroupChildCount.add(groupPosition, 0);
        notifyItemInserted(itemPosition);
        return groupPosition;
    }

    public final void removeGroup(T group) {
        removeGroup(getGroupPosition(group));
    }

    public final void removeGroups(List<T> groups) {
        int size = groups != null ? groups.size() : 0;
        for (int i = 0; i < size; i++) {
            removeGroup(groups.get(i));
        }
    }

    public final void removeAllGroup() {
        int groupCount = mGroupCount;
        for (int i = 0; i < groupCount; i++) {
            removeGroup(0);
        }
    }

    public final void removeGroup(int groupPosition) {
        int itemPosition = convertGroupPosition(groupPosition);
        if (itemPosition == -1) {
            return;
        }
        int groupChildCount = mGroupChildCount.get(groupPosition);
        mList.subList(itemPosition, itemPosition + groupChildCount + 1).clear();
        mGroupCount--;
        mGroupAndGroupChildCount -= (1 + mGroupChildCount.get(groupPosition));
        mGroupChildCount.remove(groupPosition);
        notifyItemRangeRemoved(itemPosition, groupChildCount + 1);
    }

    public final List<T> getGroups() {
        if (mGroupCount <= 0) {
            return null;
        }
        List<T> groups = new ArrayList<>(mGroupCount);
        for (int i = 0; i < mGroupCount; i++) {
            groups.add(mList.get(convertGroupPosition(i)));
        }
        return groups;
    }


    public final T getGroup(int groupPosition) {
        int itemPosition = convertGroupPosition(groupPosition);
        if (itemPosition == -1) {
            return null;
        }
        return mList.get(itemPosition);
    }

    public final void updateGroup(int groupPosition, T group) {
        int itemPosition = convertGroupPosition(groupPosition);
        if (itemPosition == -1) {
            return;
        }
        mList.set(itemPosition, group);
        notifyItemChanged(itemPosition);
    }

    public final int getGroupPosition(int itemPosition) {
        if (!checkItemPosition(itemPosition) || itemPosition < mHeaderCount + mChildCount) {
            XLog.e("Invalid itemPosition %d", itemPosition);
            return -1;
        }
        int groupPosition = -1;
        for (int i = 0; i < mGroupCount; i++) {
            if (itemPosition == convertGroupPosition(i)) {
                groupPosition = i;
                break;
            }
        }
        return groupPosition;
    }

    public final int getGroupPosition(T group) {
        return getGroupPosition(indexOfGroup(group));
    }

    public final int convertGroupPosition(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            XLog.e("Invalid group position %d", groupPosition);
            return -1;
        }
        int itemPosition = 0;
        for (int i = 0; i < groupPosition; i++) {
            itemPosition += mGroupChildCount.get(i) + 1;
        }
        return mHeaderCount + mChildCount + itemPosition;
    }

    public final int indexOfGroup(T group) {
        if (group == null) {
            return -1;
        }
        int itemPosition = -1;
        int groupItemPosition;
        for (int i = 0; i < mGroupCount; i++) {
            groupItemPosition = convertGroupPosition(i);
            if (mList.get(groupItemPosition).equals(group)) {
                itemPosition = groupItemPosition;
                break;
            }
        }
        return itemPosition;
    }

    public final int notifyGroupChanged(int groupPosition) {
        return notifyGroupChanged(groupPosition, true);
    }

    public final int notifyGroupChanged(int groupPosition, boolean notNotifyGroup) {
        if (!checkGroupPosition(groupPosition)) {
            XLog.e("Invalid group position %d", groupPosition);
            return -1;
        }
        if (!notNotifyGroup) {
            notifyItemChanged(convertGroupPosition(groupPosition));
        }
        int gcc = getGroupChildCount(groupPosition);
        for (int i = 0; i < gcc; i++) {
            notifyGroupChildChanged(groupPosition, i);
        }
        return 0;
    }

    @Override
    public final int getGroupCount() {
        return mGroupCount;
    }

    private boolean checkGroupPosition(int groupPosition) {
        if (groupPosition < 0) {
            XLog.w("Invalid group position %d", groupPosition);
            return false;
        } else if (groupPosition >= mGroupCount) {
            XLog.w("Invalid group position %d, group size is %d", groupPosition, mGroupCount);
            return false;
        }
        return true;
    }

    public final int[] addGroupChild(int groupPosition, T groupChild) {
        if (!checkGroupPosition(groupPosition)) {
            return new int[]{-1, -1};
        }
        return addGroupChild(groupPosition, mGroupChildCount.get(groupPosition), groupChild, null);
    }

    public final int[] addGroupChild(int groupPosition, int groupChildPosition, T groupChild) {
        return addGroupChild(groupPosition, groupChildPosition, groupChild, null);
    }

    public final int[] addGroupChild(int groupPosition, List<T> childList) {
        if (!checkGroupPosition(groupPosition)) {
            return new int[]{-1, -1};
        }
        return addGroupChild(groupPosition, mGroupChildCount.get(groupPosition), null, childList);
    }

    public final int[] addGroupChild(int groupPosition,
                                     int groupChildPosition,
                                     T groupChild,
                                     List<T> groupChildList) {
        if (!checkGroupPosition(groupPosition)) {
            return new int[]{-1, -1};
        } else if (groupChild == null && (groupChildList == null || groupChildList.isEmpty())) {
            XLog.e("Invalid group child mList");
            return new int[]{-1, -1};
        } else if (groupChildPosition < 0) {
            XLog.e("Invalid child position %d", groupChildPosition);
            return new int[]{-1, -1};
        }
        final int oldGroupChildCount = mGroupChildCount.get(groupPosition);
        if (groupChildPosition > oldGroupChildCount) {
            groupChildPosition = oldGroupChildCount;
        }
        int itemPosition = 0;
        for (int i = 0; i < groupPosition; i++) {
            itemPosition += mGroupChildCount.get(i) + 1;
        }
        itemPosition += mHeaderCount + mChildCount + 1 + groupChildPosition;
        int addSize;
        if (groupChild != null) {
            mList.add(itemPosition, groupChild);
            addSize = 1;
        } else {
            mList.addAll(itemPosition, groupChildList);
            addSize = groupChildList.size();
        }
        mGroupChildCount.set(groupPosition, oldGroupChildCount + addSize);
        mGroupAndGroupChildCount += addSize;
        notifyItemRangeInserted(itemPosition, addSize);
        return new int[]{groupPosition, groupChildPosition};
    }

    public final void removeGroupChild(int groupPosition, int groupChildPosition) {
        removeGroupChild(groupPosition, groupChildPosition, 1);
    }

    public final void clearGroupChild(int groupPosition) {
        clearGroupChild(groupPosition, 0);
    }

    public final void clearGroupChild(int groupPosition, int groupChildBeginPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return;
        }
        removeGroupChild(groupPosition,
                groupChildBeginPosition,
                mGroupChildCount.get(groupPosition) - groupChildBeginPosition);
    }

    private final void removeGroupChild(int groupPosition,
                                        int groupChildBeingPosition,
                                        int removeCount) {
        if (!checkGroupChildPosition(groupPosition, groupChildBeingPosition)) {
            return;
        }
        if (removeCount <= 0) {
            XLog.e("Invalid group remove count %d", removeCount);
            return;
        }
        final int groupChildCount = mGroupChildCount.get(groupPosition);
        int groupChildEnd = groupChildBeingPosition + removeCount;
        if (groupChildEnd > groupChildCount) {
            int oldRemoveCount = removeCount;
            removeCount = groupChildCount - groupChildBeingPosition;
            groupChildEnd = groupChildCount;
            XLog.i("Reset group removeCount from %d to %d", oldRemoveCount, removeCount);
        }
        XLog.d("groupPosition=%d, childStarPosition=%d, count=%d, childEnd=%d",
                groupPosition,
                groupChildBeingPosition,
                removeCount,
                groupChildEnd);
        int itemPosition = convertGroupPosition(groupPosition);
        int itemBeginPosition = itemPosition + groupChildBeingPosition + 1;
        mList.subList(itemBeginPosition, itemBeginPosition + removeCount).clear();
        mGroupChildCount.set(groupPosition, groupChildCount - removeCount);
        mGroupAndGroupChildCount -= removeCount;
        notifyItemRangeRemoved(itemBeginPosition, removeCount);
    }

    public final List<T> getGroupChilds(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return null;
        }
        int groupChildCount = mGroupChildCount.get(groupPosition);
        if (groupChildCount <= 0) {
            return null;
        }
        int itemPosition = convertGroupPosition(groupPosition);
        List<T> groupChilds = new ArrayList<>(groupChildCount);
        for (int i = 0; i < groupChildCount; i++) {
            groupChilds.add(mList.get(itemPosition + 1 + i));
        }
        return groupChilds;
    }

    public final T getGroupChild(int groupPosition, int groupChildPosition) {
        if (!checkGroupChildPosition(groupPosition, groupChildPosition)) {
            return null;
        }
        int itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition);
        if (itemPosition == -1) {
            return null;
        }
        return mList.get(itemPosition);
    }


    public final void updateGroupChild(int groupPosition, int groupChildPosition, T groupChild) {
        if (!checkGroupChildPosition(groupPosition, groupChildPosition)) {
            return;
        }
        int itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition);
        if (itemPosition == -1) {
            return;
        }
        mList.set(itemPosition, groupChild);
        notifyItemChanged(itemPosition);
    }

    public final void updateGroupChild(int groupPosition, int groupChildPosition) {
        int itemPosition = convertGroupChildPosition(groupPosition, groupChildPosition);
        if (itemPosition != -1) {
            notifyItemChanged(itemPosition);
        }
    }

    @Override
    public final int getGroupChildCount(int groupPosition) {
        if (!checkGroupPosition(groupPosition)) {
            return 0;
        }
        return mGroupChildCount.get(groupPosition);
    }

    public final int getGroupAndGroupChildCount() {
        return mGroupAndGroupChildCount;
    }

    public final void notifyGroupChildChanged(int groupPosition, int childPosition) {
        int itemPosition = convertGroupChildPosition(groupPosition, childPosition);
        if (itemPosition == -1) {
            return;
        }
        notifyItemChanged(itemPosition);
    }

    private boolean checkGroupChildPosition(int groupPosition, int groupChildPosition) {
        int groupChildCount;
        if (groupChildPosition < 0) {
            XLog.w("Invalid group child position %d, %d", groupPosition, groupChildPosition);
            return false;
        } else if (!checkGroupPosition(groupPosition)) {
            return false;
        } else if (groupChildPosition >= (groupChildCount = mGroupChildCount.get(groupPosition))) {
            XLog.w("Invalid group child position %d, %d, group %d child size is %d",
                    groupPosition,
                    groupChildPosition,
                    groupPosition,
                    groupChildCount);
            return false;
        }
        return true;
    }

    public final int[] getGroupChildPosition(int itemPosition) {
        int[] groupChildPosition = new int[]{-1, -1};
        if (!checkItemPosition(itemPosition)) {
            XLog.e("Invalid item position %d", itemPosition);
            return groupChildPosition;
        }
        if (mGroupCount > 0) {
            int groupItemPosition = mHeaderCount + mChildCount;
            if (itemPosition <= groupItemPosition) {
                return groupChildPosition;
            }
            int groupChildCount;
            for (int i = 0; i < mGroupCount; i++) {
                groupChildCount = mGroupChildCount.get(i);
                if (itemPosition > groupItemPosition && itemPosition <= groupItemPosition + groupChildCount) {
                    for (int j = 0; j < groupChildCount; j++) {
                        groupItemPosition += 1;
                        if (groupItemPosition == itemPosition) {
                            groupChildPosition[0] = i;
                            groupChildPosition[1] = j;
                            break;
                        }
                    }
                } else {
                    groupItemPosition += groupChildCount;
                }
                groupItemPosition++;
            }
        }
        return groupChildPosition;
    }

    public final int[] getGroupChildPosition(T groupChild) {
        return getGroupChildPosition(indexOfGroupChild(groupChild));
    }

    public final int indexOfGroupChild(T groupChild) {
        if (groupChild == null || mGroupCount == 0) {
            return -1;
        }
        int itemPosition = -1;
        int itemCount = getItemCount();
        int itemBeginPosition = mHeaderCount + mChildCount;
        int itemEndPosition = itemCount - mFooterCount;
        for (int i = itemBeginPosition; i < itemEndPosition; i++) {
            if (mList.get(i).equals(groupChild)) {
                itemPosition = i;
                break;
            }
        }
        return itemPosition;
    }

    public final int convertGroupChildPosition(int groupPosition, int childPosition) {
        if (!checkGroupChildPosition(groupPosition, childPosition)) {
            return -1;
        }
        return convertGroupPosition(groupPosition) + 1 + childPosition;
    }

    public final void addFooter(T footer) {
        addFooter(mFooterCount, footer, null);
    }

    public final void addFooter(List<T> footerList) {
        addFooter(mFooterCount, null, footerList);
    }

    public final void addFooter(int footerPosition, T footer) {
        addFooter(footerPosition, footer, null);
    }

    public final int addFooter(int footerPosition, T footer, List<T> footerList) {
        if (footerPosition < 0) {
            XLog.e("Invalid footer position %d", footerPosition);
            return -1;
        } else if (footer == null && (null == footerList || footerList.isEmpty())) {
            XLog.e("Wrong footer param");
            return -1;
        }
        int oldFooterCount = mFooterCount;
        if (footerPosition > oldFooterCount) {
            footerPosition = oldFooterCount;
        }
        int itemPosition = mHeaderCount + mChildCount + mGroupAndGroupChildCount + footerPosition;
        int addSize;
        if (footer != null) {
            mList.add(itemPosition, footer);
            addSize = 1;
        } else {
            mList.addAll(itemPosition, footerList);
            addSize = footerList.size();
        }
        XLog.v("Notify item from %d, count is %d", itemPosition, addSize);
        mFooterCount += addSize;
        notifyItemRangeInserted(itemPosition, addSize);
        return footerPosition;
    }

    public final void removeFooter(T footer) {
        int itemPosition = indexOfFooter(footer);
        if (itemPosition == -1) {
            return;
        }
        mList.remove(itemPosition);
        notifyItemRemoved(itemPosition);
        mFooterCount--;
    }

    public final void removeFooters(List<T> footers) {
        int size = footers != null ? footers.size() : 0;
        for (int i = 0; i < size; i++) {
            removeFooter(footers.get(i));
        }
    }

    public final void removeFooter(int footerPosition) {
        removeFooter(footerPosition, 1);
    }

    public final void clearFooter() {
        clearFooter(0);
    }

    public final void clearFooter(int footerBeginPosition) {
        removeFooter(footerBeginPosition, mFooterCount - footerBeginPosition);
    }

    public final void removeFooter(int footerBeginPosition, int removeCount) {
        if (!checkFooterPosition(footerBeginPosition)) {
            return;
        }
        int footerItemBeginPosition = convertFooterPosition(footerBeginPosition);
        int footerItemEndPosition = footerItemBeginPosition + removeCount;
        if (footerItemEndPosition > mList.size()) {
            footerItemEndPosition = mList.size();
            int oldRemoveCount = removeCount;
            removeCount = footerItemEndPosition - footerItemBeginPosition;
            XLog.i("Reset removeCount from %d to %d", oldRemoveCount, removeCount);
        }
        mList.subList(footerItemBeginPosition, footerItemEndPosition).clear();
        mFooterCount -= removeCount;
        notifyItemRangeRemoved(footerItemBeginPosition, removeCount);
    }

    public final List<T> getFooters() {
        int footerItemBeginPosition = convertFooterPosition(0);
        if (footerItemBeginPosition == -1) {
            return null;
        }
        return new ArrayList<>(mList.subList(footerItemBeginPosition,
                footerItemBeginPosition + mFooterCount));
    }


    public final T getFooter(int footerPosition) {
        int itemPosition = convertFooterPosition(footerPosition);
        if (itemPosition == -1) {
            return null;
        }
        return mList.get(itemPosition);
    }

    public final void updateFooter(int footerPosition, T footer) {
        int itemPosition = convertFooterPosition(footerPosition);
        if (itemPosition == -1) {
            return;
        }
        mList.set(itemPosition, footer);
        notifyItemChanged(itemPosition);
    }

    @Override
    public final int getFooterCount() {
        return mFooterCount;
    }

    private boolean checkFooterPosition(int footerPosition) {
        if (footerPosition < 0) {
            XLog.w("Invalid footer position %d", footerPosition);
            return false;
        } else if (footerPosition >= mFooterCount) {
            XLog.w("Invalid footer position %d, footer size is %d", footerPosition, mFooterCount);
            return false;
        }
        return true;
    }

    public final int getFooterPosition(int itemPosition) {
        if (!checkItemPosition(itemPosition)) {
            XLog.e("Invalid item position %d", itemPosition);
            return -1;
        } else if (mFooterCount <= 0) {
            return -1;
        } else if (itemPosition < mList.size() - mFooterCount) {
            return -1;
        }
        return mFooterCount - (mList.size() - itemPosition);
    }

    public final int getFooterPosition(T footer) {
        return getFooterPosition(indexOfFooter(footer));
    }

    private int indexOfFooter(T footer) {
        if (footer == null) {
            return -1;
        }
        int itemPosition = -1;
        int footerItemBeginPosition = convertFooterPosition(0);
        int itemCount = getItemCount();
        for (int i = footerItemBeginPosition; i < itemCount; i++) {
            if (mList.get(i).equals(footer)) {
                itemPosition = i;
                break;
            }
        }
        return itemPosition;
    }

    public final int convertFooterPosition(int footerPosition) {
        if (!checkFooterPosition(footerPosition)) {
            return -1;
        }
        return mHeaderCount + mChildCount + mGroupAndGroupChildCount + footerPosition;
    }

    @Override
    public final int getItemCount() {
        return mList.size();
    }

    private boolean checkItemPosition(int itemPosition) {
        int itemCount = getItemCount();
        if (itemPosition < 0 || itemPosition >= itemCount) {
            XLog.e("Invalid itemPosition %d, item count is %d", itemPosition, itemCount);
            return false;
        }
        return true;
    }

    public final int getHeaderPositionByViewType(int viewType) {
        int p = -1;
        T header;
        for (int i = 0; i < mHeaderCount; i++) {
            header = getHeader(i);
            if (viewType == getItemViewType(header) || viewType == getItemViewType(getItemPosition(
                    header))) {
                p = i;
                break;
            }
        }
        return p;
    }

    public final int getLastHeaderPositionByViewType(int viewType) {
        int p = -1;
        T header;
        for (int i = mHeaderCount - 1; i >= 0; i--) {
            header = getHeader(i);
            if (viewType == getItemViewType(header) || viewType == getItemViewType(getItemPosition(
                    header))) {
                p = i;
                break;
            }
        }
        return p;
    }

    public final void removeHeaderByViewType(int viewType) {
        int p = getHeaderPositionByViewType(viewType);
        if (p != -1) {
            removeHeader(p);
        } else {
            XLog.w("No header's viewType is %d", viewType);
        }
    }

    public final int getChildPositionByViewType(int viewType) {
        int p = -1;
        if (mChildCount <= 0) {
            return p;
        }
        int childItemPosition = mHeaderCount;
        for (int i = 0; i < mChildCount; i++) {
            if (viewType == getItemViewType(childItemPosition)) {
                p = i;
                break;
            }
            childItemPosition++;
        }
        return p;
    }

    public final int getLastChildPositionByViewType(int viewType) {
        int p = -1;
        if (mChildCount <= 0) {
            return p;
        }
        int childItemPosition = mHeaderCount + mChildCount - 1;
        for (int i = mChildCount - 1; i >= 0; i--) {
            if (viewType == getItemViewType(childItemPosition)) {
                p = i;
                break;
            }
            childItemPosition--;
        }
        return p;
    }

    public final void removeChildByViewType(int viewType) {
        int p = getChildPositionByViewType(viewType);
        if (p != -1) {
            removeChild(p);
        } else {
            XLog.w("No child's viewType is %d", viewType);
        }
    }

    public final int getGroupPositionByViewType(int viewType) {
        int p = -1;
        if (mGroupCount <= 0) {
            return p;
        }
        int groupItemPosition = mHeaderCount + mChildCount;
        for (int i = 0; i < mGroupCount; i++) {
            if (viewType == getItemViewType(groupItemPosition)) {
                p = i;
                break;
            }
            groupItemPosition += mGroupChildCount.get(i) + 1;
        }
        return p;
    }

    public final int getLastGroupPositionByViewType(int viewType) {
        int p = -1;
        if (mGroupCount <= 0) {
            return p;
        }
        int groupItemPosition = convertGroupPosition(mGroupCount - 1);
        for (int i = mGroupCount - 1; i >= 0; i--) {
            if (viewType == getItemViewType(groupItemPosition)) {
                p = i;
                break;
            }
            if (i - 1 >= 0) {
                groupItemPosition -= mGroupChildCount.get(i - 1) + 1;
            }
        }
        return p;
    }

    public final void removeGroupByViewType(int viewType) {
        int gpos = getGroupPositionByViewType(viewType);
        if (-1 != gpos) {
            removeGroup(gpos);
        } else {
            XLog.w("no group's viewType is %d", viewType);
        }
    }

    public final int getFooterPositionByViewType(int viewType) {
        int p = -1;
        if (mFooterCount <= 0) {
            return p;
        }
        int footerItemPosition = mList.size() - mFooterCount;
        for (int i = 0; i < mFooterCount; i++) {
            if (viewType == getItemViewType(footerItemPosition)) {
                p = i;
                break;
            }
            footerItemPosition++;
        }
        return p;
    }

    public final int getLastFooterPositionByViewType(int viewType) {
        int p = -1;
        int footerItemPosition = mList.size() - 1;
        for (int i = mFooterCount - 1; i >= 0; i--) {
            if (viewType == getItemViewType(footerItemPosition)) {
                p = i;
                break;
            }
            footerItemPosition--;
        }
        return p;
    }

    public final void removeFooterByViewType(int viewType) {
        int p = getFooterPositionByViewType(viewType);
        if (p != -1) {
            removeFooter(p);
        } else {
            XLog.w("no footer's viewType is %d", viewType);
        }
    }

    public int getHeaderPosition(int currViewType, List<Integer> headerListViewTypes) {
        return getPosition(0, headerListViewTypes, currViewType);
    }

    public int getChildPosition(int currViewType, List<Integer> childListViewTypes) {
        return getPosition(1, childListViewTypes, currViewType);
    }

    public int getGroupPosition(int currViewType, List<Integer> groupListViewTypes) {
        return getPosition(2, groupListViewTypes, currViewType);
    }

    public int getFooterPosition(int currViewType, List<Integer> footerListViewTypes) {
        return getPosition(3, footerListViewTypes, currViewType);
    }

    /**
     * @param type         0,header;1,child;2,group,3,footer
     * @param viewTypes    constrainted viewtype list
     * @param currViewType the viewType which you need to calculate the right position
     */
    private int getPosition(int type, List<Integer> viewTypes, int currViewType) {
        if (viewTypes == null || viewTypes.isEmpty()) {
            XLog.w("Please call method setXXXViewTypePositionConstraints");
            return -1;
        }
        int currViewTypePosition = viewTypes.indexOf(currViewType);
        if (currViewTypePosition == -1) {
            XLog.w("ViewType %d not find in XXX constraint viewType list");
            return -1;
        }
        if (currViewTypePosition == 0) {
            return 0;
        }
        int tmp;
        int targetPos = -1;
        for (int i = currViewTypePosition - 1; i >= 0; i--) {
            switch (type) {
                case 0:
                    tmp = getLastHeaderPositionByViewType(viewTypes.get(i));
                    break;
                case 1:
                    tmp = getLastChildPositionByViewType(viewTypes.get(i));
                    break;
                case 2:
                    tmp = getLastGroupPositionByViewType(viewTypes.get(i));
                    break;
                case 3:
                    tmp = getLastFooterPositionByViewType(viewTypes.get(i));
                    break;
                default:
                    return -1;
            }
            if (tmp >= 0) {
                targetPos = tmp + 1;
                break;
            }
        }
        if (targetPos == -1) {
            targetPos = 0;
        }
        return targetPos;
    }
}
