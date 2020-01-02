package edu.os.bankersalgorithm;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class BankerPane extends Application {
    private final Logger logger = LogManager.getLogger(BankerPane.class);

    private BorderPane border;  //基础面板
    private GridPane gridPane;  //表格面板
    private static final int SCENE_WIDTH = 800; //画布宽度
    private static final int SCENE_HEIGHT = 335;    //画布高度
    private static final double RES_TEXT_WIDTH = 50.0;  //资源输入文本框的最小宽度

    private int procNum;    //进程数
    private int resNum;    //资源数
    private static String[] RES;    //资源名
    private static final int MAX_PROC = 20; //最大进程数
    private static final int MAX_RES = 20;  //最大资源数
    private static final Map<String, Integer> total = new HashMap<>();  //总共的资源数
    private static final Map<String, Integer> available = new HashMap<>();  //剩余可分配资源
    private final ArrayList<Process> processes = new ArrayList<>();   //进程集合

    public static Map<String, Integer> getTotal() {
        return total;
    }

    public static Map<String, Integer> getAvailable() {
        return available;
    }

    /**
     * 构建输入资源数和进程数的表单
     * @param gridPane  表格面板
     */
    public void setUpProcess(GridPane gridPane) {
        logger.debug("构建输入资源数和进程数的表单");

        Label procLab = new Label("进程数:");
        TextField procText = new TextField();
        gridPane.add(procLab, 0, 0);
        gridPane.add(procText, 1, 0);

        Label resLab = new Label("资源种类数:");
        TextField resText = new TextField();
        gridPane.add(resLab, 0, 1);
        gridPane.add(resText, 1, 1);

        Button submit = new Button("提交");
        submit.setId("sub-button");
        HBox subBox = new HBox();

        subBox.setAlignment(Pos.CENTER_RIGHT);
        subBox.getChildren().add(submit);
        gridPane.add(subBox, 1, 2);

        submit.setOnAction(actionEvent -> {
            if(procText.getText().trim().isEmpty() || resText.getText().trim().isEmpty())
                return;

            procNum = Integer.valueOf(procText.getText());
            resNum = Integer.valueOf(resText.getText());
            if(procNum <= 0 || resNum <= 0) {
                String errMsg = "输入的进程数或资源数不能为负";
                displayAlert(errMsg, Alert.AlertType.ERROR);
                logger.error(errMsg);
                throw new IllegalArgumentException(errMsg);
            } else if(procNum > MAX_PROC || resNum > MAX_RES) {
                String errMsg = "输入的进程数或资源数不能太大";
                displayAlert(errMsg, Alert.AlertType.ERROR);
                logger.error(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
            RES = new String[resNum];
            for(int i = 0; i < resNum; i++) {
                RES[i] = String.valueOf((char) (65 + i));
            }
            setUpDetail();
        });

    }

    /**
     * 输入详细详细信息
     */
    private void setUpDetail() {
        if (procNum == 0 || resNum == 0){
            try {
                throw new IllegalAccessException("No Processes or Resources!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        logger.debug("构建输入详细详细信息界面");

        //输入各类资源总数
        HBox sumBox = new HBox();
        sumBox.setAlignment(Pos.CENTER);
        Label sumLab = new Label("各类资源总数 : ");
        sumBox.getChildren().add(sumLab);
        TextField[] sumTexts = new TextField[resNum];   //各个资源总数
        for(int t = 0; t < resNum; t++){
            sumTexts[t] = new TextField();
            sumTexts[t].setMaxWidth(RES_TEXT_WIDTH);
            Label resLab = new Label( " " + RES[t]+" : ");
            sumBox.getChildren().add(resLab);
            sumBox.getChildren().add(sumTexts[t]);
        }

        //输入每个进程的详细资源信息
        GridPane detailGrid = new GridPane();   //进程详细信息表单
        detailGrid.setAlignment(Pos.CENTER);
        detailGrid.setHgap(10);detailGrid.setVgap(10);
        Label maxLab = new Label("Max");    //最大资源需求数标签
        Label allocLab = new Label("Allocation");   //已占用标签
        detailGrid.add(maxLab, 1, 0);
        detailGrid.add(allocLab, 2, 0);

        Label[] procLabs = new Label[procNum];  //进程名标签
        HBox[] maxBox = new HBox[procNum];  //最大需求Box
        HBox[] allocBox = new HBox[procNum];    //已分配资源Box

        for(int i = 0; i < procNum; i++) {
            procLabs[i] = new Label("P:" + i);
            detailGrid.add(procLabs[i], 0, i + 1);
            maxBox[i] = new HBox();maxBox[i].setId("maxBox");
            allocBox[i] = new HBox();allocBox[i].setId("allocBox");

            TextField[] maxTexts = new TextField[resNum];
            TextField[] allocTexts = new TextField[resNum];
            for(int t = 0; t < resNum; t++) {
                maxTexts[t] = new TextField();
                maxTexts[t].setMaxWidth(RES_TEXT_WIDTH);
                allocTexts[t] = new TextField();
                allocTexts[t].setMaxWidth(RES_TEXT_WIDTH);
                allocTexts[t].setId("allocText");
                maxBox[i].getChildren().add(maxTexts[t]);
                allocBox[i].getChildren().add(allocTexts[t]);
            }
            //将带有输入文本域的HBox放入对应的表格中
            detailGrid.add(maxBox[i], 1, i + 1);
            detailGrid.add(allocBox[i], 2, i + 1);
        }
        //添加提交按钮
        HBox subBox = new HBox();
        subBox.setAlignment(Pos.CENTER);
        Button submitBtn = new Button("提交详细信息");
        //提交各进程详细资源信息
        submitBtn.setOnAction(action ->  {
            //收集所有表单信息，创建进程
            TextField[] maxTexts = listTexts(maxBox);
            TextField[] allocTexts = listTexts(allocBox);
            if(!isAllFill(sumTexts, maxTexts, allocTexts)) {
                return;
            }
            //计算剩余可分配的资源
            calcuAvailable(sumTexts);
            //创建进程
            createProcess(RES, maxTexts, allocTexts);
            //计算并展示结果
            displayTable();
        });
        subBox.getChildren().add(submitBtn);
        border.setBottom(subBox);
        //更新输入面板
        border.setTop(sumBox);
        border.setCenter(detailGrid);
    }

    /**
     * 列出带有输入文本域HBox内的所有文本域对象
     * @param boxes HBox
     * @return  文本域对象
     */
    private TextField[] listTexts(HBox[] boxes) {
        TextField[] texts = new TextField[resNum * procNum];
        for (int x = 0; x < boxes.length; x++) {
            for(int y = 0;y < resNum; y++) {
                texts[x * resNum + y] =(TextField) boxes[x].getChildren().get(y);
            }
        }
        return texts;
    }

    /**
     * 计算可分配的资源
     * @param sumTexts
     * @return
     */
    private void calcuAvailable(TextField[] sumTexts) {
        if(available.isEmpty())
            return ;
        int r = 0;
        for (Map.Entry<String, Integer> entry : available.entrySet()) {
            Integer res = Integer.valueOf(sumTexts[r++].getText());
            total.put(String.valueOf((char) (64 + r)), res);
            Integer value = res - entry.getValue();
            if(value >= 0)  entry.setValue(value);
            else {
                displayAlert("分配资源和大于系统总共资源", Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * 根据资源分配信息创建进程
     * @param res
     * @param maxTexts
     * @param allocTexts
     */
    private void createProcess(String[] res, TextField[] maxTexts, TextField[] allocTexts) {
        int v = 0;
        for(int p = 0; p < procNum; p++) {
            Map<String, Integer> max = new HashMap<>();
            Map<String, Integer> allocation = new HashMap<>();
            for(int r = 0; r < resNum; r++) {
                max.put(res[r], Integer.valueOf(maxTexts[v].getText()));
                allocation.put(res[r], Integer.valueOf(allocTexts[v].getText()));
                v++;
            }
            processes.add(new Process("P" + p, max, allocation));
        }
    }

    /**
     * 查看表单是否填写完
     * @return  是否全部填写完
     */
    private boolean isAllFill(TextField[]... texts) {
        for(TextField[] text : texts) {
            if("allocText".equals(text[0].getId())) {   //已分配的资源
                for(int t = 0; t < text.length; t++) {
                    if(text[t].getText() == null || text[t].getText().trim().isEmpty())
                        return false;
                    int col = t % resNum;   //转换得到列
                    String key = String.valueOf((char) (65 + col));
                    Integer subValue = Integer.valueOf(text[t].getText());
                    if(subValue < 0) {  //分配的资源数为负数弹出警告，并清空available
                        displayAlert("分配资源不能为负！", Alert.AlertType.ERROR);
                        available.clear();
                        return false;
                    }
                    if(available.get(key) != null) available.put(key, available.get(key) + subValue);
                    else available.put(key, subValue);
                }
            }else {
                for (TextField t : text) {
                    if (t.getText() == null || t.getText().trim().isEmpty()) return false;
                    if(Integer.valueOf(t.getText().trim()) < 0) {
                        displayAlert("分配资源不能为负！", Alert.AlertType.ERROR);
                        available.clear();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 返回一个中心布局的HBox
     * @param node  需要包装的Node
     * @return  中心布局的HBox
     */
    private HBox getCenterBox (Node node) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(node);
        return box;
    }

    /**
     * 展示运行的结果
     */
    private void displayTable() {
        logger.debug("构建展示运行的结果界面");

        String avaText = "Available : " + available.toString();

        TextField topText = new TextField(avaText);
        topText.setMinWidth(800);
        topText.setEditable(false);
        HBox topBox = getCenterBox(topText);
        border.setTop(topBox);
        Button runBtn = new Button("运行");
        HBox bottomBox = getCenterBox(runBtn);
        border.setBottom(bottomBox);

        TableView<Process> table = new TableView<>();
        border.setCenter(table);
        TableColumn procCol = new TableColumn("Process");
        TableColumn maxCol = new TableColumn("Max");
        TableColumn allocCol = new TableColumn("Allocation");
        TableColumn needCol = new TableColumn("Need");
        TableColumn finCol = new TableColumn("Finish");
        table.getColumns().addAll(procCol, maxCol, allocCol, needCol, finCol);

        ObservableList<Process> ps = FXCollections.observableList(processes);   //进程数据
        procCol.setCellValueFactory(new PropertyValueFactory<Process,String>("name"));
        maxCol.setCellValueFactory(new PropertyValueFactory<Process,String>("max"));
        allocCol.setCellValueFactory(new PropertyValueFactory<Process,String>("allocation"));
        needCol.setCellValueFactory(new PropertyValueFactory<Process,String>("need"));
        finCol.setCellValueFactory(new PropertyValueFactory<Process, String>("finish"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(ps);



        Button reqBtn = new Button("请求");
        //请求新的资源
        reqBtn.setOnAction(action -> {
            //安全序列
            SafetyState safetyState = new SafetyState(processes, available);
            Pair<String, Map<String, Integer>> result = displayReqForm();
            safetyState.request(result.getKey(), result.getValue());
            topText.setText("Available" + available);
                    //更新表单
            table.refresh();
        });

        //点击运行按钮的事件
        runBtn.setOnAction(action -> {
            //安全序列
            SafetyState safetyState = new SafetyState(processes, available);
            List<Process> safeSeq = safetyState.run();
            if(safeSeq.size() != procNum) {
                displayAlert("不存在安全序列，当前状态不安全", Alert.AlertType.ERROR);
                throw new IllegalArgumentException("不存在安全序列，当前状态不安全");
            }else {
                String safe = listToString(safeSeq);
                logger.info("安全序列 :" + safe);
                topText.setText("Available" + available + " => 安全序列 : " + safe);
//                topText.appendText();
                if(!bottomBox.getChildren().contains(reqBtn))
                    bottomBox.getChildren().add(reqBtn);
//                runBtn.setDisable(true);
            }
            //更新表单
            table.refresh();
        } );
    }

    /**
     * 将传入的进程链表集合转化为一串进程字符串
     * @param ps    进程
     * @return  安全序列
     */
    private String listToString(List<Process> ps) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        logger.debug("安全序列:");
        int index = 0;
        for(Process p : ps) {
            if(index++ < procNum - 1) builder.append(p.getName() + ", ");
            else builder.append(p.getName());
            logger.debug("进程 : " + p.getName() + " 进程已分配 : " + p.getAllocation() + " 进程还需 : " + p.getNeed());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * 弹出请求资源表单
     * @return 请求进程和请求资源的键值对
     */
    private Pair<String, Map<String, Integer>> displayReqForm() {
        Dialog<Pair<String, Map<String, Integer>>> dialog = new Dialog<>();
        dialog.setContentText("UTF-8");
        dialog.setHeaderText("输入请求资源信息");
        dialog.setWidth(300);
        dialog.setHeight(200);
        ButtonType reqBtnType = new ButtonType("请求", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reqBtnType, ButtonType.CANCEL);

        //绘制表单
        GridPane reqForm = new GridPane();
        reqForm.setHgap(10);reqForm.setVgap(5);

        Label pidLab = new Label("进程id");
        TextField pidText = new TextField();
        reqForm.add(pidLab, 0, 0);
        reqForm.add(pidText, 1, 0);

        Label reqLab = new Label("请求资源");
        HBox reqResBox = new HBox();
        TextField[] reqTexts = new TextField[resNum];
        for(int rt = 0; rt < resNum; rt++) {
            reqTexts[rt] = new TextField();
            reqTexts[rt].setMaxWidth(RES_TEXT_WIDTH);
            reqResBox.getChildren().add(reqTexts[rt]);
        }
        reqForm.add(reqLab, 0, 1);
        reqForm.add(reqResBox, 1, 1);

        Node reqSubBtn = dialog.getDialogPane().lookupButton(reqBtnType);
        reqSubBtn.setDisable(true);

        pidText.textProperty().addListener((observable, oldValue, newValue) -> {
            reqSubBtn.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(reqForm);
        dialog.setResultConverter(btnType -> {
            if (btnType == reqBtnType) {
                Map<String, Integer> request = new HashMap<>(); //请求资源
                for(int rt = 0; rt < resNum; rt++) {
                    request.put(RES[rt], Integer.valueOf(reqTexts[rt].getText()));
                }
                return new Pair<>(pidText.getText(), request);
            }
            return null;
        });
        Optional<Pair<String, Map<String, Integer>>> result = dialog.showAndWait();
        return result.get();
    }

    /**
     * 弹出错误警告框
     * @param message 警告内容
     * @param alertType 警告类型
     */
    public static void displayAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) {
        border = new BorderPane();
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);
        border.setCenter(gridPane);

        setUpProcess(gridPane);
        //构建应用界面
        Scene scene = new Scene(border, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setUserAgentStylesheet(getClass().getResource("banker.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Made By baith");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.jpg")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
