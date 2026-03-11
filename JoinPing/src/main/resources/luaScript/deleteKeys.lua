-- 删除多个键的 Lua 脚本,传入List<String> keyList,返回传入key的数量
for i, key in ipairs(KEYS) do
    redis.call('DEL', key)
end
return #KEYS