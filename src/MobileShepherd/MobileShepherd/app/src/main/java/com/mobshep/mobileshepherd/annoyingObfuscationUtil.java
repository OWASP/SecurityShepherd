package com.mobshep.mobileshepherd;

import android.util.Base64;


/**
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
 * @author Sean Duggan
 */

public class annoyingObfuscationUtil {

    public String Obfuscation1(String input){
        //base64
        byte[] base64String = Base64.encode(input.getBytes(), Base64.DEFAULT);
        String output = new String (base64String);
        return output;
    }


    public String Deobfuscation1(String input){
        //base64
        byte[] base64decoded = Base64.decode(input.getBytes(), Base64.DEFAULT);
        String output = new String (base64decoded);
        return output;
    }


    public String Obfuscation2(String input){
        //XOR

        String output ="";

        return output;
    }


    public String Obfuscation3(String input){

        String output ="";

        return output;
    }


    public String Obfuscation4(String input){

        String output ="";

        return output;
    }


    public String Obfuscation5(String input){

        String output ="";

        return output;
    }

    public String Obfuscation6(String input){

        String output ="";

        return output;
    }



}
