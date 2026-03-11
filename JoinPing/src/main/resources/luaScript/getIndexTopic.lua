-- 获取首页话题列表数据
-- data_getter.lua
local key = KEYS[1] --分布式锁的key
local active = KEYS[2] --正式数据key
local buffer = KEYS[3] --缓冲数据key

-- 检查 key 是否存在
local exists = redis.call('EXISTS', key)

local value
if exists == 1 then
    -- 如果 key 存在，从 buffer 获取数据
    value = redis.call('GET', buffer)
else
    -- 如果 key 不存在，从 active 获取数据
    value = redis.call('GET', active)
end

return value