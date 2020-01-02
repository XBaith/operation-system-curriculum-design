package edu.os.bankersalgorithm;

import javafx.scene.control.Alert;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class SafetyState {
    private final Logger logger = LogManager.getLogger(SafetyState.class);

    private ArrayList<Process> processes;
    private Map<String, Integer> available;
    private Map<String, Integer> work;
    private List<Process> safeSeq; //安全序列
    private ArrayList<Process> ps;

    public SafetyState(ArrayList<Process> processes, Map<String, Integer> available) {
        this.processes = processes;
        this.available = available;
        this.work = new HashMap<>(available);
        safeSeq = new LinkedList<>();
        ps = new ArrayList<>(processes);
    }

    /**
     * 对当前状态的进程序列试分配
     * @return  安全序列链表集合
     */
    public List<Process> run() {
        if(allFinish()){
           //reset finish
            for (Process process : processes) {
                boolean empty = true;
                for(Map.Entry<String, Integer> entry : process.need.entrySet()) {
                    if(entry.getValue() != 0)
                        empty = false;
                }
                if(empty) process.finish = false;
            }
        }
        int psCnt = 0;  //进程计数器
        while(!ps.isEmpty()) {  //每更新一个进程就从ps中删除，直到没有进程就说明全部更新完成
            //1.每次从头开始扫描找出 finish = false && Need < Wok的进程
            for (Process p : processes) {
                if (p.finish == false && compare(p.need, work)) {
                    //2.更新Work
                    for (Map.Entry<String, Integer> w : work.entrySet())
                        w.setValue(w.getValue() + p.allocation.get(w.getKey()));

                    p.finish = true;    //进程释放资源
                    ps.remove(p);   //删除更新完信息的进程
                    safeSeq.add(p); //放入安全序列
                    //写入更新进程的信息日志
                    logger.debug("更新进程 : " + p.getName() + ", Work = " + work);
                    //3.寻找下一个符合条件的进程，直至所有进程都更新完成
                    break;
                }
            }
            if(psCnt++ > processes.size())  break;  //如果遍历的次数超过了总的进程数，说明不存在还能更新状态的进程
        }
        return safeSeq;
    }

    private boolean allFinish() {
        for (Process process : processes) {
            if(!process.isFinish())
                return false;
        }
        return true;
    }

    /**
     * 比较资源smaller是否小于等于bigger
     * @param smaller   smaller资源Map
     * @param bigger    bigger资源Map
     * @return  是否小于等于
     */
    private boolean compare(Map<String, Integer> smaller, Map<String, Integer> bigger) {
        Iterator<Map.Entry<String, Integer>> smalItor = smaller.entrySet().iterator();
        Iterator<Map.Entry<String, Integer>> bigItor = bigger.entrySet().iterator();
        for(;smalItor.hasNext() && bigItor.hasNext();) {
            Map.Entry<String, Integer> smalEntry = smalItor.next();
            Map.Entry<String, Integer> bigEntry = bigItor.next();
            if(smalEntry.getValue().compareTo(bigEntry.getValue()) > 0)    //如果有一个small的值大于big的就返回false
                return false;
        }
        return true;
    }

    /**
     * 请求一个新的资源分配
     * @param pid   进程id
     * @param request   请求的资源map
     */
    public void request(String pid, Map<String, Integer> request) {
        if(processes.isEmpty() || available.isEmpty() || request == null)
            return;
        Integer id = Integer.valueOf(pid);  //进程id
        if(id < 0 || id > processes.size()) //检查请求进程id是否合法
            return;
        Process reqProcess =  processes.get(id);    //请求进程
        if(compare(request, reqProcess.need)) { //1.检查request <= need
            if (compare(request, available)) {  //2.检查request <= available
                //3.更新进程状态
                Iterator<Map.Entry<String, Integer>> allItor = reqProcess.allocation.entrySet().iterator();
                Iterator<Map.Entry<String, Integer>> avaItor = available.entrySet().iterator();
                Iterator<Map.Entry<String, Integer>> needItor = reqProcess.need.entrySet().iterator();
                Iterator<Map.Entry<String, Integer>> reqItor = request.entrySet().iterator();
                for (int p = 0; p < request.size(); p++) {
                    Map.Entry<String, Integer> reqEntry = reqItor.next();
                    Map.Entry<String, Integer> allEntry = allItor.next();
                    Map.Entry<String, Integer> avaEntry = avaItor.next();
                    Map.Entry<String, Integer> needEntry = needItor.next();

                    //need - request
                    needEntry.setValue(needEntry.getValue() - reqEntry.getValue());
                    //available - request
                    avaEntry.setValue(avaEntry.getValue() - reqEntry.getValue());
                    //allocation + request
                    allEntry.setValue(allEntry.getValue() + reqEntry.getValue());

                }
                //判断是否需要回收资源
                boolean res = true;
                for(Map.Entry<String, Integer> needEntry : reqProcess.need.entrySet()) {
                    if(needEntry.getValue() != 0)
                        res = false;
                }
                if(res) {   //回收资源
                    Iterator<Map.Entry<String, Integer>> aItor = available.entrySet().iterator();
                    Iterator<Map.Entry<String, Integer>> nItor = reqProcess.need.entrySet().iterator();
                    for (int p = 0; p < request.size(); p++) {
                        Map.Entry<String, Integer> avaEntry = aItor.next();
                        Map.Entry<String, Integer> needEntry = nItor.next();
                        avaEntry.setValue(avaEntry.getValue() + needEntry.getValue());
                    }
                }
                logger.debug("Need = " + reqProcess.need + "\nAvailable = " + available
                        + "\nAllocation = " + reqProcess.allocation);
            }else { //request > available
                BankerPane.displayAlert("进程: " + reqProcess.getName() + "　的请求资源大于系统可用的资源"
                        , Alert.AlertType.ERROR);
                throw new IllegalArgumentException("进程: " + reqProcess.getName() + "　的请求资源大于系统可用的资源");
            }
        }else { //request > need
            BankerPane.displayAlert("请求资源大于进程: " + reqProcess.getName() + "　所需的资源"
                    , Alert.AlertType.ERROR);
            throw new IllegalArgumentException("请求资源大于进程: " + reqProcess.getName() + "　所需的资源");
        }
    }

    /**
     * 获取安全序列
     * @return  安全序列
     */
    public List<Process> getSafeSeq() {
        return safeSeq;
    }
}
