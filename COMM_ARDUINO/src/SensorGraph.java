import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;


public class SensorGraph {
	
	static SerialPort chosenPort;
	static int x = 0;
	static int y = 0;
	
	static String [] value = new String[8];

	public static void main(String[] args) {
		
		JFrame window = new JFrame();
		window.setTitle("Comportamento da IMU e GPS");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		JComboBox<String> portList = new JComboBox<String>();
		JButton connectButton = new JButton("Conectar");

		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		window.add(topPanel, BorderLayout.NORTH);
		
		
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i].getSystemPortName());
		

		XYSeries series_pitch_acc = new XYSeries("Acelerômetro");
		XYSeries series_pitch_gy = new XYSeries("Giroscópio");
		XYSeries series_pitch_comp = new XYSeries("Filtro Complementar");
		
		XYSeries series_roll_acc = new XYSeries("Acelerômetro");
		XYSeries series_roll_gy = new XYSeries("Giroscópio");
		XYSeries series_roll_comp = new XYSeries("Filtro Complementar");
		
		XYSeries GPS_x_axis = new XYSeries("X (metros)");
		
		
		XYSeriesCollection dataset = new XYSeriesCollection(series_pitch_acc);
		XYSeriesCollection dataset_gps = new XYSeriesCollection(GPS_x_axis);
		
		XYSeriesCollection dataset_gy = new XYSeriesCollection(series_roll_acc);
		JFreeChart chart_pitch, chart_roll, CHART_GPS;

		
		chart_pitch = ChartFactory.createXYLineChart("IMU Roll", "Tempo (segundos)", "Roll (graus)", dataset, PlotOrientation.VERTICAL, true, true, true);
		window.add(new ChartPanel(chart_pitch), BorderLayout.WEST);

		
		chart_roll = ChartFactory.createXYLineChart("IMU Pitch", "Tempo (segundos)", "Pitch (graus)", dataset_gy, PlotOrientation.VERTICAL, true, true, true);
		window.add(new ChartPanel(chart_roll), BorderLayout.EAST);
		
		CHART_GPS = ChartFactory.createXYLineChart("Módulo GPS", "X (metros)", "Y (metros)", dataset_gps, PlotOrientation.VERTICAL, true, true, true);
		window.add(new ChartPanel(CHART_GPS), BorderLayout.SOUTH);
		
		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Conectar")) {
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(chosenPort.openPort()) {
						connectButton.setText("Desconectar");
						portList.setEnabled(false);
					}
					
					
					Thread thread = new Thread(){
						@Override public void run() {
							Scanner scanner = new Scanner(chosenPort.getInputStream());
							while(scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();
									value = line.split(",");
									System.out.println(line);
									double pitch_acc = Double.parseDouble(value[0]);
									double pitch_gy = Double.parseDouble(value[2]);
									double pitch_comp = Double.parseDouble(value[4]);
									
									double roll_acc = Double.parseDouble(value[1]);
									double roll_gy = Double.parseDouble(value[3]);
									double roll_comp = Double.parseDouble(value[5]);
									
									double GPS_Xaxis = Double.parseDouble(value[6]);
									double GPS_Yaxis = Double.parseDouble(value[7]);
								
									
									if(x ==100)
										x = 0;
									series_pitch_acc.add(x++, pitch_acc);
									series_pitch_gy.add(x++, pitch_gy);
									series_pitch_comp.add(x++, pitch_comp);
									
									
									series_roll_acc.add(x++, roll_acc);
									series_roll_gy.add(x++, roll_gy);
									series_roll_comp.add(x++, roll_comp);
									
									
									
									GPS_x_axis.add(GPS_Xaxis, GPS_Yaxis);
									
									//series.add(y++, 10);
									window.repaint();
								} catch(Exception e) {}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					chosenPort.closePort();
					portList.setEnabled(true);
					connectButton.setText("Conectar");
					series_pitch_acc.clear();
					series_pitch_gy.clear();
					series_pitch_comp.clear();

					series_roll_acc.clear();
					series_roll_gy.clear();
					series_roll_comp.clear();
					
					GPS_x_axis.clear();
					x = 0;
				}
			}
		});
		dataset.addSeries(series_pitch_gy);
		dataset.addSeries(series_pitch_comp);
		
		dataset_gy.addSeries(series_roll_gy);
		dataset_gy.addSeries(series_roll_comp);
		window.setVisible(true);
	}

}