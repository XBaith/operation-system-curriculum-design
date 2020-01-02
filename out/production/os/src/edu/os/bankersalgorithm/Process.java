package edu.os.bankersalgorithm;

import javafx.scene.control.Alert;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 进程对象
 */
public class Process {
    private final Logger logger = LogManager.getLogger(Process.class);

    protected String name;
    protected Map<String, Integer> max;
    protected Map<String, Integer> allocation;
    protected Map<String, Integer> need;
    protected boolean finish;

    public Process(String name, Map<String, Integer> max, Map<String, Integer> allocation) {
        this.name = name;
        this.max = max;
        this.allocation = allocation;
        this.finish = true;
        need = new HashMap<>();

        //计算出need
        Iterator<Map.Entry<String, Integer>> maxItor = max.entrySet().iterator();
        Iterator<Map.Entry<String, Integer>> allocItor = allocation.entrySet().iterator();
        for(;maxItor.hasNext() && allocItor.hasNext();) {
            Map.Entry<String, Integer> maxEntry = maxItor.next();
            Map.Entry<String, Integer> allocEntry = allocItor.next();
            Map<String, Integer> total = BankerPane.getTotal();

            int n = maxEntry.getValue() - allocEntry.getValue();
            if(maxEntry.getValue() > total.get(maxEntry.getKey())) {    //检查max的输入是否合法
                String errorMsg ="进程: " + this.getName() + "资源: " + maxEntry.getKey()
                        +" 的Max值大于总共的资源数";
                        //弹出错误警告
                BankerPane.displayAlert(errorMsg
                        , Alert.AlertType.ERROR);
                cleanResources();
                logger.error(errorMsg);
                //抛出异常，中断代码
                throw new IllegalArgumentException(errorMsg);
            }

            if(n < 0) { //输入的Allocation > Max的错误资源
                String errorMsg = "进程: " + this.getName() + " 的所需值为负，分配资源错误";
                BankerPane.displayAlert(errorMsg
                        , Alert.AlertType.ERROR);
                cleanResources();
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            /*证明某资源的 need 一定小于 available
            available = total - sum(alloc) && total >= max
            => available >= max - sum(alloc)
            => available >= max - alloc && need = max - alloc
            => available >= need
            因此当保证max和alloc的正确性后，need一定小于available
            */

            //任何一个need不为0那么就不是完成状态
            if(n != 0)
                this.finish = false;
            need.put(maxEntry.getKey(), n);
        }
    }

    /*清理当前资源*/
    private void cleanResources() {
        //清空allocation, max, need中的值
        this.need.clear();
        this.allocation.clear();
        this.max.clear();
        if(!BankerPane.getAvailable().isEmpty())
            BankerPane.getAvailable().clear();
    }

    /*getters*/
    public String getName() {
        return name;
    }

    public Map<String, Integer> getMax() {
        return max;
    }

    public Map<String, Integer> getAllocation() {
        return allocation;
    }

    public Map<String, Integer> getNeed() {
        return need;
    }

    public boolean isFinish() {
        return finish;
    }
}
