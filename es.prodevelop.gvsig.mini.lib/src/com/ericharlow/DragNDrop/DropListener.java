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

/**
 * Implement to handle an item being dropped.
 * An adapter handling the underlying data 
 * will most likely handle this interface.
 *  
 * @author Eric Harlow
 */
public interface DropListener {
	
	/**
	 * Called when an item is to be dropped.
	 * @param from - index item started at.
	 * @param to - index to place item at.
	 */
	void onDrop(int from, int to);
}
