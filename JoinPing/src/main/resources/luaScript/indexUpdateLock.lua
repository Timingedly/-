-- 获取首页更新的分布式锁

local key = KEYS[1]
local time = tonumber(ARGV[1])

-- 直接使用SET NX，并根据返回结果判断
local result = redis.call('SET', key, 1, 'NX', 'EX', time)

if result then
    -- SET NX 成功，返回 1 (true)
    return 1
else
    -- SET NX 失败（key已存在），返回 0 (false)
    return 0
end