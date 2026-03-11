package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 话题类别枚举
 */
@Getter
@AllArgsConstructor
public enum AreaEnum {
    MUSIC(1, "音乐"),
    GAME(2, "游戏");
    
    private final Integer id;
    private final String description;
    
    
    /**
     * 判断是否存在指定的value值
     *
     * @param value 要判断的值，为null时返回false
     * @return 如果存在该value则返回true，否则返回false
     */
    public static boolean containsValue(Integer value) {
        if (value == null) {
            return false;
        }
        
        for (AreaEnum area : AreaEnum.values()) {
            if (area.id.equals(value)) {
                return true;
            }
        }
        return false;
    }
    
}
