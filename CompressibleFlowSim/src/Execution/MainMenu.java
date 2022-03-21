package Execution;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Functions.FileUtilities;
import Functions.IsentropicRelations;

public class MainMenu extends JPanel implements ChangeListener, ActionListener, Runnable {

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
	
	private int length = 400;
	private int plenum_chamber_width = 100;
	
	private double specific_heat_p = 1005;
	private double specific_heat_k = 1.4;
	private double gas_constant_R = 287; // J / kg * K;
	
	private double plenum_chamber_pressure = 101325.0; // Pa
	private double plenum_chamber_temperature = 303.15; // K
	private double plenum_chamber_velocity = 0; // m/s
	private double plenum_chamber_mach = 0;
	private double[] totalPressurePoints = new double[1000];
	private double[] totalTemperaturePoints = new double[1000];
	private double[] velocityPoints = new double[1000];
	private double[] machPoints = new double[1000];
	private double[] staticPressurePoints = new double[1000];
	private double[] staticTemperaturePoints = new double[1000];
	
	private ArrayList<Color> colorOrder;
	
	private JPanel GUIPanel;
	
	private JPanel leftBorderPanel;
	private JPanel rightBorderPanel;
	
	private JPanel plotOptionsPanel;
	
	private JLabel plotOptionsTitleLabel;
	
	private JCheckBox axesCheckbox;
	private boolean axesEnabled = true;
	
	private JCheckBox backgroundCheckbox;
	private boolean backgroundEnabled = true;
	
	private JCheckBox gridCheckbox;
	private boolean gridEnabled = true;
	
	private JCheckBox fineGridCheckbox;
	private boolean fineGridEnabled = true;
	
	private JCheckBox legendCheckbox;
	private boolean legendEnabled = true;
	
	private JPanel lengthPanel;
	private JLabel totalLengthLabel;
	private JSlider totalLengthSlider;
	
	private JPanel flowOptionsPanel;
	
	private JLabel flowOptionsTitleLabel;
	
	private JPanel plenumPanel;
	
	private JLabel plenumPressureLabel;
	private JTextField plenumPressureTextbox;
	private JLabel plenumPressureUnitLabel;
	
	private JLabel plenumTemperatureLabel;
	private JTextField plenumTemperatureTextbox;
	private JLabel plenumTemperatureUnitLabel;
	
	private JLabel plenumVelocityLabel;
	private JLabel plenumMachLabel;
	private JTextField plenumVelocityTextbox;
	private JTextField plenumMachTextbox;
	private JLabel plenumVelocityUnitLabel;
	
	private JPanel flowSelectorPanel;
	private JPanel flowSelectorSubPanel;
	private JLabel flowSelectorLabel;
	private JRadioButton noneButton;
	private JRadioButton fannoButton;
	private JRadioButton rayleighButton;
	private JRadioButton convergingButton;
	private JRadioButton divergingButton;
	private JRadioButton convergingDivergingButton;
	private ButtonGroup flowSelector;
	
	private JPanel fannoPanel;
	//
	
	private JPanel rayleighPanel;
	//
	
	private JPanel convergingPanel;
	//
	
	private JPanel divergingPanel;
	//
	
	private JPanel convergingDivergingPanel;
	//
	
