package org.NooLab.somfluid.app.up;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class MsgDialog{
	
	String openingMsg = "";
	
	
	public MsgDialog(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	
	public boolean show(String msg) {
		openingMsg = msg;
		return show() ;
	}
	
	public boolean show(){
		boolean rB = false;
		if( JOptionPane.showConfirmDialog(null, openingMsg , 
												"NooLab SomFluid", 
												JOptionPane.OK_CANCEL_OPTION) == 0){
			
			// JOptionPane.showMessageDialog(null, "You clicked on \"Ok\" button", "NooLab SomFluid", 1);
			rB = true;
		}
		return rB;
	}
	public String getOpeningMsg() {
		return openingMsg;
	}
	public void setOpeningMsg(String openingMsg) {
		this.openingMsg = openingMsg;
	}
	
}
