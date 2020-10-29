package utils;

/**
 * Locates the database Properties File for Database manipulation methods. This
 * file contains the application sign on credentials for the database. <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark
 */

public class InvalidCountdownStateException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3421841348651773178L;

	/**
	 * 
	 */

	public InvalidCountdownStateException(String errorMessage) {
		super(errorMessage);
	}
}