	private enum FLOW_MODE {
			NONE,
			FANNO,
			RAYLEIGH,
			CONVERGING,
			DIVERGING,
			CONVERGING_DIVERGING;
	}
	private FLOW_MODE flowMode = FLOW_MODE.NONE;
	
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
		frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight + 512));
		
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
			}
		});
		
		return frame;
	}
	
	private JPanel initializeGUIPanel() {
		// Overall and top level panels use line and page axes
		// Could be utilized for portrait/landscape mode
		JPanel overallPanel = new JPanel();		
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.LINE_AXIS));
		
		Dimension maximumTextboxSize = new Dimension(200, 25);
		
		Dimension borderSize = new Dimension(125, 1);
		
		leftBorderPanel = new JPanel();
		leftBorderPanel.setMinimumSize(borderSize);
		leftBorderPanel.setMaximumSize(borderSize);
		//leftBorderPanel.setBackground(Color.DARK_GRAY);
		overallPanel.add(leftBorderPanel);
		
		flowOptionsPanel = new JPanel();
		flowOptionsPanel.setLayout(new BoxLayout(flowOptionsPanel, BoxLayout.PAGE_AXIS));
		
		flowOptionsTitleLabel = new JLabel("Flow Options");
		flowOptionsPanel.add(flowOptionsTitleLabel);
		
		plenumPanel = new JPanel();
		plenumPanel.setLayout(new GridLayout(4, 3));
		((GridLayout) plenumPanel.getLayout()).setHgap(4);
		
		plenumPressureTextbox = new JTextField();
		plenumPressureTextbox.addActionListener(this);
		plenumPressureTextbox.setMaximumSize(maximumTextboxSize);
		
		plenumPressureLabel = new JLabel("Plenum chamber pressure:");
		plenumPressureUnitLabel = new JLabel("Pa.");
		
		plenumTemperatureTextbox = new JTextField();
		plenumTemperatureTextbox.addActionListener(this);
		plenumTemperatureTextbox.setMaximumSize(maximumTextboxSize);
		
		plenumTemperatureLabel = new JLabel("Plenum chamber temperature:");
		plenumTemperatureUnitLabel = new JLabel("K");
		
		plenumVelocityLabel = new JLabel("Plenum chamber exit velocity:");
		plenumMachLabel = new JLabel("Plenum chamber exit mach:");
		
		plenumVelocityTextbox = new JTextField();
		plenumVelocityTextbox.addActionListener(this);
		plenumVelocityTextbox.setMaximumSize(maximumTextboxSize);
		
		plenumMachTextbox = new JTextField();
		plenumMachTextbox.addActionListener(this);
		plenumMachTextbox.setMaximumSize(maximumTextboxSize);
		
		plenumVelocityUnitLabel = new JLabel("m/s");
		
		plenumPanel.add(plenumPressureLabel);
		plenumPanel.add(plenumPressureTextbox);
		plenumPanel.add(plenumPressureUnitLabel);
		plenumPanel.add(plenumTemperatureLabel);
		plenumPanel.add(plenumTemperatureTextbox);
		plenumPanel.add(plenumTemperatureUnitLabel);
		plenumPanel.add(plenumVelocityLabel);
		plenumPanel.add(plenumVelocityTextbox);
		plenumPanel.add(plenumVelocityUnitLabel);
		plenumPanel.add(plenumMachLabel);
		plenumPanel.add(plenumMachTextbox);
		
		plenumPanel.setMaximumSize(new Dimension(maximumTextboxSize.width * 3, maximumTextboxSize.height));
		
		flowOptionsPanel.add(plenumPanel);		
		
		flowSelectorPanel = new JPanel();
		// Leave as default FlowLayout to avoid clipping
		//flowSelectorPanel.setLayout(new BoxLayout(flowSelectorPanel, BoxLayout.X_AXIS));
		
		flowSelectorLabel = new JLabel("Flow type:      ");
		
		noneButton = new JRadioButton("None");
		noneButton.setMnemonic(KeyEvent.VK_N);
		noneButton.setSelected(true);
		
		fannoButton = new JRadioButton("Fanno Flow");
		fannoButton.setMnemonic(KeyEvent.VK_F);
		
		rayleighButton = new JRadioButton("Rayleigh Flow");
		rayleighButton.setMnemonic(KeyEvent.VK_R);
		
		convergingButton = new JRadioButton("Converging Nozzle");
		convergingButton.setMnemonic(KeyEvent.VK_C);
		
		divergingButton = new JRadioButton("Diverging Nozzle");
		divergingButton.setMnemonic(KeyEvent.VK_D);
		
		convergingDivergingButton = new JRadioButton("Converging-Diverging Nozzle");
		convergingDivergingButton.setMnemonic(KeyEvent.VK_Z);
		
		flowSelector = new ButtonGroup();
		flowSelector.add(noneButton);
		flowSelector.add(fannoButton);
		flowSelector.add(rayleighButton);
		flowSelector.add(convergingButton);
		flowSelector.add(divergingButton);
		flowSelector.add(convergingDivergingButton);
		
		flowSelectorSubPanel = new JPanel();
		flowSelectorSubPanel.setLayout(new BoxLayout(flowSelectorSubPanel, BoxLayout.Y_AXIS));
		
		flowSelectorSubPanel.add(noneButton);
		flowSelectorSubPanel.add(fannoButton);
		flowSelectorSubPanel.add(rayleighButton);
		flowSelectorSubPanel.add(convergingButton);
		flowSelectorSubPanel.add(divergingButton);
		flowSelectorSubPanel.add(convergingDivergingButton);
		
		flowSelectorPanel.add(flowSelectorLabel);
		flowSelectorPanel.add(flowSelectorSubPanel);
		
		flowOptionsPanel.add(flowSelectorPanel);
		
		overallPanel.add(flowOptionsPanel);
		
		plotOptionsPanel = new JPanel();
		plotOptionsPanel.setLayout(new BoxLayout(plotOptionsPanel, BoxLayout.PAGE_AXIS));
		
		plotOptionsTitleLabel = new JLabel("Plot Options:");
		plotOptionsPanel.add(plotOptionsTitleLabel);
		
		axesCheckbox = new JCheckBox("Axes");
		axesCheckbox.setMnemonic(KeyEvent.VK_X);
		axesCheckbox.addActionListener(this);
		axesCheckbox.setSelected(true);
		
		backgroundCheckbox = new JCheckBox("Background");
		backgroundCheckbox.setMnemonic(KeyEvent.VK_B);
		backgroundCheckbox.addActionListener(this);
		backgroundCheckbox.setSelected(true);
		
		gridCheckbox = new JCheckBox("Grid");
		gridCheckbox.setMnemonic(KeyEvent.VK_G);
		gridCheckbox.addActionListener(this);
		gridCheckbox.setSelected(true);

		fineGridCheckbox = new JCheckBox("Fine Grid");
		fineGridCheckbox.setMnemonic(KeyEvent.VK_I);
		fineGridCheckbox.addActionListener(this);
		fineGridCheckbox.setSelected(true);
		
		legendCheckbox = new JCheckBox("Legend");
		legendCheckbox.setMnemonic(KeyEvent.VK_L);
		legendCheckbox.addActionListener(this);
		legendCheckbox.setSelected(true);
		
		plotOptionsPanel.add(axesCheckbox);
		plotOptionsPanel.add(backgroundCheckbox);
		plotOptionsPanel.add(gridCheckbox);
		plotOptionsPanel.add(fineGridCheckbox);
		plotOptionsPanel.add(legendCheckbox);
		
		lengthPanel = new JPanel();
		lengthPanel.setLayout(new BoxLayout(lengthPanel, BoxLayout.X_AXIS));
		
		totalLengthSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		totalLengthSlider.setMinimumSize(new Dimension(800, 100));
		totalLengthSlider.setMajorTickSpacing(10);
		totalLengthSlider.setMinorTickSpacing(1);
		totalLengthSlider.setPaintTicks(true);
		totalLengthSlider.setPaintLabels(true);
		totalLengthSlider.addChangeListener(this);
		
		totalLengthLabel = new JLabel("Total Length: ");
		
		lengthPanel.add(totalLengthLabel);
		lengthPanel.add(totalLengthSlider);
		
		plotOptionsPanel.add(lengthPanel);
		
		overallPanel.add(plotOptionsPanel);
		
		rightBorderPanel = new JPanel();
		rightBorderPanel.setMinimumSize(borderSize);
		rightBorderPanel.setMaximumSize(borderSize);
		//rightBorderPanel.setBackground(Color.DARK_GRAY);
		overallPanel.add(rightBorderPanel);
		
		return overallPanel;
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
			
			Color plenumTextColor = new Color(224, 224, 224);
			Color gridColor = new Color(116, 116, 116);
			Color fineGridColor = new Color(132, 132, 132);
			
			if (backgroundEnabled) {
				g.setColor(Color.WHITE);
				g.fillRect(centerX - halflength, centerY - halfheight, length, halfheight << 1);
				
				gridColor = new Color(216, 216, 216);
				fineGridColor = new Color(240, 240, 240);
			}
			
			g.setColor(new Color(16, 16, 16));
			// Upper Line
			g.drawLine(centerX - halflength, centerY + halfheight, centerX + halflength, centerY + halfheight);
		
			// Lower Line
			g.drawLine(centerX - halflength, centerY - halfheight, centerX + halflength, centerY - halfheight);
			
			int length_tens = length / 10 / length_scale;
			int height_tens = (halfheight << 1) / 10;
			FontMetrics defaultFontMetrics = g.getFontMetrics();
			
			if (fineGridEnabled) {
				g.setColor(fineGridColor);
				for (int i = 0; i < 100; i++) {
					for (int j = 1; j < 100; j++) {
						if (i > length/length_scale) break;
						g.drawLine(centerX - halflength + i * length_scale, centerY + halfheight - 1, centerX - halflength + i * length_scale, centerY - halfheight + 1);
						g.drawLine(centerX - halflength, centerY - halfheight + j * halfheight / 50, centerX + halflength, centerY - halfheight + j * halfheight / 50);
					}
				}
			}
			
			if (axesEnabled || gridEnabled) {
				for (int i = 0; i <= length_tens; i++) {
					// Gridlines
					if (gridEnabled) {
						for (int j = 1; j < 10; j++) {
							g.setColor(gridColor);
							g.drawLine(centerX - halflength + i * 10 * length_scale, centerY + halfheight - 1, centerX - halflength + i * 10 * length_scale, centerY - halfheight + 1);
							g.drawLine(centerX - halflength, centerY - halfheight + j * halfheight / 5, centerX + halflength, centerY - halfheight + j * halfheight / 5);
						}
					}
					
					// Length axis
					if (axesEnabled) {
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
				}
			}
			
			// Plenum chamber graphic
			g.setColor(new Color(128, 128, 128));
			g.fillRect(centerX - halflength - plenum_chamber_width, centerY - halfheight, plenum_chamber_width, halfheight << 1);
			
			g.setColor(new Color(24, 24, 24));
			g.drawRect(centerX - halflength - plenum_chamber_width, centerY - halfheight, plenum_chamber_width, halfheight << 1);
		
			// Plenum chamber pressure label
			g.setColor(plenumTextColor);
			
			String family = g.getFont().getFamily();
			
			g.setFont(new Font(family, Font.PLAIN, 10 + length_scale));
			String plenum_po_string = String.format("%1$.3f kPa", plenum_chamber_pressure / 1000);
			String plenum_to_string = String.format("%1$.2f K", plenum_chamber_temperature);
			String plenum_v_string = String.format("%1$.3f m/s", plenum_chamber_velocity);
			String plenum_m_string = String.format("Mach %1.4f", plenum_chamber_mach);
			g.drawString(plenum_po_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1) - 64);
			g.drawString(plenum_to_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1) - 32);
			g.drawString(plenum_v_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1));
			g.drawString(plenum_m_string, centerX - halflength - (plenum_chamber_width) + (length_scale << 2), centerY + (defaultFontMetrics.getHeight() >> 1) + 32);
			
			recalculateProperties();
			
			colorOrder = new ArrayList<Color>();
			
			// Static pressure
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, staticPressurePoints, 1000000, Color.GRAY);
			
			// Total pressure
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, totalPressurePoints, 1000000);
			
			// Static temperature
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, staticTemperaturePoints, 1000, new Color(224, 125, 125));
			
			// Total temperature
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, totalTemperaturePoints, 1000, Color.RED);
			
			// Velocity
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, velocityPoints, 1000, new Color(68, 128, 69));
			
			// Mach
			plot(g, centerX - halflength, centerY + halfheight, halfheight << 1, machPoints, 10, Color.BLUE);
			
			if (axesEnabled) {
				renderAxis(g, 1, centerX + halflength, centerY - halfheight, halfheight << 1, 1000, family, 12, " kPa");
				renderAxis(g, 3, centerX + halflength + 40, centerY - halfheight, halfheight << 1, 1000, family, 12, "  K");
				renderAxis(g, 4, centerX + halflength + 80, centerY - halfheight, halfheight << 1, 1000, family, 12, " m/s");
				renderAxis(g, 5, centerX + halflength + 120, centerY - halfheight, halfheight << 1, 10, family, 12, "Mach");
			}
			if (legendEnabled) renderLegend(g, new String[] {"Static Pressure", "Total Pressure", "Static Temperature", "Total Temperature", "Velocity", "Mach"}, family, 12);
		}

		private void plot(Graphics g, int startX, int startY, int height, double[] y, double scale) {
			plot(g, startX, startY, height, y, scale, Color.BLACK);
		}
		
		private void plot(Graphics g, int startX, int startY, int height, double[] y, double scale, Color color) {
			int vlength = y.length;
			for (int i = 0; i < length && i < vlength; i++) {
				g.setColor(color);
				g.fillOval(startX + i, startY - (int) Math.round(y[i] / scale * height), 2, 2);
			}
			colorOrder.add(color);
		}
		
		private void renderAxis(Graphics g, int index, int startX, int startY, int height, int max, String family, int size) {
			renderAxis(g, index, startX, startY, height, max, family, size, "");
		}
		
		private void renderAxis(Graphics g, int index, int startX, int startY, int height, int max, String family, int size, String unit) {
			if (index >= colorOrder.size()) g.setColor(Color.BLACK);
			else g.setColor(colorOrder.get(index));
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
		
		private void renderLegend(Graphics g, String[] labels, String family, int size) {
			int startX = currentWidth - 144 - 32;
			int startY = (currentWidth >> 5) + 16;
			
			g.setColor(Color.WHITE);
			g.fillRect(startX, startY - 16, 144, 128);
			g.setColor(Color.BLACK);
			g.drawRect(startX, startY - 16, 144, 128);
			
			g.setFont(new Font(family, Font.PLAIN, size));
			
			int stringLength = labels.length;
			int colorLength = colorOrder.size();
			int smallestLength = Math.min(stringLength, colorLength);
			int height = g.getFontMetrics().getHeight();
			
			for (int j = 0; j < smallestLength; j++) {
				// Line
				int spacing = j * (height + 4);
				if (j >= colorLength) g.setColor(Color.GRAY);
				else g.setColor(colorOrder.get(j));
				g.drawLine(startX + 16, startY + spacing, startX + 32, startY + spacing);
				
				// Text
				g.setColor(new Color(24, 24, 24));
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
	public void run() {
		graphicsThread = new GraphicsThread();
		
		colorOrder = new ArrayList<Color>();
		
		mainFrame = initializeFrame(title, minimumWidth, minimumHeight);
		
		setBackground(Color.BLACK);
		setMinimumSize(new Dimension(minimumWidth, minimumHeight));
		setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		
		GUIPanel = initializeGUIPanel();
		 
		mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.PAGE_AXIS));
		mainFrame.add(this);
		mainFrame.add(GUIPanel);
		
		plenumPressureTextbox.setText(Double.toString(plenum_chamber_pressure));
		plenumTemperatureTextbox.setText(Double.toString(plenum_chamber_temperature));
		plenumVelocityTextbox.setText(Double.toString(plenum_chamber_velocity));
		plenumMachTextbox.setText(Double.toString(plenum_chamber_mach));
		
		setVisible(true);
		GUIPanel.setVisible(true);
		
		mainFrame.pack();
		
		mainFrame.setVisible(true);
		
		recalculateProperties();
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
	
	public double[] calculateVelocity(int length, double initialVelocity) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] velocities = new double[length];
		for (int i = 0; i < length; i++) {
			velocities[i] = initialVelocity;
		}
		return velocities;
	}
	
	public double[] calculateMach(int length, double[] staticTemperatures, double[] velocities) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] mach = new double[length];
		for (int i = 0; i < length; i++) {
			mach[i] = IsentropicRelations.mach(velocities[i], staticTemperatures[i], specific_heat_k, gas_constant_R);
		}
		return mach;
	}
	
	public double[] calculateStaticPressure(int length, double staticTemperatures[], double totalPressures[], double[] velocities) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] staticPressures = new double[length];
		for (int i = 0; i < length; i++) {
			staticPressures[i] = totalPressures[i] / IsentropicRelations.totalStaticPressureRatioFromTotalStaticTemperatureRatio(IsentropicRelations.totalStaticTemperatureRatio(staticTemperatures[i], velocities[i], specific_heat_p), specific_heat_k);
		}
		return staticPressures;
	}
	
	public double[] calculateStaticTemperature(int length, double[] totalTemperatures, double[] velocities) {
		if (length < 0) return null;
		else if (length == 0) length = 1;
		double[] staticTemperatures = new double[length];
		for (int i = 0; i < length; i++) {
			staticTemperatures[i] =IsentropicRelations.staticTemperatureFromTotalTemperature(totalTemperatures[i], velocities[i], specific_heat_p);
		}
		return staticTemperatures;
	}
	
	public void recalculateProperties() {
		totalPressurePoints = calculateTotalPressure(length, plenum_chamber_pressure);
		totalTemperaturePoints = calculateTotalTemperature(length, plenum_chamber_temperature);
		velocityPoints = calculateVelocity(length, plenum_chamber_velocity);
		staticTemperaturePoints = calculateStaticTemperature(length, totalTemperaturePoints, velocityPoints);
		staticPressurePoints = calculateStaticPressure(length, staticTemperaturePoints, totalPressurePoints, velocityPoints);
		machPoints = calculateMach(length, staticTemperaturePoints, velocityPoints);
		if (!(machPoints == null)) plenum_chamber_mach = machPoints[0];
		
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
			plenum_chamber_pressure = getPlenumChamberPropertyFromTextbox(plenumPressureTextbox, 0, 1000000, plenum_chamber_pressure);
		} else if (source.equals(plenumTemperatureTextbox)) {
			plenum_chamber_temperature = getPlenumChamberPropertyFromTextbox(plenumTemperatureTextbox, 0, 1000, plenum_chamber_temperature);
		} else if (source.equals(plenumVelocityTextbox)) {
			plenum_chamber_velocity = getPlenumChamberPropertyFromTextbox(plenumVelocityTextbox, 0, 1000, plenum_chamber_velocity);
		} else if (source.equals(plenumMachTextbox)) {
			plenum_chamber_mach = getPlenumChamberPropertyFromTextbox(plenumMachTextbox, 0, 10, plenum_chamber_mach);
		} else if (source.equals(legendCheckbox)) {
			legendEnabled = legendCheckbox.isSelected();
		} else if (source.equals(axesCheckbox)) {
			axesEnabled = axesCheckbox.isSelected();
		} else if (source.equals(gridCheckbox)) {
			gridEnabled = gridCheckbox.isSelected();
		} else if (source.equals(fineGridCheckbox)) {
			fineGridEnabled = fineGridCheckbox.isSelected();
		} else if (source.equals(backgroundCheckbox)) {
			backgroundEnabled = backgroundCheckbox.isSelected();
		}
	}
	
	public double getPlenumChamberPropertyFromTextbox(JTextField textbox, double min, double max, double init) {
		if (min > max) throw new IllegalArgumentException("Minimum is greater than maximum");
		double bounded_value;
		try {
			bounded_value = Math.min(max, Math.max(min, Double.parseDouble(textbox.getText()))); 
		}
		catch (NumberFormatException exc) {
			textbox.setText(Double.toString(init));
			return init;
		}
		
		textbox.setText(Double.toString(bounded_value));
		if (!(bounded_value == init)) recalculateProperties();
		return bounded_value;
	}
}
