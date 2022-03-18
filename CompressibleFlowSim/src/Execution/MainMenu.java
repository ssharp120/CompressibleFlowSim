package Execution;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Functions.FileUtilities;

public class MainMenu extends JPanel implements KeyListener, MouseListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6706276688740706862L;

	private JFrame mainFrame;
	private String title = "Compressible Flow Sim";
	private int preferredWidth = 800;
	private int preferredHeight = 600;
	private final int minimumWidth = 800;
	private final int minimumHeight = 600;
	
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
		
		graphicsThread = new GraphicsThread();
		
		mainFrame = initializeFrame("Compressible Flow Sim", minimumWidth, minimumHeight);
		
		setBackground(Color.BLUE);
		setMinimumSize(new Dimension(minimumWidth, minimumHeight));
		setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		
		mainFrame.add(this);
		setVisible(true);
		
		mainFrame.pack();
		
		mainFrame.setVisible(true);
	}
	
	public JFrame initializeFrame(String title, int minimumWidth, int minimumHeight) {
		JFrame frame = new JFrame();
		
		frame.setTitle(title);
		frame.setBackground(Color.BLUE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
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
		
		return frame;
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
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, minimumWidth, minimumHeight);
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
		// TODO Auto-generated method stub
		
	}

}
