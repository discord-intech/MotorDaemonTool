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

    private XYChart.Series<Number, Number> leftSeries = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> rightSeries = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> trajSeries = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> leftSeriesConsigne = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> rightSeriesConsigne = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> trajSeriesConsigne = new XYChart.Series<Number, Number>();

    private boolean speedPrinting = false;
    private boolean curvePrinting = false;
    private boolean go = false;

    private int curveValue = 0;
    private long startTime = -1;

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
                    trajSeries.getData().add(new XYChart.Data<Number, Number>(p[0], p[1]));
                    trajSeriesConsigne.getData().add(new XYChart.Data<Number, Number>(p[0], Math.sqrt(curveValue - (p[0]-curveValue)*(p[0]-curveValue))));
                }
                else
                {
                    System.err.println("Did not receive response to p");
                }
            }

            if(speedPrinting)
            {
                Long[] sv = control.getSpeedValues();
                if(sv != null)
                {
                    if(startTime < 0) startTime = sv[0];

                    leftSeries.getData().add(new XYChart.Data<Number, Number>(sv[0]-startTime,sv[2]));
                    leftSeriesConsigne.getData().add(new XYChart.Data<Number, Number>(sv[0]-startTime,sv[1]));
                    rightSeries.getData().add(new XYChart.Data<Number, Number>(sv[0]-startTime,sv[4]));
                    rightSeriesConsigne.getData().add(new XYChart.Data<Number, Number>(sv[0]-startTime,sv[3]));
                }
                else
                {
                    System.err.println("Did not receive response to sv");
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
                Thread.sleep(70);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMotordaemonIsOnline(boolean state)
    {
        this.motordaemonIsOnline = state;
    }

    public void setGraphs(LineChart<Number, Number> left, LineChart<Number, Number> right, ScatterChart<Number, Number> traj)
    {
        this.left_graph = left;
        this.right_graph = right;
        this.traj_graph = traj;

        traj_graph.getXAxis().setAutoRanging(false);
        traj_graph.getYAxis().setAutoRanging(false);
        ((NumberAxis)traj_graph.getXAxis()).setUpperBound(1000);
        ((NumberAxis)traj_graph.getXAxis()).setLowerBound(-1000);
        ((NumberAxis)traj_graph.getYAxis()).setUpperBound(1200);
        ((NumberAxis)traj_graph.getYAxis()).setLowerBound(0);
    }

    private void startSpeedPrinting()
    {
        leftSeries = new XYChart.Series<Number, Number>();
        rightSeries = new XYChart.Series<Number, Number>();
        leftSeriesConsigne = new XYChart.Series<Number, Number>();
        rightSeriesConsigne = new XYChart.Series<Number, Number>();
        speedPrinting = true;
    }

    private void stopSpeedPrinting()
    {
        speedPrinting = false;
        startTime = -1;
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
        leftSeries = new XYChart.Series<Number, Number>();
        rightSeries = new XYChart.Series<Number, Number>();
        leftSeriesConsigne = new XYChart.Series<Number, Number>();
        rightSeriesConsigne = new XYChart.Series<Number, Number>();
        trajSeries = new XYChart.Series<Number, Number>();
        trajSeriesConsigne = new XYChart.Series<Number, Number>();
        speedPrinting = true;
        curvePrinting = true;
    }

    private void stopCurvePrinting()
    {
        speedPrinting = false;
        curvePrinting = false;
        startTime = -1;
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
        else startSpeedPrinting();
        go = true;
    }
}
