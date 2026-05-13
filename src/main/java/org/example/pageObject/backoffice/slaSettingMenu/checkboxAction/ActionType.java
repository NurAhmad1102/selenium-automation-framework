package org.example.pageObject.backoffice.slaSettingMenu.checkboxAction;

import java.util.Arrays;
import java.util.List;

public class ActionType {
    public static final List<String> ACTIONS = List.of(
            "Rename",
            "Add",
            "Move",
            "Merge",
            "Split",
            "Remove"
    );

    public static String getByIndex(int index){
        return ACTIONS.get(index);
    }

    public static List<String> getAll(){
        return ACTIONS;
    }

    public static int getSize(){
        return ACTIONS.size();
    }
}



