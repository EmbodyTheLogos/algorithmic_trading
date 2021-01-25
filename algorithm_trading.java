import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.Math;


interface IMedian<T extends Comparable<? super T>> {
    void add(T x);

    void remove(T x);

    T getMedian();

    void replace(T oldValue, T newValue);
}

class LazyPriorityQueue<E extends Comparable<? super E>> extends PriorityQueue<E> {
    private static final long serialVersionUID = 1L;
    private HashMap<E, Integer> pendingDeletes = new HashMap<>();
    private int pendingDeleteCount = 0; // The number of elements in the PriorityQueue that have been
    // marked for deletion

    public LazyPriorityQueue() {
        super();
    }

    public LazyPriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        super(initialCapacity, comparator);
    }

    /**
     * Add element to the heap
     *
     * @see java.util.PriorityQueue#add(java.lang.Object)
     */
    @Override
    public boolean add(E element) {
        // TODO: Add code to remove element from the pending delete map
        // If element is not a pending delete, then add it to the queue
        if (pendingDeletes.containsKey(element)) {
            int value = pendingDeletes.get(element);
            if (value == 1) {
                pendingDeletes.remove(element);
            } else {
                pendingDeletes.put(element, value - 1);
            }
            pendingDeleteCount -= 1;
        } else super.add(element);
        return true;
    }

    /**
     * Remove the object from the heap
     *
     * @see java.util.PriorityQueue#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object object) {
        // TODO Do not call super.remove(element), instead, simply record in the
        // pending deletes hashmap that there is one more element that needs to be
        // deleted. Also, make sure you update the total number of elements marked
        // for deletion.  Note that we always return true from remove
        @SuppressWarnings("unchecked")

        E element = (E) object;


        if (pendingDeletes.containsKey(element)) {
            pendingDeletes.put(element, pendingDeletes.get(element) + 1);
        } else pendingDeletes.put(element, 1);

        pendingDeleteCount += 1;

        return true;
    }

    /**
     * Return the number of elements in the heap
     *
     * @see java.util.PriorityQueue#size()
     */
    @Override
    public int size() {
        // TODO Update this method so that it works correctly, taken
        // into account the number of pending deletes
        //return super.size();
        return super.size() - pendingDeleteCount;
    }

    /**
     * Return the top of the heap
     *
     * @see java.util.AbstractQueue#element()
     */
    @Override
    public E element() {
        // TODO Update this method, so that it does not return an element
        // that has been lazy deleted
        removeHelper();
        return super.element();
    }

    /**
     * @see java.util.AbstractQueue#remove()
     */
    @Override
    public E remove() {
        // TODO Update this method, so that it does not return an element
        // that has been lazy deleted
        removeHelper();
        return super.remove();
    }

    public void removeHelper() {

        while (pendingDeletes.containsKey(super.element())) {
            int value = pendingDeletes.get(super.element());
            if (value == 1) {
                pendingDeletes.remove(super.element());
            } else {
                pendingDeletes.put(super.element(), value - 1);
            }
            pendingDeleteCount -= 1;
            super.remove();
        }
    }
}

class FastMedian<E extends Comparable<? super E>> implements IMedian<E> {
    private LazyPriorityQueue<E> minHeap = new LazyPriorityQueue<>();
    private LazyPriorityQueue<E> maxHeap = new LazyPriorityQueue<>(10,Collections.reverseOrder());
    private E median;

    @Override
    public void add(E x) {
        if(minHeap.size() == 0 && maxHeap.size()==0)
        {
            minHeap.add(x);
        }
        else if(x.compareTo(minHeap.element())<0)
        {
            maxHeap.add(x);
        }
        else minHeap.add(x);
    }

    @Override
    public void remove(E x) {

        if(x.compareTo(minHeap.element())<0)
        {
            maxHeap.remove(x);
        }else minHeap.remove(x);
    }


    @Override
    public E getMedian() {

        balanceHeap();
        if (minHeap.size() > maxHeap.size()) {
            return minHeap.element();
        } else return maxHeap.element();
    }

    @Override
    public void replace(E oldValue, E newValue) {
        remove(oldValue);
        if(newValue.compareTo(getMedian())>0)
        {
            minHeap.add(newValue);
        }
        else maxHeap.add(newValue);
    }

    public void balanceHeap()
    {
        while(Math.abs(minHeap.size()-maxHeap.size()) >=3)
        {
            if(minHeap.size()>maxHeap.size())
            {
                maxHeap.add(minHeap.remove());
            }
            else minHeap.add(maxHeap.remove());
        }
    }
}

interface IMedianQueue<E extends Comparable<? super E>> {
    boolean isEmpty();

    void enqueue(E x);

    E dequeue();

    E front();

    E back();

    E getMedian();

    void replace(E newVal);
}


class MedianQueue<E extends Comparable<? super E>> implements IMedianQueue<E> {

    ArrayDeque<E> list = new ArrayDeque<>();
    FastMedian<E> iMedian = new FastMedian<>();

    @Override
    public boolean isEmpty() {
        return (list.size() == 0);
    }

    @Override
    public void enqueue(E x) {

        iMedian.add(x);
        list.add(x);
    }

    @Override
    public E dequeue() {
        iMedian.remove(front());
        return list.removeFirst();
    }

    @Override
    public E front() {
        return list.getFirst();
    }

    @Override
    public E back() {
        return list.getLast();
    }

    @Override
    public E getMedian() {
        return iMedian.getMedian();
    }

    @Override
    public void replace(E newVal) {

        list.add(newVal);
        iMedian.replace(list.removeFirst(), newVal);

    }
}

class Solution {

    public static void main(String[] args) throws IOException {
        // I recommend that you use this approach for IO, rather than a Scanner for
        // speed reasons

        IMedianQueue<Integer> queue = new MedianQueue<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String[] tokens = reader.readLine().split(" ");
        int n = Integer.valueOf(tokens[0]);
        int m = Integer.valueOf(tokens[1]);

        // Collect your output before printing it out (for speed reasons)
        StringBuffer sbOut = new StringBuffer();

        for (int k = 0; k < m; k++) {
            queue.enqueue(Integer.valueOf(reader.readLine()));
        }

        if (queue.getMedian().equals(queue.back())) {
            sbOut.append("hold\n");
        } else if (queue.getMedian() > queue.back()) {
            sbOut.append("sell\n");
        } else sbOut.append("buy\n");

        for (int i = m; i < n; i++) {
            queue.replace(Integer.valueOf(reader.readLine()));

            if (queue.getMedian().equals(queue.back())) {
                sbOut.append("hold\n");
            } else if (queue.getMedian() > queue.back()) {
                sbOut.append("sell\n");
            } else sbOut.append("buy\n");
        }
        System.out.print(sbOut.toString());
    }
}