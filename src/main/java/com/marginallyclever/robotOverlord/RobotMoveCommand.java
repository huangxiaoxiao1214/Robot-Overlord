package com.marginallyclever.robotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotWithConnection;


public class RobotMoveCommand extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// enumerate axies for movement commands
	// linear
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_Z = 2;
	// rotation
	public static final int AXIS_U = 3;
	public static final int AXIS_V = 4;
	public static final int AXIS_W = 5;
	// fk
	public static final int AXIS_A = 6;
	public static final int AXIS_B = 7;
	public static final int AXIS_C = 8;
	
	private RobotWithConnection robot;
	private int axis;
	private int direction;
	
	/**
	 * 
	 * @param robot which machine
	 * @param axis index of axis
	 * @param direction 1 or -1
	 */
	public RobotMoveCommand(RobotWithConnection robot,int axis,int direction) {
		this.robot = robot;
		this.axis = axis;
		this.direction = direction;
		moveNow(direction);
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		String name = "Move ";
		switch(axis) {
		case RobotMoveCommand.AXIS_X: name+=" X";  break;
		case RobotMoveCommand.AXIS_Y: name+=" Y";  break;
		case RobotMoveCommand.AXIS_Z: name+=" Z";  break;
		case RobotMoveCommand.AXIS_A: name+=" A";  break;
		case RobotMoveCommand.AXIS_B: name+=" B";  break;
		case RobotMoveCommand.AXIS_C: name+=" C";  break;
		case RobotMoveCommand.AXIS_U: name+=" U";  break;
		case RobotMoveCommand.AXIS_V: name+=" V";  break;
		case RobotMoveCommand.AXIS_W: name+=" W";  break;
		}
		if(direction>0) name += "+";
		name += Float.toString(direction);
		return name;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		moveNow(direction);
	}

	@Override
	public void undo() throws CannotUndoException {
		moveNow(-direction);
	}
	
	private void moveNow(int n) {
		robot.move(axis,n);
	}
}
