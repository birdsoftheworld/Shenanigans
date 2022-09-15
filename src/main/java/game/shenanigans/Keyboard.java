package game.shenanigans;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;

public class Keyboard implements KeyListener {
    public static boolean keys[]= new boolean[65535];
    public static int lastCheckPressed = -1;
    public static int lastPressed = -1;

    public void keyPressed(KeyEvent e){
        keys[e.getKeyCode()] = true;
        if(lastCheckPressed != e.getKeyCode()){
            lastPressed = e.getKeyCode();
            lastCheckPressed = e.getKeyCode();
        }
    }

    public void keyReleased(KeyEvent e){
        keys[e.getKeyCode()] = false;
        lastCheckPressed = -1;
    }

    public void keyTyped(KeyEvent e){

    }

    public static int lastKeyPressed(){
        int last = lastPressed;
        lastPressed = -1;
        return last;
    }

    public static boolean isKeyDown(int keyCode){
        return keys[keyCode];
    }



}
