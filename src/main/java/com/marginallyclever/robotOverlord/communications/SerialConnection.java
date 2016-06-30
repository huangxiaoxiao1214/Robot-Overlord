package com.marginallyclever.robotOverlord.communications;

import jssc.*;

import java.awt.Color;
import java.awt.Component;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Created on 4/12/15.  Encapsulate all jssc serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7
 */
public class SerialConnection 
implements SerialPortEventListener, AbstractConnection {
	private String[] portsDetected;

	protected SerialPort serialPort;
	protected boolean portOpened=false;
	protected String portName;
	protected boolean waitingForCue=true;
	private String connectionName = "";

	public final static int BAUD_RATE = 57600;
	static final String cue=">";
	static final String NL="\n";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	
	
	// Menus & GUIs
	JTextArea log = new JTextArea();
	JScrollPane logPane;
    
    // Buffers
    String inputBuffer;
    ArrayList<String> commandQueue = new ArrayList<String>();

    // Listeners which should be notified of a change to the percentage.
    private ArrayList<AbstractConnectionListener> listeners = new ArrayList<AbstractConnectionListener>();

	
	public SerialConnection() {
		super();
	}
	
	public void log(String msg) {
		System.out.print(msg);
		log.append(msg);
		log.setCaretPosition(log.getText().length());
	}
	
	// tell all listeners data has arrived
	private void processLine(String line) {
	      for (AbstractConnectionListener listener : listeners) {
	        listener.dataAvailable(this,line);
	      }
	}
	
	@Override
	public void serialEvent(SerialPortEvent events) {
		String rawInput, oneLine;
		int x;
		
        if(events.isRXCHAR()) {
        	if(!portOpened) return;
            try {
            	int len = events.getEventValue();
				byte [] buffer = serialPort.readBytes(len);
				if( len>0 ) {
					rawInput = new String(buffer,0,len, StandardCharsets.UTF_8);
//					Log(rawInput);
					inputBuffer+=rawInput;
					// each line ends with a \n.
					for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
						x=x+1;
						oneLine = inputBuffer.substring(0,x);
						inputBuffer = inputBuffer.substring(x);
						processLine(oneLine);
						// wait for the cue to send another command
						if(oneLine.indexOf(cue)==0) {
							waitingForCue=false;
						}
					}
					if(!waitingForCue) {
						sendQueuedCommand();
					}
				}
            } catch (SerialPortException e) {}
        }
	}
	
	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;
		
		if(commandQueue.size()==0) {
		      notifyListeners();
		      return;
		}
		
		String command;
		try {
			command=commandQueue.remove(0);
			String line = command;
			if(line.contains(";")) {
				String [] lines = line.split(";");
				command = lines[0];
			}
			log(command+NL);
			line+=NL;
			serialPort.writeBytes(line.getBytes(StandardCharsets.UTF_8));
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(SerialPortException e2) {}
	}
	
	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}
	
	public boolean readyForCommands() {
		return !waitingForCue;
	}
	
	
	public boolean doesPortExist(String portName) {
		if(portName==null || portName.equals("")) return false;

		int i;
		for(i=0;i<portsDetected.length;++i) {
			if(portName.equals(portsDetected[i])) {
				return true;
			}
		}
		
		return false;
	}
	
    public void addListener(AbstractConnectionListener listener) {
      listeners.add(listener);
    }
    
    public void removeListener(AbstractConnectionListener listener) {
    	listeners.remove(listener);
    }

    private void notifyListeners() {
      for (AbstractConnectionListener listener : listeners) {
        listener.connectionReady(this);
      }
    }
	

	public Component getGUI() {
	    // the log panel
	    log.setEditable(false);
	    log.setForeground(Color.GREEN);
	    log.setBackground(Color.BLACK);
	    logPane = new JScrollPane(log);
	    
	    return logPane;
	}

	
	@Override
	public void close() {
		if(!portOpened) return;
		
	    if (serialPort != null) {
	        try {
		        // Close the port.
		        serialPort.removeEventListener();
		        serialPort.closePort();
	        } catch (SerialPortException e) {
	            // Don't care
	        }
	    }

		portOpened=false;
		connectionName="";
	}

	@Override
	public void open(String portName) throws Exception {
		if(portOpened) return;
		
		serialPort = new SerialPort(portName);
        serialPort.openPort();// Open serial port
        serialPort.setParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
        serialPort.addEventListener(this);

		portOpened=true;
		connectionName=portName;
	}

	@Override
	public void reconnect() throws Exception {}

	@Override
	public boolean isConnectionOpen() {
		return portOpened;
	}

	@Override
	public String getRecentConnection() {
		return connectionName;
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if(!portOpened) return;
		
		commandQueue.add(msg);
		sendQueuedCommand();
	}
}
