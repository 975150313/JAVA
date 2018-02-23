package com.aast.TestJavaCV;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;

public class ShowJavaCV {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShowJavaCV window = new ShowJavaCV();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ShowJavaCV() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 891, 544);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JCheckBox chckbxHaar = new JCheckBox("Haar分类器");
		chckbxHaar.setBounds(51, 33, 103, 23);
		frame.getContentPane().add(chckbxHaar);
		
		JCheckBox chckbxLbp = new JCheckBox("LBP分类器");
		chckbxLbp.setBounds(262, 33, 103, 23);
		frame.getContentPane().add(chckbxLbp);
		
		JButton btncamera = new JButton("开启Camera");
		btncamera.setBounds(180, 460, 93, 23);
		frame.getContentPane().add(btncamera);
	}
}
