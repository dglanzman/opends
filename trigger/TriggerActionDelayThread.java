/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2015 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.opends.trigger;

/**
 * 
 * @author Rafael Math
 */
public class TriggerActionDelayThread extends Thread
{
	private TriggerAction triggerAction;
	private float seconds;
	
	
	public TriggerActionDelayThread(TriggerAction triggerAction, float seconds) 
	{
		this.triggerAction = triggerAction;
		this.seconds = seconds;
	}
	
	
	public void run() 
	{
		if(seconds > 0)
		{
			try {
				
				long milliSeconds = (long)(seconds*1000);
				sleep(milliSeconds);
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		
		triggerAction.execute();
	}
}
