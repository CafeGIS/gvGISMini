/*
*Basic drag and drop for Android
*Copyright (C) 2010  Eric Harlow
*
*This program is free software; you can redistribute it and/or
*modify it under the terms of the GNU General Public License
*as published by the Free Software Foundation; either version 2
*of the License, or (at your option) any later version.
*
*This program is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*GNU General Public License for more details.
*
*You should have received a copy of the GNU General Public License
*along with this program; if not, write to the Free Software
*Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.ericharlow.DragNDrop;

import android.view.View;
import android.widget.ListView;

/**
 * Implement to handle an item being dragged.
 *  
 * @author Eric Harlow
 */
public interface DragListener {
	/**
	 * Called when a drag starts.
	 * @param itemView - the view of the item to be dragged i.e. the drag view
	 */
	void onStartDrag(View itemView);
	
	/**
	 * Called when a drag is to be performed.
	 * @param x - horizontal coordinate of MotionEvent.
	 * @param y - verital coordinate of MotionEvent.
	 * @param listView - the listView
	 */
	void onDrag(int x, int y, ListView listView);
	
	/**
	 * Called when a drag stops.
	 * Any changes in onStartDrag need to be undone here 
	 * so that the view can be used in the list again.
	 * @param itemView - the view of the item to be dragged i.e. the drag view
	 */
	void onStopDrag(View itemView);
}
