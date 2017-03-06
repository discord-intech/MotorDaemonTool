package sample;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;


public class Controller
{
    @FXML
    private LineChart<Number, Number> left_graph;

    @FXML
    private LineChart<Number, Number> right_graph;

    @FXML
    private TextField ip_field;

    @FXML
    private Button connect_buton;

    @FXML
    private Button speed_test_button;

    @FXML
    private ScatterChart<Number, Number> traj_graph;

    @FXML
    private Slider speed_slider;

    @FXML
    private Button curve_test_button;

    @FXML
    private TextField kslp;

    @FXML
    private Button set_const_button;

    @FXML
    private Slider curve_slider;

    @FXML
    private TextField ksrp;

    @FXML
    private TextField ktp;

    @FXML
    private TextField kcp;

    @FXML
    private TextField ksli;

    @FXML
    private TextField ksri;

    @FXML
    private TextField kti;

    @FXML
    private TextField kci;

    @FXML
    private TextField ksld;

    @FXML
    private TextField ksrd;

    @FXML
    private TextField ktd;

    @FXML
    private TextField kcd;

    @FXML
    private Label curve_info;

    @FXML
    private Label speed_info;

    private boolean connected;

    private Communication control = Communication.getInstance();
    private BackgroundCommunication background = BackgroundCommunication.getInstance();

    @FXML
    void setSpeed(MouseEvent event)
    {
        speed_info.setText("Speed : "+(long)speed_slider.getValue());
    }

    @FXML
    void setCurve(MouseEvent event)
    {
        curve_info.setText("Curve (mm) : "+(long)curve_slider.getValue());
    }

    @FXML
    void onConnect(MouseEvent event)
    {
        connected = control.connect(ip_field.getText());
        ip_field.setText(connected ? "Connection successful !" : "Connection failed !");
        if(connected)
        {
            ip_field.setDisable(true);
            connect_buton.setDisable(true);
            curve_test_button.setDisable(false);
            speed_test_button.setDisable(false);
            set_const_button.setDisable(false);

            ArrayList<Float> constants = control.getConstants();

            if(constants != null && constants.size() == 12)
            {
                kslp.setText(constants.get(0).toString());
                ksli.setText(constants.get(1).toString());
                ksld.setText(constants.get(2).toString());
                ksrp.setText(constants.get(3).toString());
                ksri.setText(constants.get(4).toString());
                ksrd.setText(constants.get(5).toString());
                ktp.setText(constants.get(6).toString());
                kti.setText(constants.get(7).toString());
                ktd.setText(constants.get(8).toString());
                kcp.setText(constants.get(9).toString());
                kci.setText(constants.get(10).toString());
                kcd.setText(constants.get(11).toString());
            }

            background.setMotordaemonIsOnline(true);
            background.setGraphs(left_graph, right_graph, traj_graph);
            background.start();
        }
    }

    @FXML
    void onSetConstants(MouseEvent event)
    {
        if(!connected) return;

        control.send("setksg "+kslp.getText()+" "+ksli.getText()+" "+ksld.getText());
        control.send("setksd "+ksrp.getText()+" "+ksri.getText()+" "+ksrd.getText());
        control.send("setktd "+ktp.getText()+" "+kti.getText()+" "+ktd.getText());
        control.send("setkcd "+kcp.getText()+" "+kci.getText()+" "+kcd.getText());
    }

    @FXML
    void onTestCurve(MouseEvent event) throws InterruptedException
    {
        if(!connected) return;

        control.setPosition(0,0);
        control.setAngle(0);
        control.send("sets "+speed_slider.getValue());
        control.send("cr "+curve_slider.getValue());
        control.send("d 600");
        background.curveSet((int)curve_slider.getValue());
        background.launchPrinting(true);
    }

    @FXML
    void onTestSpeed(MouseEvent event) throws InterruptedException
    {
        if(!connected) return;

        control.send("sets "+speed_slider.getValue());
        control.send("ts "+speed_slider.getValue());
        background.launchPrinting(false);
    }

}
