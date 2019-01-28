/*******************************************************************************
* Copyright (c) 2013, Saarland University. All rights reserved.
* Lisa Detzler
******************************************************************************/

package countdownZuZweit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;

public class Settings {

    public static HashMap<String, HashMap<Integer, HashSet<Integer>>> lineNumberMapping = new HashMap<String, HashMap<Integer, HashSet<Integer>>>();
    
    public static void initLineNumberMapping(){
        HashMap<Integer, HashSet<Integer>> map2;
        HashSet<Integer> set;
        
        map2 = new HashMap<Integer, HashSet<Integer>>();
        set = new HashSet<Integer>();
        map2.put(1, set);
        set = new HashSet<Integer>();
        map2.put(2, set);
        set = new HashSet<Integer>();
        map2.put(3, set);
        set = new HashSet<Integer>();
        map2.put(4, set);
        set = new HashSet<Integer>();
        map2.put(5, set);
        set = new HashSet<Integer>();
        map2.put(6, set);
        set = new HashSet<Integer>();
        map2.put(7, set);
        set = new HashSet<Integer>();
        map2.put(8, set);
        set = new HashSet<Integer>();
        map2.put(9, set);
        set = new HashSet<Integer>();
        map2.put(10, set);
        set = new HashSet<Integer>();
        map2.put(11, set);
        set = new HashSet<Integer>();
        set.add(1);
        map2.put(12, set);
        set = new HashSet<Integer>();
        map2.put(13, set);
        set = new HashSet<Integer>();
        set.add(2);
        map2.put(14, set);
        set = new HashSet<Integer>();
        set.add(3);
        map2.put(15, set);
        set = new HashSet<Integer>();
        map2.put(16, set);
        set = new HashSet<Integer>();
        set.add(3);
        set.add(4);
        map2.put(17, set);
        set = new HashSet<Integer>();
        map2.put(18, set);
        set = new HashSet<Integer>();
        set.add(5);
        map2.put(19, set);
        set = new HashSet<Integer>();
        map2.put(20, set);
        set = new HashSet<Integer>();
        map2.put(21, set);
        set = new HashSet<Integer>();
        set.add(4);
        map2.put(22, set);
        set = new HashSet<Integer>();
        set.add(4);
        map2.put(23, set);
        set = new HashSet<Integer>();
        set.add(8);
        map2.put(24, set);
        set = new HashSet<Integer>();
        map2.put(25, set);
        set = new HashSet<Integer>();
        set.add(9);
        map2.put(26, set);
        set = new HashSet<Integer>();
        set.add(10);
        map2.put(27, set);
        set = new HashSet<Integer>();
        set.add(5);
        map2.put(28, set);
        set = new HashSet<Integer>();
        set.add(4);
        map2.put(29, set);
        set = new HashSet<Integer>();
        set.add(4);
        set.add(10);
        map2.put(30, set);
        set = new HashSet<Integer>();
        map2.put(31, set);
        set = new HashSet<Integer>();
        map2.put(32, set);
        set = new HashSet<Integer>();
        set.add(10);
        map2.put(33, set);
        set = new HashSet<Integer>();
        set.add(11);
        map2.put(34, set);
        set = new HashSet<Integer>();
        map2.put(35, set);
        set = new HashSet<Integer>();
        set.add(8);
        map2.put(36, set);
        set = new HashSet<Integer>();
        set.add(11);
        map2.put(37, set);
        set = new HashSet<Integer>();
        map2.put(38, set);
        set = new HashSet<Integer>();
        map2.put(39, set);
        set = new HashSet<Integer>();
        set.add(9);
        set.add(11);
        map2.put(40, set);
        set = new HashSet<Integer>();
        map2.put(41, set);
        set = new HashSet<Integer>();
        set.add(12);
        map2.put(42, set);
        set = new HashSet<Integer>();
        set.add(10);
        map2.put(43, set);
        set = new HashSet<Integer>();
        map2.put(44, set);
        set = new HashSet<Integer>();
        map2.put(45, set);
        set = new HashSet<Integer>();
        set.add(10);
        set.add(13);
        map2.put(46, set);
        set = new HashSet<Integer>();
        map2.put(47, set);
        set = new HashSet<Integer>();
        map2.put(48, set);
        set = new HashSet<Integer>();
        map2.put(49, set);
        set = new HashSet<Integer>();
        set.add(14);
        map2.put(50, set);
        set = new HashSet<Integer>();
        set.add(10);
        map2.put(51, set);
        set = new HashSet<Integer>();
        map2.put(52, set);
        set = new HashSet<Integer>();
        map2.put(53, set);
        set = new HashSet<Integer>();
        set.add(11);
        map2.put(54, set);
        set = new HashSet<Integer>();
        map2.put(55, set);
        set = new HashSet<Integer>();
        map2.put(56, set);
        set = new HashSet<Integer>();
        set.add(11);
        map2.put(57, set);
        set = new HashSet<Integer>();
        map2.put(58, set);
        set = new HashSet<Integer>();
        map2.put(59, set);
        set = new HashSet<Integer>();
        map2.put(60, set);
        set = new HashSet<Integer>();
        map2.put(61, set);
        set = new HashSet<Integer>();
        set.add(11);
        map2.put(62, set);
        set = new HashSet<Integer>();
        map2.put(63, set);
        set = new HashSet<Integer>();
        map2.put(64, set);
        set = new HashSet<Integer>();
        map2.put(65, set);
        set = new HashSet<Integer>();
        set.add(12);
        map2.put(66, set);
        set = new HashSet<Integer>();
        map2.put(67, set);
        set = new HashSet<Integer>();
        map2.put(68, set);
        set = new HashSet<Integer>();
        map2.put(69, set);
        set = new HashSet<Integer>();
        map2.put(70, set);
        set = new HashSet<Integer>();
        map2.put(71, set);
        set = new HashSet<Integer>();
        set.add(13);
        map2.put(72, set);
        set = new HashSet<Integer>();
        map2.put(73, set);
        set = new HashSet<Integer>();
        map2.put(74, set);
        set = new HashSet<Integer>();
        map2.put(75, set);
        set = new HashSet<Integer>();
        map2.put(76, set);
        set = new HashSet<Integer>();
        map2.put(77, set);
        set = new HashSet<Integer>();
        set.add(14);
        map2.put(78, set);
        set = new HashSet<Integer>();
        map2.put(79, set);
        set = new HashSet<Integer>();
        map2.put(80, set);
        set = new HashSet<Integer>();
        map2.put(81, set);
        lineNumberMapping.put("Main", map2);
        
    }

