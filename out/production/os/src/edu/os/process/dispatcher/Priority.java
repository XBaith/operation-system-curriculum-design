package edu.os.process.dispatcher;

import edu.os.process.ProcessScheduling;

import java.util.Comparator;

/**
 * 优先级调度
 * 优先级数值越大，优先级越高，排序越靠前
 */
public class Priority implements Comparator<ProcessScheduling.Process> {
    @Override
    public int compare(ProcessScheduling.Process p1, ProcessScheduling.Process p2) {
        return p1.priority > p2.priority ?
                (p1.priority == p2.priority ? 0 : -1)
                : 1;
    }
}
