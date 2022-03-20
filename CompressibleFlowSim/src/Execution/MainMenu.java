package Execution;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Functions.FileUtilities;

public class MainMenu extends JPanel implements KeyListener, MouseListener, ChangeListener, ActionListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6706276688740706862L;

	private JFrame mainFrame;
	private String title = "Compressible Flow Sim";
	private int preferredWidth = 1020;
	private int preferredHeight = 600;
	private final int minimumWidth = 1020;
	private final int minimumHeight = 600;
	private int currentWidth = 800;
	private int currentHeight = 600;
	private double currentRatio = currentWidth / currentHeight;
	
	private int length = 400;
	private int plenum_chamber_width = 100;
	
	private double plenum_chamber_pressure = 101325.0; // Pa
	private double plenum_chamber_temperature = 303.15; // K
	private double[] totalPressurePoints = new double[1000];
	private double[] totalTemperaturePoints = new double[1000];
	
	private JPanel GUIPanel;
	
	private JLabel optionsTitleLabel;
	private JLabel totalLengthLabel;
	private JPanel lengthPanel;
	private JSlider totalLengthSlider;
	private JPanel plenumPressurePanel;
	private JLabel plenumPressureLabel;
	private JTextField plenumPressureTextbox;
	private JLabel plenumPressureUnitLabel;
	private JPanel plenumTemperaturePanel;
	private JLabel plenumTemperatureLabel;
	private JTextField plenumTemperatureTextbox;
	private JLabel plenumTemperatureUnitLabel;
	
	private GraphicsThread graphicsThread;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
            public void run() {                
            	new Thread(new MainMenu()).start();             
            }
        });
	}
	
	public MainMenu() {
		FileUtilities.checkApplicationFolder();
		FileUtilities.log("Initializing Compressible Flow Sim...\n");
	}
	
	public JFrame initializeFrame(String title, int minimumWidth, int minimumHeight) {
		JFrame frame = new JFrame();
		
		frame.setTitle(title);
		frame.setBackground(Color.BLUE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight + 128));
		
		// Exit confirmation dialog
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Runtime.getRuntime().gc();
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure to want to exit the application?", "Quit now?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	exitSequence();
		            System.exit(0);
		        }
		    }
		});
		
		// Update current size only on resize
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				currentWidth = getWidth();
				currentHeight = getHeight();
				currentRatio = (currentWidth + 1) / (currentHeight + 1);
			}
		});
		
		return frame;
	}
	
	private JPanel initializeGUIPanel() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		optionsTitleLabel = new JLabel("Options");
		panel.add(optionsTitleLabel);
		
		lengthPanel = new JPanel();
		lengthPanel.setLayout(new BoxLayout(lengthPanel, BoxLayout.X_AXIS));
		
		totalLengthSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		totalLengthSlider.setMajorTickSpacing(10);
		totalLengthSlider.setMinorTickSpacing(1);
		totalLengthSlider.setPaintTicks(true);
		totalLengthSlider.setPaintLabels(true);
		totalLengthSlider.addChangeListener(this);
		
		totalLengthLabel = new JLabel("Total Length: ");
		
		lengthPanel.add(totalLengthLabel);
		lengthPanel.add(totalLengthSlider);
		
		panel.add(lengthPanel);
		
		plenumPressurePanel = new JPanel();
		plenumPressurePanel.setLayout(new BoxLayout(plenumPressurePanel, BoxLayout.X_AXIS));
		
		plenumPressureTextbox = new JTextField();
		plenumPressureTextbox.addActionListener(this);
		plenumPressureTextbox.setMaximumSize(new Dimension(200, 64));
		
		plenumPressureLabel = new JLabel("Plenum chamber pressure:      ");
		plenumPressureUnitLabel = new JLabel("   Pa.");
		
		plenumPressurePanel.add(plenumPressureLabel);
		plenumPressurePanel.add(plenumPressureTextbox);
		plenumPressurePanel.add(plenumPressureUnitLabel);
		
		panel.add(plenumPressurePanel);
		
		plenumTemperaturePanel = new JPanel();
		plenumTemperaturePanel.setLayout(new BoxLayout(plenumTemperaturePanel, BoxLayout.X_AXIS));
		
		plenumTemperatureTextbox = new JTextField();
		plenumTemperatureTextbox.addActionListener(this);
		plenumTemperatureTextbox.setMaximumSize(new Dimension(200, 64));
		
		plenumTemperatureLabel = new JLabel("Plenum chamber temperature:      ");
		plenumTemperatureUnitLabel = new JLabel("   K");
		
		plenumTemperaturePanel.add(plenumTemperatureLabel);
		plenumTemperaturePanel.add(plenumTemperatureTextbox);
		plenumTemperaturePanel.add(plenumTemperatureUnitLabel);
		
		panel.add(plenumTemperaturePanel);
		
		return panel;
	}
	
	public void exitSequence() {
		FileUtilities.log("Exiting Compressible Flow Sim...\n");
		FileUtilities.closeLog();
	}
	
	public class GraphicsThread extends Thread {
		Graphics g;
		
		public void updateGraphicsObject(Graphics g) {
			this.g = g;
		}
		
		public void run() {
			if (g == null) throw new IllegalArgumentException("No graphics object provided");
			else render();
		}

		private void render() {
			g.setColor(new Color(208, 208, 208));
			g.fillRect(0, 0, currentWidth, currentHeight);
			
			g.setColor(new Color(64, 64, 64));
			g.drawRect(0, 0, currentWidth - 1, currentHeight - 1);
			
			int centerX = currentWidth >> 1;
			int centerY = currentHeight >> 1;
			int length_scale = currentWidth / 200;
			length = totalLengthSlider.getValue() * length_scale;
			plenum_chamber_width = 25 * length_scale;
			int halflength = length >> 1;
			int halfheight = (currentHeight / 400) * 200;
			
			Color textColor = new Color(224, 224, 224);
			
			g.setColor(Color.WHITE);
			g.fillRect(centerX - halflength, centerY - halfheight, length, halfheight << 1);
			
			g.setColor(new Color(16, 16, 16));
			// Upper Line
			g.drawLine(centerX - halflength, centerY + halfheight, centerX + halflength, centerY + halfheight);
		
			// Lower Line
			g.drawLine(centerX - halflength, centerY - halfheight, centerX + halflength, centerY - halfheight);
			
			int length_tens = length / 10 / length_scale;
			int height_tens = (halfheight << 1) / 10 / length_scale;
			FontMetrics defaultFontMetrics = g.getFontMetrics();
			for (int i = 0; i <= length_tens; i++) {
				for (int j = 1; j < height_tens; j++) {
					// Gridlines
					g.setColor(textColor);
					g.drawLine(centerX - halflength + i * 10 * length_scale, centerY + halfheight - 1, centerX - halflength + i * 10 * length_scale, centerY - halfheight + 1);
					g.drawLine(centerX - halflength, centerY - halfheight + j * 10 * length_scale, centerX + halflength, centerY - halfheight + j * 10 * length_scale);
				}
				
				for (int ii = 0; i < 10 && ii <= 10 && i * 10 + ii <= length / length_scale; ii++) {
					g.setColor(new Color(64, 64, 64));
					byte tickLength = 2;
					if (ii % 10 == 0) tickLength = 4;
					g.drawLine(centerX - halflength + i * 10 * length_scale + ii * length_scale, centerY + halfheight + 1, centerX - halflength + i * 10 * length_scale + ii * length_scale, centerY + halfheight + 1 + tickLength);
				}
				// Tick labels
				g.setColor(new Color(8, 8, 8));
				String tick = Integer.toString(i * 10);
				g.drawString(tick, centerX - halflength + i * 10 * length_scale - defaultFontMetrics.stringWidth(tick) + 8, centerY + halfheight + 24);
			}
			
			// Plenum chamber graphic
			g.setColor(new Color(128, 128, 128));
			g.fillRect(centerX - halflength - plenum_chamber_width, centerY - halfheight, plenum_chamber_width, halfheight << 1);
			
			g.setColor(new Color(24, 24, 24));
			g.drawRect(centerX - halflength - plenum_chamber_width, centerY - halfheight, plenum_chamber_width, halfheight << 1);
		
			// Plenum chamber pressure label
			g.setColor(textColor);
			
			String family = g.getFont().getFamily();
			
			g.setFont(new Font(family, Font.PLAIN, 10 + length_scale));
			String plenum_po_string = String.format("%1$.3f kPa", plenum_chamber_pressure / 1000);
			String plenum_to_string = String.format("%1$.2f K", plenum_chamber_temperature);
			g.drawString(plenum_po_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1));
			g.drawString(plenum_to_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1) + 24);
		
			recalculateProperties();
			
			// Total pressure
			int polength = totalPressurePoints.length;
			for (int i = 0; i < length && i < polength; i++) {
				g.setColor(Color.BLUE);
				g.fillOval(centerX - halflength + i, centerY + halfheight - (int) Math.round(totalPressurePoints[i] / 1000000 * (halfheight << 1)), 2, 2);
			}
			
			// Total temperature
			int tolength = totalTemperaturePoints.length;
			for (int i = 0; i < length && i < tolength; i++) {
				g.setColor(Color.RED);
				g.fillOval(centerX - halflength + i, centerY + halfheight - (int) Math.round(totalTemperaturePoints[i] / 1000 * (halfheight << 1)), 2, 2);
			}
			
			renderAxis(g, 0, centerX + halflength, centerY - halfheight, halfheight << 1, 1000, family, 12, Color.BLUE, " kPa");
			renderAxis(g, 1, centerX + halflength + 40, centerY - halfheight, halfheight << 1, 1000, family, 12, Color.RED, "  K");
			renderLegend(g, new String[] {"Total Pressure", "Total Temperature"}, family, 12, new Color[] {Color.BLUE, Color.RED});
		}
		
		private void renderAxis(Graphics g, int index, int startX, int startY, int height, int max, String family, int size, Color color, String unit) {
			g.setColor(color);
			g.drawLine(startX, startY, startX, startY + height);
			
			g.setFont(new Font(family, Font.PLAIN, size));
			// Tick marks
			g.drawString(unit, startX + 8, startY - 10);
			for (int j = 0; j <= 10; j++) {
				for (int jj = 0; j < 10 && jj < 10; jj++) {
					g.drawLine(startX, startY + j * height / 10 + jj * height / 100, startX + 2, startY + j * height / 10 + jj * height / 100);
				}
				g.drawLine(startX, startY + j * height / 10, startX + 4, startY + j * height / 10);
				String tick = Integer.toString(max - j * max / 10);
				g.drawString(tick, startX + 8, startY + j * height / 10 + 4);
			}
		}
		
		private void renderLegend(Graphics g, String[] labels, String family, int size, Color[] colors) {
			int startX = currentWidth - 144 - 32;
			int startY = (currentWidth >> 5) + 16;
			
			g.setColor(Color.WHITE);
			g.fillRect(startX, startY - 16, 144, 128);
			g.setColor(Color.BLACK);
			g.drawRect(startX, startY - 16, 144, 128);
			
			g.setFont(new Font(family, Font.PLAIN, size));
			
			int stringLength = labels.length;
			int colorLength = colors.length;
			int smallestLength = Math.min(stringLength, colorLength);
			
			for (int j = 0; j < smallestLength; j++) {
				int spacing = j * (g.getFontMetrics().getHeight() + 4);
				g.setColor(colors[j]);
				g.drawLine(startX + 16, startY + spacing, startX + 32, startY + spacing);
				g.drawString(labels[j], startX + 36, startY + spacing + 4);
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		graphicsThread.updateGraphicsObject(g);
		graphicsThread.run();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		graphicsThread = new GraphicsThread();
		
		mainFrame = initializeFrame(title, minimumWidth, minimumHeight);
		
		setBackground(Color.BLACK);
		setMinimumSize(new Dimension(minimumWidth, minimumHeight));
		setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		
		GUIPanel = initializeGUIPanel();
		 
		mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.PAGE_AXIS));
		mainFrame.add(this);
		mainFrame.add(GUIPanel);
		
		setVisible(true);
		GUIPanel.setVisible(true);
		
		mainFrame.pack();
		
		mainFrame.setVisible(true);
		
		recalculateProperties();
		
		repaint();
	}
	
	public double[] calculateTotalPressure(int length, double initialTotalPressure) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] totalPressures = new double[length];
		for (int i = 0; i < length; i++) {
			totalPressures[i] = initialTotalPressure;
		}
		return totalPressures;
	}
	
	public double[] calculateTotalTemperature(int length, double initialTotalTemperature) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] totalTemperatures = new double[length];
		for (int i = 0; i < length; i++) {
			totalTemperatures[i] = initialTotalTemperature;
		}
		return totalTemperatures;
	}
	
	public void recalculateProperties() {
		totalPressurePoints = calculateTotalPressure(length, plenum_chamber_pressure);
		totalTemperaturePoints = calculateTotalTemperature(length, plenum_chamber_temperature);
		
		repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		repaint();
		recalculateProperties();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source.equals(plenumPressureTextbox)) {
			Double plenum_text_pressure = 0.0;
			try {
				plenum_text_pressure = Double.parseDouble(plenumPressureTextbox.getText());
				if (plenum_text_pressure > 1000000) plenum_text_pressure = (double) 1000000;
			}
			catch (NumberFormatException exc) {
				return;
			}
			
			if (plenum_text_pressure > 0.0) {
				plenum_chamber_pressure = plenum_text_pressure;
				recalculateProperties();
			} 
			//plenum_chamber_pressure = 0;
		} else if (source.equals(plenumTemperatureTextbox)) {
			Double plenum_text_temperature = 0.0;
			try {
				plenum_text_temperature = Double.parseDouble(plenumTemperatureTextbox.getText());
				if (plenum_text_temperature > 1000) plenum_text_temperature = (double) 1000;
			}
			catch (NumberFormatException exc) {
				return;
			}
			
			if (plenum_text_temperature > 0.0) {
				plenum_chamber_temperature = plenum_text_temperature;
				recalculateProperties();
			} 
		}
	}
}
