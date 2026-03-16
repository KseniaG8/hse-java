package hse.java.lectures.lecture6.tasks.synchronizer;

public class StreamingMonitor {
    private int currentId = 1;
    private final int writerCount;
    private final int tickPerWriter;
    private int[] totalTick;
    private boolean completed = false;

    public StreamingMonitor(int writerCount, int tickPerWriter) {
        this.writerCount = writerCount;
        this.tickPerWriter = tickPerWriter;
        this. totalTick = new int[writerCount+1];
    }

    public synchronized boolean waitingForTurn(int writerId) {
        if (completed) {
            return false;
        }

        if (totalTick[writerId] >= tickPerWriter) {
            return false;
        }

        while (writerId != currentId && !completed) {
            try {
                wait();

                if (completed) {
                    return false;
                }

                if (totalTick[writerId] >= tickPerWriter) {
                    return false;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (completed || totalTick[writerId] >= tickPerWriter) {
            return false;
        }

        return true;
    }

    public synchronized boolean tickCompleated(int writerId) {
        totalTick[writerId]++;
        
        if (completed) {
            return false;
        }

        currentId = (currentId % writerCount) + 1;

        boolean allWriterCompleated = true;

        for (int i = 1; i < writerCount; i++) {
            if(totalTick[i] < tickPerWriter) {
                allWriterCompleated = false;
                break;
            }
        }

        if (allWriterCompleated) {
            completed = true;
            notifyAll();
            return false;
        }

        notifyAll();
        return true;
    }

    public synchronized boolean isCompleted() {
        return completed;
    }
}
