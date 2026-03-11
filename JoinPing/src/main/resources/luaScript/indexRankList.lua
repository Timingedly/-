-- 查询Zset所有数据的Lua脚本 - 只返回排序后的member
local key, active, buffer = KEYS[1], KEYS[2], KEYS[3]

-- 确定数据源并获取数据（不要对应的score）
local source_key = redis.call('EXISTS', key) == 1 and buffer or active
local members = redis.call('ZRANGE', source_key, 0, -1)  -- 移除了WITHSCORES

return members