package sample;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

/**
 * Background communication /w MotorDaemon for real-time informations
 */
public class BackgroundCommunication extends Thread
{

    private static BackgroundCommunication instance = null;

    private BackgroundCommunication()
    {
        control = Communication.getInstance();
    }

    private Communication control;

    private boolean motordaemonIsOnline = false;

    private final NumberAxis xAxisLeft = new NumberAxis();
    private final NumberAxis yAxisLeft = new NumberAxis();
    private final NumberAxis xAxisRight = new NumberAxis();
    private final NumberAxis yAxisRight = new NumberAxis();
    private final NumberAxis xAxisTraj = new NumberAxis();
    private final NumberAxis yAxisTraj = new NumberAxis();

    private LineChart<Number, Number> left_graph;
    private LineChart<Number, Number> right_graph;
    private ScatterChart<Number, Number> traj_graph;

    private XYChart.Series leftSeries = new XYChart.Series();
    private XYChart.Series rightSeries = new XYChart.Series();
    private XYChart.Series trajSeries = new XYChart.Series();
    private XYChart.Series leftSeriesConsigne = new XYChart.Series();
    private XYChart.Series rightSeriesConsigne = new XYChart.Series();
    private XYChart.Series trajSeriesConsigne = new XYChart.Series();

    private boolean speedPrinting = false;
    private boolean curvePrinting = false;
    private boolean go = false;

    private int curveValue = 0;

    public static BackgroundCommunication getInstance()
    {
        if(instance == null) instance = new BackgroundCommunication();
        return instance;
    }

    @Override
    public void run()
    {
        while(motordaemonIsOnline)
        {
            if(curvePrinting)
            {
                Double[] p = control.getPosition();
                if(p != null)
                {
                    trajSeries.getData().add(new XYChart.Data(p[0], p[1]));
                    trajSeriesConsigne.getData().add(new XYChart.Data(p[0], 0/*TODO y val calc*/));
                }
            }

            if(speedPrinting)
            {
                Long[] sv = control.getSpeedValues();
                if(sv != null)
                {
                    leftSeries.getData().add(new XYChart.Data(sv[0],sv[2]));
                    leftSeriesConsigne.getData().add(new XYChart.Data(sv[0],sv[1]));
                    rightSeries.getData().add(new XYChart.Data(sv[0],sv[4]));
                    rightSeriesConsigne.getData().add(new XYChart.Data(sv[0],sv[3]));
                }
            }

            if(go)
            {
                if(!control.isMoving())
                {
                    go = false;
                    if(curvePrinting) stopCurvePrinting();
                    else if(speedPrinting) stopSpeedPrinting();
                }
            }


            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMotordaemonIsOnline(boolean state)
    {
        this.motordaemonIsOnline = state;
    }

    public void setGraphs(LineChart<?, ?> left, LineChart<?, ?> right, ScatterChart<?, ?> traj)
    {
        this.left_graph = (LineChart<Number, Number>) left;
        this.right_graph = (LineChart<Number, Number>) right;
        this.traj_graph = (ScatterChart<Number, Number>) traj;
    }

    private void startSpeedPrinting()
    {
        leftSeries = new XYChart.Series();
        rightSeries = new XYChart.Series();
        leftSeriesConsigne = new XYChart.Series();
        rightSeriesConsigne = new XYChart.Series();
        speedPrinting = true;
    }

    private void stopSpeedPrinting()
    {
        speedPrinting = false;
        Platform.runLater(() -> {
            traj_graph.getData().clear();
            left_graph.getData().clear();
            left_graph.getData().add(leftSeries);
            left_graph.getData().add(leftSeriesConsigne);
            right_graph.getData().clear();
            right_graph.getData().add(rightSeries);
            right_graph.getData().add(rightSeriesConsigne);
        });
    }

    private void startCurvePrinting()
    {
        leftSeries = new XYChart.Series();
        rightSeries = new XYChart.Series();
        leftSeriesConsigne = new XYChart.Series();
        rightSeriesConsigne = new XYChart.Series();
        trajSeries = new XYChart.Series();
        trajSeriesConsigne = new XYChart.Series();
        speedPrinting = true;
        curvePrinting = true;
    }

    private void stopCurvePrinting()
    {
        speedPrinting = false;
        curvePrinting = false;
        Platform.runLater(() -> {
            left_graph.getData().clear();
            left_graph.getData().add(leftSeries);
            left_graph.getData().add(leftSeriesConsigne);
            right_graph.getData().clear();
            right_graph.getData().add(rightSeries);
            right_graph.getData().add(rightSeriesConsigne);
            traj_graph.getData().clear();
            traj_graph.getData().add(trajSeries);
            traj_graph.getData().add(trajSeriesConsigne);
        });

    }

    public void curveSet(int curve)
    {
        this.curveValue = curve;
    }

    public void launchPrinting(boolean curve)
    {
        if(curve) startCurvePrinting();
        else stopSpeedPrinting();
        go = true;
    }
}
