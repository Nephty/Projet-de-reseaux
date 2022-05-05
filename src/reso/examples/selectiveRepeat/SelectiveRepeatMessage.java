package reso.examples.selectiveRepeat;

import reso.common.Message;

public class SelectiveRepeatMessage implements Message {

    public final String msg;

    public SelectiveRepeatMessage(String msg){
        this.msg = msg;
    }

    public String toString() {
        return "SelectiveRepeat [msg="+msg+"]";
    }

    @Override
    public int getByteLength() {
        return (Character.SIZE * msg.length()) / 8;
    }
}
