package com.example.joinping.constant;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * Lua脚本相关常量类
 */
public class LuaScriptConstants {
    
    /**
     * Lua脚本预加载，分布式限流锁的解锁
     */
    public static final String UNLOCK_OPERATIONLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";
    
    
    /**
     * lua文件存放路径（src/main/resource包下）
     */
    public static final String LUA_SCRIPT_PATH = "luaScript/";
    
    /**
     * 尝试获取首页热榜更新的分布式锁
     *
     * @return
     */
    public static DefaultRedisScript<Integer> indexUpdateLock() {
        DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH + "indexUpdateLock.lua"));
        script.setResultType(Integer.class);
        return script;
    }
    
    /**
     * 获取首页热榜话题
     *
     * @return
     */
    public static DefaultRedisScript<String> getIndexTopic() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH + "getIndexTopic.lua"));
        script.setResultType(String.class);
        return script;
    }
    
    /**
     * 删除List<String>内所有key
     *
     * @return
     */
    public static DefaultRedisScript<Integer> deleteKeys() {
        DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH + "deleteKeys.lua"));
        script.setResultType(Integer.class);
        return script;
    }
    
    /**
     * 获取热榜整个排行榜
     *
     * @return
     */
    public static DefaultRedisScript<List> indexRankList() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH + "indexRankList.lua"));
        script.setResultType(List.class);
        return script;
    }
    
    /**
     * 点赞/收藏热榜话题
     *
     * @return
     */
    public static DefaultRedisScript<Integer> updateIndexTopicHash() {
        DefaultRedisScript<Integer> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(LUA_SCRIPT_PATH + "updateIndexTopicHash.lua"));
        script.setResultType(Integer.class);
        return script;
    }
}
