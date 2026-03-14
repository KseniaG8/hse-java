package hse.java.lectures.lecture6.tasks.queue;

public class BoundedBlockingQueue<T> {
    private final Object[] buffer;
    private int head = 0;
    private int tail = 0;
    private int count = 0;
    private final int capacity;

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity error");
        }
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Item error");
        }

        synchronized (this) {
            while (count == capacity) {
                wait();
            }

            buffer[tail] = item;
            tail = (tail + 1) % capacity;
            count++;

            notifyAll();
        }
    }

    public T take() throws InterruptedException {
        synchronized (this) {
            while (count == 0) {
                wait();
            }

            T item = (T) buffer[head];
            buffer[head] = null;
            head = (head + 1) % capacity;
            count--;

            notifyAll();
            return item;
        }
    }

    public int size() {
        return count;
    }

    public int capacity() {
        return capacity;
    }
}
