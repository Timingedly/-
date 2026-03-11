-- 新增点赞/收藏
local key, active, buffer = KEYS[1], KEYS[2], KEYS[3]
local likeNum_key, favoriteNum_key = KEYS[4], KEYS[5]
local like_value, favo_value = ARGV[1], ARGV[2]

-- 确定数据源并获取数据（不要对应的score）
local source_key = redis.call('EXISTS', key) == 1 and buffer or active

-- 在source_key对应的Hash中更新likeNum和favoriteNum字段
if source_key then
    if like_value then
        redis.call('HSET', source_key, likeNum_key, like_value)
    end
    if favo_value then
        redis.call('HSET', source_key, favoriteNum_key, favo_value)
    end
end

return source_key