    public static LinkedList<String> getListOfExternJavaFiles(){
        LinkedList<String> list = new LinkedList<String>();
        list.add("Boolchan.java");
        list.add("Intchan.java");
        list.add("Stringchan.java");
        list.add("IntChannel.java");
        list.add("BoolChannel.java");
        list.add("StringChannel.java");
        list.add("IntHandshakeChan.java");
        list.add("BoolHandshakeChan.java");
        list.add("StringHandshakeChan.java");
        list.add("PseuCoThread.java");
        list.add("Work.java");
        list.add("DefaultWork.java");
        list.add("Message.java");
        list.add("WorkList.java");
        list.add("Channel.java");
        list.add("Simulate.java");
        list.add("Handshake.java");
        list.add("ErrorHandling.java");
        list.add("Settings.java");
        list.add("CodeGenError.java");
        return list;
    }
    public static LinkedList<Integer> getPseuCoLineNumber(int javaLineNumber,
           String className, boolean onlyPositivNumbers) {
        // copy of lineNumberMapping
        HashMap<Integer, HashSet<Integer>> map2 = new HashMap<Integer, HashSet<Integer>>();
        map2.putAll(lineNumberMapping.get(className));
        LinkedList<Integer> returnList = new LinkedList<Integer>();
        if (map2.isEmpty()) {
            returnList.add(-1);
             return returnList;
        }
        if (map2.containsKey(javaLineNumber)) {
            HashSet<Integer> set = new HashSet<Integer>();
            if (onlyPositivNumbers) {
                // if list contains only negativ values, only return -1
                if (set.contains(-1) && set.contains(-2) && set.size() == 2) {
                    set.remove(-2);
                }
                // if list contains at least one postitiv value, remove all
                // negativ ones
                if (!(set.contains(-1) && set.size() == 1)
		                 && !(set.contains(-2) && set.size() == 1)) {
                    set.remove(-1);
                    set.remove(-2);
                }
            }
            returnList.addAll(map2.get(javaLineNumber));
        }
        return returnList;
    }
}