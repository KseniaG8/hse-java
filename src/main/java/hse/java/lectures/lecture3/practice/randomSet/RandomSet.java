package hse.java.lectures.lecture3.practice.randomSet;

public class RandomSet<T> {

    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.7;

    private Object[] data;
    private int size;

    private Entry<T>[] table;
    private int capacity;
    private int tableSize;

    private final Random random = new Random();

    public RandomSet() {
        data = new Object[INITIAL_CAPACITY];
        size = 0;

        capacity = INITIAL_CAPACITY;
        table = (Entry<T>[]) new Entry[capacity];
        tableSize = 0;
    }

    public boolean insert(T value) {
        if (contains(value)) {
            return false;
        }

        ensureDataCapacity();
        ensureTableCapacity();

        data[size] = value;
        put(value, size);

        size++;
        return true;
    }

    public boolean remove(T value) {
        int index = findIndex(value);
        if (index == -1) {
            return false;
        }

        int dataIndex = table[index].dataIndex;

        T lastElement = elementAt(size - 1);
        data[dataIndex] = lastElement;

        int lastTableIndex = findIndex(lastElement);
        table[lastTableIndex].dataIndex = dataIndex;

        data[size - 1] = null;
        size--;

        table[index].deleted = true;
        tableSize--;

        return true;
    }

    public boolean contains(T value) {
        return findIndex(value) != -1;
    }

    public T getRandom() {
        if (size == 0) {
            throw new EmptySetException("Set is empty");
        }

        int index = random.nextInt(size);
        return elementAt(index);
    }

    private T elementAt(int index) {
        return (T) data[index];
    }

    private void ensureDataCapacity() {
        if (size >= data.length) {
            Object[] newData = new Object[data.length * 2];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    private void ensureTableCapacity() {
        if ((double) tableSize / capacity >= LOAD_FACTOR) {
            rehash();
        }
    }

    private void rehash() {
        Entry<T>[] oldTable = table;
        capacity *= 2;
        table = (Entry<T>[]) new Entry[capacity];
        tableSize = 0;

        for (Entry<T> entry : oldTable) {
            if (entry != null && !entry.deleted) {
                put(entry.value, entry.dataIndex);
            }
        }
    }

    private void put(T value, int dataIndex) {
        int hash = hash(value);
        while (table[hash] != null && !table[hash].deleted) {
            hash = (hash + 1) % capacity;
        }
        table[hash] = new Entry<>(value, dataIndex);
        tableSize++;
    }

    private int findIndex(T value) {
        int hash = hash(value);
        int start = hash;

        while (table[hash] != null) {
            if (!table[hash].deleted && table[hash].value.equals(value)) {
                return hash;
            }
            hash = (hash + 1) % capacity;
            if (hash == start) {
                break;
            }
        }

        return -1;
    }

    private int hash(T value) {
        return (value == null ? 0 : Math.abs(value.hashCode())) % capacity;
    }

    private static class Entry<T> {
        T value;
        int dataIndex;
        boolean deleted;

        Entry(T value, int dataIndex) {
            this.value = value;
            this.dataIndex = dataIndex;
            this.deleted = false;
        }
    }

}