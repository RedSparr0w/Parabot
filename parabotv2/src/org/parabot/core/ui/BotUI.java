package org.parabot.core.ui;

import org.parabot.core.Context;
import org.parabot.core.ui.components.GamePanel;
import org.parabot.core.ui.images.Images;
import org.parabot.environment.scripts.Script;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * The bot user interface
 * 
 * @author Dane, Everel
 * 
 */
public class BotUI extends JFrame implements ActionListener, ComponentListener, WindowListener {

	private static final long serialVersionUID = -2126184292879805519L;
	private static BotUI instance;
	private static JDialog dialog;
	
	private JMenuItem run, pause, stop;
	private boolean runScript, pauseScript;

	public static BotUI getInstance() {
		return instance == null ? instance = new BotUI() : instance;
	}

	private BotUI() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		
		setTitle("Parabot");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(Images.getResource("/org/parabot/core/ui/images/icon.png"));

        /** Adds the dock icon to mac users */
        try {
            Class util = Class.forName("com.apple.eawt.Application");
            Method getApplication = util.getMethod("getApplication", new Class[0]);
            Object application = getApplication.invoke(util);
            Class params[] = new Class[1];
            params[0] = Image.class;
            Method setDockIconImage = util.getMethod("setDockIconImage", params);
            setDockIconImage.invoke(application, Images.getResource("/org/parabot/core/ui/images/icon.png"));
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // Ignore for windows
        }

		setLayout(new BorderLayout());
		addComponentListener(this);
		addWindowListener(this);

		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		JMenu scripts = new JMenu("Script");
		
		JMenuItem proxy = new JMenuItem("Network");
        JMenuItem dialog = new JCheckBoxMenuItem("Disable dialog");

		JMenuItem exit = new JMenuItem("Exit");
		
		run = new JMenuItem("Run");
		run.setIcon(new ImageIcon(Images.getResource("/org/parabot/core/ui/images/run.png")));
		
		pause = new JMenuItem("Pause");
		pause.setEnabled(false);
		pause.setIcon(new ImageIcon(Images.getResource("/org/parabot/core/ui/images/pause.png")));
		
		stop = new JMenuItem("Stop");
		stop.setEnabled(false);
		stop.setIcon(new ImageIcon(Images.getResource("/org/parabot/core/ui/images/stop.png")));
		
		proxy.addActionListener(this);
		dialog.addActionListener(this);
		exit.addActionListener(this);
		
		run.addActionListener(this);
		pause.addActionListener(this);
		stop.addActionListener(this);
		
		file.add(proxy);
        file.add(dialog);
		file.add(exit);
		
		scripts.add(run);
		scripts.add(pause);
		scripts.add(stop);
		
		menuBar.add(file);
		menuBar.add(scripts);

		setJMenuBar(menuBar);

		add(GamePanel.getInstance());
		GamePanel.getInstance().addLoader();

		pack();
		setLocationRelativeTo(null);
		BotDialog.getInstance(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		switch (command) {
			case "Exit":
				System.exit(0);
				break;
            case "Network":
                NetworkUI.getInstance().setVisible(true);
                break;
            case "Run":
            	if(pauseScript) {
            		pauseScript = false;
            		pause.setEnabled(true);
            		run.setEnabled(false);
            		setScriptState(Script.STATE_RUNNING);
            		break;
            	}
        		new ScriptSelector().setVisible(true);
            	break;
            case "Pause":
            	setScriptState(Script.STATE_PAUSE);
            	pause.setEnabled(false);
            	run.setEnabled(true);
            	pauseScript = true;
            	break;
            case "Stop":
            	setScriptState(Script.STATE_STOPPED);
            	break;
            case "Disable dialog":
            	BotDialog.getInstance().setVisible(!dialog.isVisible());
            	break;
			default:
				System.out.println("Invalid command: " + command);
		}
	}
	
	protected void setDialog(JDialog dialog) {
		BotUI.dialog = dialog;
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		if(dialog == null || !isVisible()) {
			return;
		}
		Point gameLocation = GamePanel.getInstance().getLocationOnScreen();
		dialog.setLocation(gameLocation.x, gameLocation.y);
	}
	
	public void toggleRun() {
		runScript = !runScript;
		if(runScript) {
			scriptRunning();
		} else {
			scriptStopped();
		}
	}
	
	private void scriptRunning() {
		run.setEnabled(false);
		pause.setEnabled(true);
		stop.setEnabled(true);
	}
	
	private void scriptStopped() {
		run.setEnabled(true);
		pause.setEnabled(false);
		stop.setEnabled(false);
	}
	
	private void setScriptState(int state) {
		Context.getInstance().getRunningScript().setState(state);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if(isVisible()) {
			BotDialog.getInstance().setSize(getSize());
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		if(isVisible()) {
			BotDialog.getInstance().setVisible(false);
			BotDialog.getInstance().setVisible(true);
		}
	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
